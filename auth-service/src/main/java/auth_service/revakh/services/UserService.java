package auth_service.revakh.services;

import auth_service.revakh.Exceptions.UserAlreadyExistsException;
import auth_service.revakh.Exceptions.UserCannotBeDeletedException;
import auth_service.revakh.Exceptions.UserLoginException;
import auth_service.revakh.Exceptions.UserNotFound;
import auth_service.revakh.dto.*;
import auth_service.revakh.events.UserCreatedEvent;
import auth_service.revakh.events.UserDeletedEvent;
import auth_service.revakh.messaging.UserEventPublisher;
import auth_service.revakh.models.User;
import auth_service.revakh.repo.UserRepo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    //1 user create => done
    //2 user delete => done
    //3 user update (update the password)
    //4 user login => login
    //5 get current authenticated user profile => ok so see when the user profile is requested we not only need to give the user details except the password but also the financial details the user has done, so the api gateway must do 2 calls
    /*
    Solution Approaches
1. API Gateway Aggregation (Recommended)
Architecture:
Frontend → API Gateway → (Auth + Finance)
Flow:
Frontend requests GET /user/profile.
API Gateway authenticates the request (validates JWT via Auth Service).
Then it makes two parallel internal API calls:
To Auth Service → /auth/me
To Finance Service → /finance/user/{id}
Combines both responses into a single JSON response and returns it to frontend.
Benefits:
Keeps microservices independent.
Only Gateway knows how to merge data.
Allows versioning or shaping of responses easily.
Example Gateway Aggregated Response:
{
  "id": 101,
  "name": "Jeethan",
  "email": "jeethan@revakh.com",
  "role": "USER",
  "finance": {
    "accountId": 501,
    "balance": 25000,
    "recentTransactions": [
      { "id": 1, "amount": -1000, "type": "DEBIT" },
      { "id": 2, "amount": 3000, "type": "CREDIT" }
    ]
  }
}*/
    //6 logout
    //7 forgot password integrstion by sening the email stuff like

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventPublisher userEventPublisher;
    private final OTPservice otpService;
    //registration

    //in real life when a user registers, he must register with an email and then password, the otp is generated and sent to the email and the email is verified and then only access token is generated
    //what i was previously doing was just give an email, just give a password and boom u get the access token this is wrong and must be avoided in the real world
    //also how its done is that, the user first enters the email and the password and then clicks register, the user information is saved but with a flag, isVerfied for checking if the email is verified or not
    //once verified using the otp, the email is verified, else its unverified and removed from the db
    @Transactional
    public ResponseEntity<?> userRegister(UserRegisterDTO userRegisterDTO) throws MessagingException, UnsupportedEncodingException {
            String userName = userRegisterDTO.getUserName();
            String userEmail = userRegisterDTO.getUserEmail();
            if(userRepo.findByUserEmail(userEmail).isPresent()){
                throw new UserAlreadyExistsException("User already exists");
            }
            String userPassword = userRegisterDTO.getUserPassword();
            String userInternationalCode = userRegisterDTO.getUserInternationalCode();
            LocalDate userBirthDate = userRegisterDTO.getUserBirthDate();
            long userNumber = userRegisterDTO.getUserNumber();

            User user = User.builder()
                    .userName(userName)
                    .userEmail(userEmail)
                    .userPassword(passwordEncoder.encode(userPassword))
                    .userInternationalCode(userInternationalCode)
                    .userNumber(userNumber)
                    .userBirthDate(userBirthDate)
                    .isVerified(false)
                    .build();
            //save it first
            User savedUser =userRepo.save(user);

            //generate the otp and send it to the email
            String otp = otpService.otpGenerate(userEmail);
            otpService.sendOTPEmail(userEmail,otp);


//            String jwtToken = jwtService.generateToken(userEmail);
//            String jwtRefreshToken = jwtService.generateRefreshToken(userEmail);
//            AuthResponseDTO authResponseDTO = AuthResponseDTO.builder()
//                    .accessToken(jwtToken)
//                    .refreshToken(jwtRefreshToken)
//                    .message("User created Successfully")
//                    .build();


            //initially we were publishing the user created event in register, but the user here isnt even verified yet through otp
            //this could creat issues, what if the clicked on the reigister button but otp is invalid
            //this means that this event will get published even if the user email validated
            //so we need to move the user created event to the verification part

            return ResponseEntity.status(HttpStatus.OK).body("Please check your mail for the otp");
    }
    // STEP 2: Verify (Check OTP & Activate)

    @Transactional
    public ResponseEntity<?> verifyRegistration(String email, String otp) {
        // 1. Validate OTP
        boolean isOtpValid = otpService.isOtpValid(email, otp);
        if (!isOtpValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        }

        // 2. Find User
        User user = userRepo.findByUserEmail(email)
                .orElseThrow(() -> new UserNotFound("User not found"));

        // 3. Activate User
        if (user.isVerified()) {
            return ResponseEntity.badRequest().body("User is already verified");
        }
        user.setVerified(true);
        User savedUser = userRepo.save(user);

        // 4. NOW Generate Tokens (The Reward)
        String jwtToken = jwtService.generateToken(email);
        String jwtRefreshToken = jwtService.generateRefreshToken(email);

        AuthResponseDTO authResponseDTO = AuthResponseDTO.builder()
                .accessToken(jwtToken)
                .refreshToken(jwtRefreshToken)
                .message("User Verified and Logged in Successfully")
                .userId(user.getUserId())
                .build();

        //after finally generating the access token and the user email is verified we must publish the user created event
        UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                .userId(savedUser.getUserId())
                .userEmail(savedUser.getUserEmail())
                .userName(savedUser.getUserName())
                .userInternationalCode(savedUser.getUserInternationalCode())
                .userPhone(savedUser.getUserNumber())
                .userCreatedAt(savedUser.getUserCreatedAt())
                .eventId(UUID.randomUUID().toString())
                .eventVersion("v1")
                .eventType("user.created")
                .source("auth-service")
                .build();

        userEventPublisher.userCreatedEventPublisher(userCreatedEvent);

        return ResponseEntity.ok(authResponseDTO);
    }


    //delete
    @Transactional
    public ResponseEntity<?> deleteUser(){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //get the current authenticated user
            String userEmail = authentication.getName(); //get the user-email

            User user = userRepo.findByUserEmail(userEmail).orElseThrow(()-> new UserCannotBeDeletedException("User could not be deleted")); //find the user by userEmail
            UserDeletedEvent userDeletedEvent = UserDeletedEvent.builder()
                    .userId(user.getUserId())
                    .userName(user.getUserName())
                    .userEmail(user.getUserEmail())
                    .deletedAt(LocalDateTime.now())
                    .eventId(UUID.randomUUID().toString())
                    .eventType("user.deleted")
                    .source("auth-service")
                    .eventVersion("v1").
                    build();
            try{
                //try to delete
                userRepo.delete(user);
                userRepo.flush();
            }catch (Exception e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User could not be deleted");
            }
            userEventPublisher.userDeletedEventPublisher(userDeletedEvent);
            return  ResponseEntity.ok("User deleted successfully");

    }


    //login
    //previously in login we were not even checkin if the email is verified
    //so during login we must check if the email is verified or not
    /*
    A user can register (creating an account with isVerified=false), ignore the OTP email, and
    immediately hit the /login endpoint.
    Your current code will give them a valid JWT, completely bypassing the email verification requirement
     */
    @Transactional
    public ResponseEntity<?> userLogin(UserLoginDTO userLoginDTO){
        String userEmail = userLoginDTO.getUserEmail();
        String userPassword =  userLoginDTO.getUserPassword();
        User user = userRepo.findByUserEmail(userEmail).orElseThrow(()->new UserLoginException("Wrong username or password"));
        //so before checking the password we must check if the user email is verified
        if (!user.isVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Account not verified. Please verify your email.");
        }
        if(!(passwordEncoder.matches(userPassword,user.getUserPassword()))){
            throw new UserLoginException("Wrong username or password");
        }
        String jwtToken = jwtService.generateToken(userEmail);
            String jwtRefreshToken = jwtService.generateRefreshToken(userEmail);
            AuthResponseDTO authResponseDTO = AuthResponseDTO.builder()
                    .accessToken(jwtToken)
                    .refreshToken(jwtRefreshToken)
                    .message("Successfully Logged in")
                    .build();
            return ResponseEntity.ok(authResponseDTO);
    }

    //update password
    public ResponseEntity<?> updatePasswordAuthenticatedUser(String oldPassword, String newPassword){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userRepo.findByUserEmail(userEmail).orElseThrow(()->new UserNotFound("User not found"));
        String userPassword = user.getUserPassword(); //get password from db
        if(passwordEncoder.matches(oldPassword,userPassword)){ //check id the password from db matches with the old password given by the user
            user.setUserPassword(passwordEncoder.encode(newPassword)); //set the new password
            userRepo.save(user);
            return ResponseEntity.ok("Password changed successfully");
        }
        return ResponseEntity.badRequest().body("Wrong Password");
    }

    //initiate the update by generating the otp
    @Transactional
    public ResponseEntity<?> initiateUpdate(EmailUpdateDTO emailUpdateDTO) throws MessagingException, UnsupportedEncodingException {
        //we can get the old email from the authentication container
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //get the current authenticated user
        String currentUserEmail = authentication.getName(); //get the user-email
        User user = userRepo.findByUserEmail(currentUserEmail).orElseThrow(()-> new UserNotFound("User not found"));

        //the user will enter the new email and the current password
        //verify first with the user password
        // Check if new email is already taken! (Important missing check)
        if(userRepo.existsByUserEmail(emailUpdateDTO.getNewEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        if(passwordEncoder.matches(emailUpdateDTO.getCurrentPassword(), user.getUserPassword())){
            // LOGIC FIX: Generate OTP against CURRENT user, but store the NEW email
            String otp = otpService.otpGenerateForEmailUpdate(currentUserEmail, emailUpdateDTO.getNewEmail());

            otpService.sendOTPEmail(emailUpdateDTO.getNewEmail(), otp);
            return ResponseEntity.ok("OTP sent");
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong password");
        }
    }

    //verify the new email with the otp and update it in the db
    @Transactional
    public ResponseEntity<?> verifyEmailUpdate(OtpDTO otpDTO){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //get the current authenticated user
        String currentUserEmail = authentication.getName(); //get the user-email

        //verify the otp and update the user email
        if(otpService.isOtpValid(currentUserEmail, otpDTO.getOtp())){

            // SECURE RETRIEVAL: Get the pending email from the service/cache
            String newEmail = otpService.getPendingEmail(currentUserEmail);

            User user = userRepo.findByUserEmail(currentUserEmail).orElseThrow();

            user.setUserEmail(newEmail); // We trust the cache, not the DTO
            userRepo.save(user);

            // Generate NEW tokens immediately for the NEW email
            String newToken = jwtService.generateToken(newEmail);
            String newRefreshToken = jwtService.generateRefreshToken(newEmail);

            AuthResponseDTO response = AuthResponseDTO.builder()
                    .accessToken(newToken)
                    .refreshToken(newRefreshToken)
                    .message("Email updated successfully")
                    .build();

            return ResponseEntity.ok(response);
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong otp");
        }
    }

    /*
    Split the process into 3 distinct endpoints:
        /reset-password/request → generate and send OTP //this will be in otpservicd
        /reset-password/verify → verify OTP, issue short JWT //
        /reset-password/confirm → use JWT to set new password
    */
//    public ResponseEntity<?> resetPassword(String userEmail){
//        //otp service
//        // otp generation
//        // otp sent to the email of the user requesting to reset the password
//
//
//        // if the otp is correct, generate a shot jwt token
//        // allow the user to reset the password
//        // send them back to the login page after resetting succesfully
//
//    }

    //the problem with the update email was that see
    //imagine the user updated the email to a new email
    // the new email is in the database but it is not verified
    // so the user needs to re register to verify the email , he loses all data
    // so after updating to new email, we must keep them logged in with new access and refresh token


    // for logging out we need to invalidate the tokens on the front end or the backend

}
