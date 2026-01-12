package finance_service.revakh.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public enum SystemCategories{
    SALARY(CategoryType.INCOME),
    TOP_UP(CategoryType.INCOME),

    FOOD(CategoryType.EXPENSE),
    TRAVEL(CategoryType.EXPENSE),
    RENT(CategoryType.EXPENSE),
    GROCERIES(CategoryType.EXPENSE),
    MEDICINE(CategoryType.EXPENSE),

    GLOBAL(CategoryType.EXPENSE);

    private final CategoryType type;

    SystemCategories(CategoryType type) {
        this.type = type;
    }

}
