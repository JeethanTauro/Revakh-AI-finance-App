package finance_service.revakh.metrics.trends.DTO;

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
public class CashFlowPointDTO {

    // Raw temporal anchor for proper backend sorting & filtering
    private LocalDate date;

    // Label for frontend chart display (e.g., "Mon", "Feb", "Week 12", "2024")
    private String timeLabel;

    // DAILY / WEEKLY / MONTHLY / YEARLY
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Period periodType;

    private BigDecimal incomeAmount;
    private BigDecimal expenseAmount;
    private BigDecimal netCashFlow;
}
