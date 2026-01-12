package finance_service.revakh.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private String eventId;         // UUID (Unique ID for this message)
    private Long userId;            // Who spent it
    private Long transactionId;     // DB ID (for reference)
    private BigDecimal amount;
    private String type;            // "CREDIT" or "DEBIT"
    private String category;        // "FOOD", "TRAVEL"
    private String description;     // "Lunch at Subway" (Crucial for AI)
    private String currency;        // "INR"
    private LocalDateTime occurredAt; // When it happened
}
