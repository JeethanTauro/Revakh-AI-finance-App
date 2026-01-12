package finance_service.revakh.metrics.trends.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AveragesDTO {
    private BigDecimal avgDailyExpense;
    private BigDecimal avgWeeklyExpense;
    private BigDecimal avgMonthlyExpense;
    private BigDecimal avgYearlyExpense;

    private BigDecimal avgDailyIncome;
    private BigDecimal avgWeeklyIncome;
    private BigDecimal avgMonthlyIncome;
    private BigDecimal avgYearlyIncome;

    private BigDecimal avgDailySavings;
    private BigDecimal avgWeeklySavings;
    private BigDecimal avgMonthlySavings;
    private BigDecimal avgYearlySavings;

    private int numberOfDaysConsidered;
    private int numberOfWeeksConsidered;
    private int numberOfMonthsConsidered;
    private int numberOfYearsConsidered;

    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal totalSavings;
}
