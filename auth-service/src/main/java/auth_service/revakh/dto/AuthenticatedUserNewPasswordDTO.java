package auth_service.revakh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//if the user wants to update their password when they are already logged in
public class AuthenticatedUserNewPasswordDTO {
    private String oldUserPassword;
    private String newUserPassword;
}
