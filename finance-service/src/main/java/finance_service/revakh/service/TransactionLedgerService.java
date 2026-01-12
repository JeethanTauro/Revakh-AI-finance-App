package finance_service.revakh.service;

import finance_service.revakh.DTO.TransactionLedgerRequestDTO;
import finance_service.revakh.DTO.TransactionLedgerResultDTO;
import finance_service.revakh.events.TransactionCreatedEvent;
import finance_service.revakh.exceptions.OptimisticRetryFailedException;
import finance_service.revakh.exceptions.InsufficientBalanceException;
import finance_service.revakh.exceptions.TransactionNotFound;
import finance_service.revakh.exceptions.UserNotFoundException;
import finance_service.revakh.messages.TransactionEventPublisher;
import finance_service.revakh.models.*;
import finance_service.revakh.repo.CategoryRepo;
import finance_service.revakh.repo.FinanceUserRepo;
import finance_service.revakh.repo.TransactionLedgerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static finance_service.revakh.models.TransactionType.DEBIT;

//also done not need many changes
@Service
public class TransactionLedgerService {


    private final TransactionLedgerRepo ledgerRepo;
    private final WalletService walletService;
    private final PlatformTransactionManager txManager;
    private final FinanceUserService financeUserService;
    private final CategoryService categoryService;
    private final TransactionEventPublisher transactionEventPublisher;
    private static final int MAX_RETRY = 3;

    // 2. CONSTRUCTOR INJECTION WITH @LAZY
    public TransactionLedgerService(TransactionLedgerRepo ledgerRepo,
                                    WalletService walletService,
                                    PlatformTransactionManager txManager,
                                    TransactionEventPublisher transactionEventPublisher,
                                    @Lazy FinanceUserService financeUserService, // <--- ADD @LAZY HERE
                                    @Lazy CategoryService categoryService) {
        this.ledgerRepo = ledgerRepo;
        this.walletService = walletService;
        this.txManager = txManager;
        this.transactionEventPublisher = transactionEventPublisher;
        this.financeUserService = financeUserService;
        this.categoryService = categoryService;

    }

    /**
     * Orchestrates: validate -> insert ledger -> update wallet (atomic).
     * Retries whole transactional unit on optimistic locking failures.
     */
    public TransactionLedgerResultDTO createTransaction(TransactionLedgerRequestDTO dto) {

        // Basic request validation (defensive)
        if (dto == null) throw new IllegalArgumentException("Request is null");
        if (dto.getUserId() == null) throw new IllegalArgumentException("userId is required");
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("amount must be > 0");
        if (dto.getCategoryName() == null || dto.getCategoryName().trim().isEmpty())
            throw new IllegalArgumentException("categoryName is required");
        if (dto.getTransactionType() == null || dto.getTransactionType().toString().trim().isEmpty())
            throw new IllegalArgumentException("transactionType is required");
        if (dto.getSource() == null || dto.getSource().toString().trim().isEmpty())
            throw new IllegalArgumentException("source is required");

        // Resolve enums early and handle invalid values with clear messages
        final TransactionType type;
        final Source source;
        try {
            type = dto.getTransactionType();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid transactionType. Allowed: CREDIT or DEBIT");
        }
        try {
            source = dto.getSource();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid source. Allowed: MANUAL, UPI, NET_BANKING");
        }

        // Fetch required domain entities (validate cross-entity rules)
        final FinanceUser user = financeUserService.getUser(dto.getUserId());

        final Category category = categoryService.getCategory(user, dto.getCategoryName());

        // Validate category <-> transaction type rules
        if (category.getType() == CategoryType.INCOME && type != TransactionType.CREDIT) {
            throw new IllegalArgumentException("INCOME category accepts only CREDIT transactions");
        }
        if (category.getType() == CategoryType.EXPENSE && type != DEBIT) {
            throw new IllegalArgumentException("EXPENSE category accepts only DEBIT transactions");
        }

        // Prepare transaction template for programmatic transaction control
        final TransactionTemplate txTemplate = new TransactionTemplate(txManager);

        int attempt = 0;
        while (true) {
            attempt++;
            try {
                TransactionLedgerResultDTO result =  txTemplate.execute(status -> {
                    // 1. UPDATE WALLET FIRST
                    // This locks the wallet row immediately, preventing race conditions.
                    // It also returns the *new* balance we need for the ledger.
                    Wallet updatedWallet = walletService.updateBalance(user, dto.getAmount(), type);

                    // 2. CREATE LEDGER (With the final balance already set)
                    TransactionLedger ledger = TransactionLedger.builder()
                            .financeUser(user)
                            .category(category)
                            .amount(dto.getAmount())
                            .transactionType(type)
                            .source(source)
                            .description(dto.getDescription())
                            .balanceAfterTransaction(updatedWallet.getBalance()) // Set it NOW
                            .isDeleted(false)
                            .build();

                    // 3. SAVE LEDGER (One DB Hit)
                    ledger = ledgerRepo.save(ledger);


                    // 4. Return Result
                    return TransactionLedgerResultDTO.builder()
                            .transactionId(ledger.getTransactionId())
                            .categoryName(ledger.getCategory().getName())
                            .userId(ledger.getFinanceUser().getUserId())
                            .userName(ledger.getFinanceUser().getUserName())
                            .amount(ledger.getAmount())
                            .type(ledger.getTransactionType())
                            .source(ledger.getSource())
                            .description(ledger.getDescription())
                            .balance(ledger.getBalanceAfterTransaction())
                            .build();
                });
                transactionEventPublisher.transactionCreatedPublisher(mapToEvent(result));
                return result;

            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt >= MAX_RETRY) {
                    throw new OptimisticRetryFailedException("System busy, please try again.");
                }
                // Loop automatically retries
            } catch (InsufficientBalanceException | IllegalArgumentException e) {
                throw e; // Do not retry business errors
            }
        }
    }

    @Transactional
    public void softDeleteLedgerForUser(FinanceUser user) {
        List<TransactionLedger> ledgers = ledgerRepo.findAllByFinanceUserAndIsDeletedFalse(user);
        ledgers.forEach(l -> l.setDeleted(true));
        ledgerRepo.saveAll(ledgers);
    }

    public List<TransactionLedger> getAllTransactions(Long userId){
        FinanceUser financeUser = financeUserService.getUser(userId);
        List<TransactionLedger> ledgers = ledgerRepo.findAllByFinanceUserAndIsDeletedFalse(financeUser);
        return ledgers;
    }

    public void deleteOneTransaction(Long userId, Long transactionId){
        FinanceUser financeUser = financeUserService.getUser(userId);
        TransactionLedger ledger = ledgerRepo.findByFinanceUserAndTransactionIdAndIsDeletedFalse(financeUser,transactionId).orElseThrow(()-> new TransactionNotFound("Transaction not found"));
        ledger.setDeleted(true);
        ledgerRepo.save(ledger);
    }
    public TransactionLedger getTransaction(Long userId, Long transactionId){
        FinanceUser financeUser = financeUserService.getUser(userId);
        TransactionLedger ledger = ledgerRepo.findByFinanceUserAndTransactionIdAndIsDeletedFalse(financeUser,transactionId).orElseThrow(()-> new TransactionNotFound("Transaction not found"));
        return ledger;
    }

    public boolean transactionExistsByCategory(Category category){
        return ledgerRepo.existsByCategory(category);
    }

    public BigDecimal computeSpent(FinanceUser user, Category category, LocalDate periodStart,LocalDate periodEnd){
        return ledgerRepo.computeSpent(user,category,periodStart,periodEnd);
    }

    private TransactionCreatedEvent mapToEvent(TransactionLedgerResultDTO dto) {
        return TransactionCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(dto.getUserId())
                .transactionId(dto.getTransactionId())
                .amount(dto.getAmount())
                .type(dto.getType().toString())        // Enum to String
                .category(dto.getCategoryName())
                .description(dto.getDescription())
                .currency("INR")                       // Hardcoded or fetched from config
                .occurredAt(LocalDateTime.now())
                .build();
    }
}

