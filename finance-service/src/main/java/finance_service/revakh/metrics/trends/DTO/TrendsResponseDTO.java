package finance_service.revakh.metrics.trends.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import finance_service.revakh.metrics.MetricName;
import finance_service.revakh.metrics.PeriodPreset;
import finance_service.revakh.models.Period;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendsResponseDTO {

    private Long userId;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private MetricName metricName;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Period periodType;   // DAILY / WEEKLY / MONTHLY / YEARLY

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private PeriodPreset preset;


    private LocalDate startDate;
    private LocalDate endDate;

    private String rangeDescription; // "Last 30 days", "This month", "Custom: 1 Jan – 31 Mar"

    private Integer totalPoints;     // number of data points returned for example 30 data points for last 30 days
    private Integer missingPoints;   // gaps in data if any, iof user spent on jan1 and on jan3 then gap is in jan2

    private String labelFormat;      // "dd/MM", "EEE", "MMM yyyy"

    private List<TimeSeriesPointDTO> timeSeriesPointDTOList;
}
