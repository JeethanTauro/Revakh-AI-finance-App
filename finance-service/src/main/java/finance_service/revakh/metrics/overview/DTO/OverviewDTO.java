package finance_service.revakh.metrics.overview.DTO;

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
// This dto is for mainly answering the question "How am i doing financially" this dto gives the overall metrics that a user needs to see

public class OverviewDTO {
    private BigDecimal totalIncome; //total income in the selected period
    private BigDecimal totalExpense; //total expense in the selected period
    private BigDecimal totalSavings; //total savings in the selected period
    private BigDecimal walletBalance; //just wallet balance
    private int totalTransactions; //total transactions in the period
    private BigDecimal largestIncome; //largest credit to the account in the selected period
    private BigDecimal largestExpense; //largest debit from the account in the selected period
    private BigDecimal averageExpensePerPeriod;
    private BigDecimal averageIncomePerPeriod;
    private BigDecimal savingsPercentage; //savings have been done in percentage

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String averageType; // Values: "DAILY", "MONTHLY"
}
