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

//list of these points either daily or weekly or monthly or yearly will give us the line graph

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSeriesPointDTO {
    // Backend anchor for sorting/time logic
    private LocalDate date;

    // Human-readable label for charts
    private String timeLabel;

    // DAILY / WEEKLY / MONTHLY / YEARLY
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Period periodType;

    // Value for the chart (expense, income, or savings)
    private BigDecimal amount;

    // For multi-currency future support
    private String currency;
}
