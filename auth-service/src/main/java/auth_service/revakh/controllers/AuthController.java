package auth_service.revakh.controllers;
import auth_service.revakh.dto.*;

import auth_service.revakh.services.JwtService;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtService jwtService;
    private final UserService userService;

    //registration
    @Operation(description = "register a new user's account", summary = "Step 1: enter your email and password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "OTP sent successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                            )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - email already exists"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"

            )

    })
    @PostMapping("register")
    public ResponseEntity<?> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) throws MessagingException, UnsupportedEncodingException {
        return userService.userRegister(userRegisterDTO);
    }

    //--------------------------------------------------------------------------------------------

    //verify
    @Operation(description = "used to verify the user's email",summary = "Step 2: enter the otp to verify")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid otp"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"

            )

    })
    @PostMapping("register/verify")
    public ResponseEntity<?> userVerify(@RequestBody OtpDTO otpDTO){
        return userService.verifyRegistration(otpDTO.getUserEmail() , otpDTO.getOtp());
    }

    //-----------------------------------------------------------------------------------------------

    //login
    @Operation(description = "login of a user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User logged in successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid password or username"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"

            )

    })
    @PostMapping("login")
    public ResponseEntity<?> userLogin(@RequestBody UserLoginDTO userLoginDTO){
        return userService.userLogin(userLoginDTO);
    }

    //----------------------------------------------------------------------------------------------------------

    //when the front end gets a 401 unautherised, it realises the access token has been expired, so now from the front end where the refresh token is stored in http only cookie, it sends the refresh token as a body
    @Operation(description = "endpoint for refresh tokens when the jwt token expires")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "New access token generated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid refresh token, login once again"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"

            )

    })
    @PostMapping("/refresh-access")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
       return userService.refreshToken(refreshTokenRequestDTO);
    }


    //--------------------------------------------------------------------------------------------------------------------

    //delete user
    @Operation(description = "deletes a user's account")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User Deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"

            )

    })
    @DeleteMapping("/user-delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("userId") Long userId){
         return userService.deleteUser(userId);
    }
    //logout
    //have kept it because the logout basically invalidates the jwt token so we can just rwmove the token from the frontend cache

}
