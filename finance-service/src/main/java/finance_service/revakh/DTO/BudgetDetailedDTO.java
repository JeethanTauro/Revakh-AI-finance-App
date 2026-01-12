package finance_service.revakh.DTO;

import finance_service.revakh.models.Period;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BudgetDetailedDTO {

    private Long budgetId;

    // Category details
    private Long categoryId;
    private String categoryName;

    // Budget configuration details
    private BigDecimal limitAmount;
    private Period period;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private boolean isActive;

    // Consumption details
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private int percentageUsed;
    private BigDecimal overspent;
    private boolean isHalfReached;
    private boolean isEightyReached;
    private boolean isFullReached;

    private boolean budgetExceeded;
}
