package auth_service.revakh.controllers;
import auth_service.revakh.Exceptions.UserAlreadyExistsException;
import auth_service.revakh.Exceptions.UserCannotBeDeletedException;
import auth_service.revakh.Exceptions.UserDeletionWasNotPerformed;
import auth_service.revakh.Exceptions.UserLoginException;
import auth_service.revakh.dto.*;

import auth_service.revakh.services.JwtService;
import auth_service.revakh.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> userRegister(@RequestBody UserRegisterDTO userRegisterDTO){
        try{
            return userService.userRegister(userRegisterDTO);
        }
        catch (UserAlreadyExistsException u){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(u.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending email");
        }
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
        try{
            return userService.userLogin(userLoginDTO);
        }catch (UserLoginException u){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(u.getMessage());
        }
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
        String refreshToken = refreshTokenRequestDTO.getRefresh();
        if(!jwtService.validateRefreshToken(refreshToken)){
            return ResponseEntity.badRequest().body("Login once again refresh token has been expired");
        }
        String newAccessToken  = jwtService.generateNewAccessTokenFromRefreshToken(refreshToken);
        AuthResponseDTO authResponseDTO = AuthResponseDTO.builder()
                        .accessToken(newAccessToken)
                                .refreshToken(refreshToken)
                                        .build();
        return ResponseEntity.ok(authResponseDTO);
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
    public ResponseEntity<?> deleteUser(){
        try{
            return userService.deleteUser();
        }
        catch (UserDeletionWasNotPerformed e) {
            // DB Failure -> Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
        catch (UserCannotBeDeletedException u){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( u.getMessage());
        }


    }
    //logout
    //have kept it because the logout basically invalidates the jwt token so we can just rwmove the token from the frontend cache

}
