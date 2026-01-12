package finance_service.revakh.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

//user in the finance db
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "finance_users", uniqueConstraints={
        @UniqueConstraint(columnNames = {"user_international_code","user_phone"})
})
@Builder
public class FinanceUser {

    //will get it from the user event
    @Id
    private Long userId;

    @Column(unique = true,nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String userInternationalCode;

    @Column(nullable = false)
    private Long userPhone;

    @Column(nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime userCreatedAt;


}
