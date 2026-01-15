package finance_service.revakh.service;

import finance_service.revakh.DTO.FinanceUserDTO;
import finance_service.revakh.DTO.FinanceUserUpdateDTO;
import finance_service.revakh.events.UserCreatedEvent;
import finance_service.revakh.events.UserDeletedEvent;
import finance_service.revakh.exceptions.UserAlreadyExistsException;
import finance_service.revakh.exceptions.UserNotFoundException;
import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.repo.FinanceUserRepo;
import finance_service.revakh.repo.TransactionLedgerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//all done
@Service
public class FinanceUserService {
    private final FinanceUserRepo financeUserRepo;
    private final WalletService walletService;
    private final CategoryService categoryService;
    private final TransactionLedgerService transactionLedgerService;
    private final BudgetService budgetService;

    // 2. MANUAL CONSTRUCTOR WITH @LAZY
    // We use @Lazy on all services to prevent any future circular dependency loops
    public FinanceUserService(FinanceUserRepo financeUserRepo,
                              @Lazy WalletService walletService,
                              @Lazy CategoryService categoryService,
                              @Lazy TransactionLedgerService transactionLedgerService,
                              @Lazy BudgetService budgetService) {
        this.financeUserRepo = financeUserRepo;
        this.walletService = walletService;
        this.categoryService = categoryService;
        this.transactionLedgerService = transactionLedgerService;
        this.budgetService = budgetService;
    }
    //create user
    @Transactional(rollbackFor = Exception.class)
    public void userCreate(UserCreatedEvent event){
        if(financeUserRepo.existsById(event.getUserId())){
             throw new UserAlreadyExistsException("User already Exists");
        }
        FinanceUser financeUser = FinanceUser.builder()
                .userId(event.getUserId())
                .userName(event.getUserName())
                .userEmail(event.getUserEmail())
                .userInternationalCode(event.getUserInternationalCode())
                .userPhone(event.getUserPhone())
                .isActive(true)
                .build();

        financeUserRepo.save(financeUser); //finance user saved

        //create wallet for the user
        walletService.createWallet(financeUser);

        //create category for the user
        categoryService.createDefaultCategories(financeUser);

    }
    @Transactional
    public void userDelete(UserDeletedEvent event){
        Long userId = event.getUserId();
        FinanceUser financeUser = financeUserRepo.findById(userId).orElseThrow(()->new UserNotFoundException("User not found"));
        //soft delete transactions
        transactionLedgerService.softDeleteLedgerForUser(financeUser);

        //delete budgets
        budgetService.deleteAllBudgetForTheUser(financeUser);
        //delete category
        categoryService.deleteAllCategoriesForUser(financeUser);

        //delete wallet
        walletService.deleteWallet(financeUser);

        //finally delete the user
        financeUser.setActive(false);
        financeUserRepo.save(financeUser);
    }
    /** Helper: fetch user */
    public FinanceUser getUser(Long userId) {
        return financeUserRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Finance user not found"));
    }

    /** Helper:	validate user existence */
    public boolean exists(Long userId) {
        return financeUserRepo.existsById(userId);
    }
    // Inside FinanceUserService.java


    //Auth service is the truth right
    //so if a user is created there its created here,
    //if user is deleted there, then it must be deleted here
    //if a user is updated there, then the user must be updated here
    //anything related to user, for example updating, getting information must be done in the auth service

}
