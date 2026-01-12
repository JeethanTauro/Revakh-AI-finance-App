package finance_service.revakh.metrics.category.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import finance_service.revakh.models.Period;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//how is this category performing, am i overspending in this category, this dto answers this qn
// categoiry level financial health

public class CategoryBreakdownDTO {
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalSpentInCategory;
    private int totalTransactionsInCategory;
    private BigDecimal percentageOfCategory; //percent = (totalSpentInCategory / totalSpentAcrossAllCategories) * 100
    //if the user has created a budget for that category (because he must have made only global budget)
    //if category-specific budget exists → show that.
    //If not → fall back to global budget calculations.
    private BigDecimal budgetLimit;
    private BigDecimal budgetConsumed;
    private BigDecimal budgetRemaining;

    //this can apply even for global budget
    private BigDecimal overspent = BigDecimal.ZERO;
    private boolean isHalfReached;   // >= 50%
    private boolean isEightyReached; // >= 80%
    private boolean isFullReached;   // >= 100%
    private boolean isOverspent; //spent>budget


    //make sure we select the period, eg. last 30 days, last month, this month etc
    private LocalDate periodStart;
    private LocalDate periodEnd;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Period periodType;

    private BigDecimal averageTransactionAmount; //averageTransactionAmount = totalSpentInCategory / totalTransactionsInCategory
    private BigDecimal trendPercentage; //Apps show “Food ↑12% compared to last month”.
    //trendPercentage = ((currentPeriodSpent - previousPeriodSpent) / previousPeriodSpent) * 100
}
