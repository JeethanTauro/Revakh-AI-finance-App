package auth_service.revakh.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "otp")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiry;

    @Column(nullable = false)
    private boolean isUsed;

    //this is for email update functionality
    @Column(nullable = true)
    private String pendingNewEmail;

}
