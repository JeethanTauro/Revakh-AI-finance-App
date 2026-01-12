package finance_service.revakh.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDeletedEvent {
    private Long userId;
    private String userEmail;
    private String userName;
    private LocalDateTime deletedAt;


    //idempotency of events
    private String eventType;
    private String source;
    private String eventId;
    private String eventVersion;
}
