package auth_service.revakh.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent {
    private Long userId;
    private String userEmail;
    private String userName;
    private String userInternationalCode;
    private Long userPhone;
    private LocalDateTime userCreatedAt;


    //idempotency of events
    private String eventType;
    private String source;
    private String eventId;
    private String eventVersion;
}
