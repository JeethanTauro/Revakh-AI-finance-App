package auth_service.revakh.controllers;

import auth_service.revakh.dto.OtpDTO;
import auth_service.revakh.dto.ResetRequestDTO;
import auth_service.revakh.models.User;
import auth_service.revakh.repo.UserRepo;
import auth_service.revakh.services.JwtService;
import auth_service.revakh.services.OTPservice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/reset-password")
@Tag(name = "Password Reset", description = "Endpoints for OTP generation, validation, and password updates")
public class PasswordResetController {

    private final OTPservice otpService;
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    //generate otp
    @Operation(summary = "Step 1: Send OTP", description = "Generates a 6-digit OTP and sends it to the user's email address.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "OTP successfully sent to email"))),
            @ApiResponse(responseCode = "404", description = "User email not found"),
            @ApiResponse(responseCode = "500", description = "Error sending email")
    })
    @PostMapping("generate")
    public ResponseEntity<?> passwordReset(@RequestBody ResetRequestDTO resetRequestDTO) throws MessagingException, UnsupportedEncodingException {
        User user = userRepo.findByUserEmail(resetRequestDTO.getUserEmail()).orElseThrow(()->new RuntimeException("Wrong email"));
        String otp = otpService.otpGenerate(user.getUserEmail());
        otpService.sendOTPEmail(user.getUserEmail(), otp);
        return ResponseEntity.ok("OTP successfully sent to email");
    }


    //validate otp
    @Operation(summary = "Step 2: Validate OTP", description = "Verifies the OTP. If valid, returns a temporary Reset Token (JWT).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP Verified",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\": \"OTP verified...\", \"resetToken\": \"eyJhbGci...\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("validate")
    public ResponseEntity<?> passwordOTPvalidity(@RequestBody OtpDTO otpDTO){
        boolean isValid = otpService.isOtpValid(otpDTO.getUserEmail(), otpDTO.getOtp());

        if (!isValid) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid or expired OTP"));
        }

        String resetToken = otpService.generateResetJWT(otpDTO.getUserEmail());
        return ResponseEntity.ok(Map.of(
                "message", "OTP verified successfully. Use this token to reset your password.",
                "resetToken", resetToken
        ));
    }


    //confirm
    @Operation(
            summary = "Step 3: Set New Password",
            description = "Requires the 'resetToken' received from step 2 in the Authorization header.",
            security = @SecurityRequirement(name = "Bearer Authentication") // Adds the Lock icon
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token or empty password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("confirm")
    public  ResponseEntity<?> newPasswordConfirm(@Parameter(hidden = true) @RequestHeader("Authorization") String tokenHeader,

                                                 // We manually document the Map body so frontend devs know what key to send
                                                 @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                         description = "JSON object containing the new password",
                                                         content = @Content(
                                                                 mediaType = "application/json",
                                                                 schema = @Schema(example = "{\"newPassword\": \"MyNewStrongPass123!\"}")
                                                         )
                                                 )
                                                     @RequestBody Map<String, String> map){

        String token = tokenHeader.substring(7);
        if(jwtService.validateResetToken(token)){
            String userEmail = jwtService.extractEmail(token);
            String newPassword = map.get("newPassword");
            if (newPassword == null || newPassword.isBlank()) {
                return ResponseEntity.badRequest().body("New password cannot be empty");
            }
            User user = userRepo.findByUserEmail(userEmail).orElseThrow(()->new RuntimeException("User not found"));

            user.setUserPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);
            return ResponseEntity.ok(Map.of("message","password reset successful"));
        }
        return ResponseEntity.badRequest().body("Try again, token invalid");

    }


}
