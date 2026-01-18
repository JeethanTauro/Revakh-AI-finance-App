package finance_service.revakh.service;

import finance_service.revakh.exceptions.CategoryExceptions.CategoryExistsException;
import finance_service.revakh.exceptions.CategoryExceptions.CategoryNotFoundException;
import finance_service.revakh.models.Category;
import finance_service.revakh.models.CategoryType;
import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.models.SystemCategories;
import finance_service.revakh.repo.CategoryRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


//completely done nothing is missing in this service
//its a go ahead

@Service
@Slf4j
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
    //only if the user gets deleted
    @Transactional
    public void deleteAllCategoriesForUser(FinanceUser user) {
        List<Category> categories = categoryRepo.findAllByFinanceUserAndIsActiveTrue(user);
        for(Category category : categories){
            category.setActive(false);
        }
        categoryRepo.saveAll(categories);
    }

    //delete one category
    //now the user wnats to delete a category, we will delete all the respective budgets but still keep the transactions
    @Transactional
    public void deleteOneCategory(FinanceUser user, Long categoryId){
        //imagine u have alrewady some ledger entries for this category how will u delete it, u will basically lose data
        // also dont delete default categories

        Category category = categoryRepo.findByFinanceUserAndCategoryIdAndIsActiveTrue(user,categoryId);
        if(category == null){
            throw new CategoryNotFoundException("Category Not Found");
        }
        if (category.isSystem()) {
            throw new RuntimeException("System default categories cannot be deleted");
        }

        if (budgetService.categoryHasActiveBudgets(category)) {
            budgetService.deactivateBudgetsByCategory(category);
        }

        category.setActive(false);
        categoryRepo.save(category);

        log.info("Category [{}] and its budgets were deactivated for user [{}]",
                category.getName(), user.getUserId());
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
