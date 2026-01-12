package finance_service.revakh.service;

import finance_service.revakh.exceptions.*;
import finance_service.revakh.models.Category;
import finance_service.revakh.models.CategoryType;
import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.models.SystemCategories;
import finance_service.revakh.repo.BudgetRepo;
import finance_service.revakh.repo.CategoryRepo;
import finance_service.revakh.repo.FinanceUserRepo;
import finance_service.revakh.repo.TransactionLedgerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


//completely done nothing is missing in this service
//its a go ahead

@Service
public class CategoryService {
    private final CategoryRepo categoryRepo;
    private final TransactionLedgerService transactionLedgerService;
    private final FinanceUserService financeUserService;
    private final BudgetService budgetService;

    // 2. Remove @RequiredArgsConstructor and use Manual Constructor with @Lazy
    public CategoryService(CategoryRepo categoryRepo,
                           @Lazy TransactionLedgerService transactionLedgerService,
                           @Lazy FinanceUserService financeUserService,
                           @Lazy BudgetService budgetService) {
        this.categoryRepo = categoryRepo;
        this.transactionLedgerService = transactionLedgerService;
        this.financeUserService = financeUserService;
        this.budgetService = budgetService;
    }
    //create categories by default
    public void createDefaultCategories(FinanceUser financeUser) {

        if (categoryRepo.existsByFinanceUser(financeUser)) {
            return;
        }

        List<Category> defaults = Arrays.stream(SystemCategories.values())
                .map(sc -> Category.builder()
                        .financeUser(financeUser)
                        .name(sc.name())           // Store enum name
                        .type(sc.getType())        // Store mapped type
                        .isSystem(true)
                        .isActive(true)
                        .build())
                .toList();

        categoryRepo.saveAll(defaults);
    }


    //delete all category belongiing to a user
    @Transactional
    public void deleteAllCategoriesForUser(FinanceUser user) {
        List<Category> categories = categoryRepo.findAllByFinanceUserAndIsActiveTrue(user);
        for(Category category : categories){
            category.setActive(false);
        }
        categoryRepo.saveAll(categories);
    }

    //delete one category
    @Transactional
    public void deleteOneCategory(FinanceUser user, String name){
        //imagine u have alrewady some ledger entries for this category how will u delete it, u will basically lose data
        // also dont delete default categories
        // userrs must manuailly delete their transactions before deleing their categories, much safer
        // Normalize Case
        String normalizedName = name.trim().toUpperCase();

        Category category = categoryRepo.findByFinanceUserAndNameIgnoreCaseAndIsActiveTrue(user, normalizedName)
                .orElseThrow(() -> new CategoryNotFoundException("Category Not Found"));

        if( transactionLedgerService.transactionExistsByCategory(category)){
            throw new CategoryHasTransactionException("Category has a transaction");
        }

        boolean hasActiveBudgets = budgetService.categoryHasActiveBudgets(category);
        if (hasActiveBudgets) {
            throw new CategoryHasBudgetException("Category has an active budget");
        }

        if (category.isSystem()) {
            throw new RuntimeException("System default categories cannot be deleted");
        }

        category.setActive(false);
        categoryRepo.save(category);
    }
    //add category
    @Transactional
    public Category addCategory(FinanceUser user, String name, CategoryType type) {
        String normalizedName = name.trim().toUpperCase();

        if (categoryRepo.existsByFinanceUserAndNameIgnoreCaseAndIsActiveTrue(user, normalizedName)) {
            throw new CategoryExistsException("Category already exists");
        }
        // Validate category type logic
        if (type == CategoryType.INCOME && !normalizedName.equals("SALARY") && !normalizedName.equals("TOP_UP")) {
            throw new IllegalArgumentException("Only SALARY and TOP_UP allowed under INCOME type");
        }

        // Prevent overriding system category names
        try {
            SystemCategories.valueOf(name.toUpperCase());
            throw new CategoryExistsException("Name is reserved for system categories");
        } catch (IllegalArgumentException ignored) {
        }

        Category category = Category.builder()
                .financeUser(user)
                .name(name)
                .type(type)
                .isSystem(false)
                .isActive(true)
                .build();

        return categoryRepo.save(category);
    }

    public Category getCategory(FinanceUser user, String name) {
        return categoryRepo.findByFinanceUserAndNameAndIsActiveTrue(user, name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + name));
    }
    public Category getCategoryByIdAndUser(Long id,FinanceUser financeUser){
        return categoryRepo.findByFinanceUserAndCategoryId(id,financeUser).orElseThrow(()->new CategoryNotFoundException("Category not found"));
    }

    public List<Category> getAllCategory(Long userId){
        FinanceUser financeUser = financeUserService.getUser(userId);
        List<Category> categories = categoryRepo.findAllByFinanceUserAndIsActiveTrue(financeUser);
        return categories;
    }

}
