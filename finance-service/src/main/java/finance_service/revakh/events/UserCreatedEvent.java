package finance_service.revakh.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private Long userId;
    private String userEmail;
    private String userName;
    private String userInternationalCode;
    private Long userPhone;
    private LocalDateTime userCreatedAt;

    private String eventId;
    private String eventType;
    private String eventVersion;
    private String source;
}
