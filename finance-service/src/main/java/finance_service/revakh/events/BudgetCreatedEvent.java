package finance_service.revakh.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BudgetCreatedEvent {
    private String eventId;
    private Long userId;
    private Long budgetId;
    private String category;      // "FOOD"
    private BigDecimal limitAmount; // 4000
    private String period;        // "MONTHLY", "DAILY"
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;       // true
    private LocalDateTime createdAt;
}
