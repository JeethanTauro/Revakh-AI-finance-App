package finance_service.revakh.service;

import finance_service.revakh.DTO.TransactionLedgerRequestDTO;
import finance_service.revakh.exceptions.InsufficientBalanceException;
import finance_service.revakh.exceptions.WalletNotFoundException;
import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.models.TransactionType;
import finance_service.revakh.models.Wallet;
import finance_service.revakh.repo.FinanceUserRepo;
import finance_service.revakh.repo.WalletRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

//wallet service is also done
@RequiredArgsConstructor
@Service
@Slf4j
public class WalletService {


    private final WalletRepo walletRepo;

    @Transactional(rollbackFor = Exception.class)
    public void createWallet(FinanceUser user) {

        if (walletRepo.findByFinanceUser(user).isPresent()) {
            log.info("Wallet already exists for user {}", user.getUserId());
            return;
        }

        Wallet wallet = Wallet.builder()
                .financeUser(user)
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .currency("INR")
                .build();

        walletRepo.save(wallet);
    }

    @Transactional
    public Wallet updateBalance(FinanceUser user, BigDecimal amount, TransactionType type) {
        Wallet wallet = walletRepo.findByFinanceUserAndIsActiveTrue(user)
                .orElseThrow(() -> new WalletNotFoundException("Active wallet not found"));
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance;

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (!wallet.getFinanceUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("Wallet does not belong to this user");
        }
        if (type == TransactionType.CREDIT && amount.signum() <= 0)
            throw new IllegalArgumentException("Credit amount must be positive");

        if (type == TransactionType.DEBIT && amount.signum() <= 0)
            throw new IllegalArgumentException("Debit amount must be positive");

        if (type == TransactionType.CREDIT) {
            newBalance = oldBalance.add(amount);
        } else { //DEBIT
            if (oldBalance.compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Not enough balance");
            }
            newBalance = oldBalance.subtract(amount);
        }


        wallet.setBalance(newBalance);
        Wallet savedWallet = walletRepo.save(wallet);
        walletRepo.flush(); // force version increment + DB sync
        return savedWallet;
    }

    @Transactional
    public void deleteWallet(FinanceUser user) {
        // FIX: Removed the "check balance > 0" constraint.
        // If a user deletes their account, we Soft Delete the wallet regardless of balance.
        // The money is effectively "forfeited" or archived.
        walletRepo.findByFinanceUserAndIsActiveTrue(user)
                .ifPresent(wallet -> {
                    wallet.setActive(false);
                    walletRepo.save(wallet);
                    log.info("Wallet soft-deleted for user {}", user.getUserId());
                });
    }

    public Wallet getWallet(FinanceUser user) {
        Wallet wallet = walletRepo.findByFinanceUserAndIsActiveTrue(user)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        return wallet;
    }
}

