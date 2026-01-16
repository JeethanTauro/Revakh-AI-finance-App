package auth_service.revakh.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Data //adds getters and setters
@NoArgsConstructor
@AllArgsConstructor
@Entity //treats it as an entity
@Table(name = "users",uniqueConstraints={
        @UniqueConstraint(columnNames = {"user_international_code","user_phone"})
})//the table name is users
@Builder //helps us use the builder design whenever we want to create the user
@EntityListeners(AuditingEntityListener.class) // for updating date and time when user waa created and updated
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //generates the id for the user
    private Long userId;

    @Column(nullable = false,unique = true) // username cannot be null and has to be unique
    private String userName;

    @Column(nullable = false, unique = true) //email cannot be null and hast o be unique
    private String userEmail;

    @Column(nullable = false) //birth date to ensure user is above 16
    private LocalDate userBirthDate;

    @Column(nullable = false) //password has to be entered
    private String userPassword;

    @Column(nullable = false)
    private String userInternationalCode;

    @Column(nullable = false)
    private long userNumber;

    //this is for the email , if it is verified or not using the initial registration otp
    @Column(nullable = false)
    private boolean isVerified;


    // logs for when user was created and updated
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime userCreatedAt;
    @LastModifiedDate
    private LocalDateTime userUpdatedAt;

    // Calculate age dynamically
    public int getUserAge() {
        if (userBirthDate == null) return 0;
        return Period.between(userBirthDate, LocalDate.now()).getYears();
    }

}
