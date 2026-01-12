package finance_service.revakh.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsumptionDTO {

    private Long budgetId;

    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private int percentageUsed;
    private BigDecimal overspent = BigDecimal.ZERO;

    private boolean isHalfReached;   // >= 50%
    private boolean isEightyReached; // >= 80%
    private boolean isFullReached;   // >= 100%

    private boolean budgetExceeded;
}
