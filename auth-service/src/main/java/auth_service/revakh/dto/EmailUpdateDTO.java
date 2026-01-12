package auth_service.revakh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailUpdateDTO {
    private String newEmail;
    private String currentPassword;
}
