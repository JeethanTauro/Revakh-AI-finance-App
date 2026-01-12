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
public class FinanceUserDTO {

    @NotNull
    String userName;

    @NotNull
    String userEmail;

    @NotNull
    String userInternationalCode;

    @NotNull
    Long userPhoneNumber;



}
