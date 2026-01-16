package auth_service.revakh.controllers;

import auth_service.revakh.dto.AuthResponseDTO;
import auth_service.revakh.dto.EmailUpdateDTO;
import auth_service.revakh.dto.OtpDTO;
import auth_service.revakh.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

//updating email
@RestController
@RequestMapping("api/auth/update-email")
@RequiredArgsConstructor
public class EmailUpdateController {
    private final UserService userService;

    @Operation(description = "to initiate the email update process", summary = "Step 1: generate the otp to the new email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "OTP successfully sent to email"))),
            @ApiResponse(responseCode = "404", description = "User email not found"),
            @ApiResponse(responseCode = "500", description = "Error sending email")
    })
    @PostMapping("initiate")
    public ResponseEntity<?> initiateUpdateEmail(@RequestHeader("userId") Long userId, @RequestBody EmailUpdateDTO emailUpdateDTO) throws MessagingException, UnsupportedEncodingException {
        return userService.initiateUpdate(userId,emailUpdateDTO);
    }

    @Operation(description = "used to verify the new email", summary = "Step 2: verify the new email using valid otp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User email not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PostMapping("verify")
    public ResponseEntity<?> verifyNewEmail(@RequestHeader("userId") Long userId,@RequestBody OtpDTO otpDTO){
        return userService.verifyEmailUpdate(userId,otpDTO);
    }

}
