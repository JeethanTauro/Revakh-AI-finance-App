package auth_service.revakh.controllers;

import auth_service.revakh.dto.AuthenticatedUserNewPasswordDTO;
import auth_service.revakh.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/update-password")
@Tag(name = "Password Update", description = "Endpoint for authenticated users to change their password")
public class PasswordUpdateController {
    private final UserService userService;

    @Operation(
            summary = "Change Password",
            description = "Allows a logged-in user to change their password by providing the old password for verification.",
            security = @SecurityRequirement(name = "Bearer Authentication") // Adds the Lock icon
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "Password updated successfully"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid old password or weak new password"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized (Invalid or expired JWT token)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PatchMapping
    public ResponseEntity<?> userUpdatePassword(@RequestBody AuthenticatedUserNewPasswordDTO authenticatedUserNewPasswordDTO){
        return  userService.updatePasswordAuthenticatedUser(authenticatedUserNewPasswordDTO.getOldUserPassword(), authenticatedUserNewPasswordDTO.getNewUserPassword());
    }
}
