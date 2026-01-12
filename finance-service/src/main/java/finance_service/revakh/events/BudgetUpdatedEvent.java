package finance_service.revakh.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BudgetUpdatedEvent {
    private String eventId;
    private Long userId;
    private Long budgetId;
    private String category;
    private BigDecimal newLimitAmount; // The updated limit
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private LocalDateTime updatedAt;
}
