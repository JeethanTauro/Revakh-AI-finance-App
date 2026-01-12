package auth_service.revakh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterDTO {

    private String userName;

    private String userEmail;

    private String userInternationalCode;

    private long userNumber;

    private LocalDate userBirthDate;

    private String userPassword;

}
