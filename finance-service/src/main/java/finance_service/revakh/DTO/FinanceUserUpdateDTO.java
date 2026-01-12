package finance_service.revakh.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceUserUpdateDTO {
    String userName;
    String userEmail;
    String userInternationalCode;
    Long userPhoneNumber;


}
