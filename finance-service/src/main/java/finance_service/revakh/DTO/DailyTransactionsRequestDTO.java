package finance_service.revakh.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTransactionsRequestDTO {
    private Long userId;
    private LocalDate targetDate;
}
