package auth_service.revakh.services;


import auth_service.revakh.models.OTP;
import auth_service.revakh.repo.OtpRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor

public class OTPservice {

    private final OtpRepo otpRepo;
    private  final JavaMailSender mailSender;
    private  final PasswordEncoder passwordEncoder;
    private  final  JwtService jwtService;



//    public String otpGenerate(String userEmail){
//        otpRepo.deleteByUserEmail(userEmail); //first delete the user previousl generated otps
//        SecureRandom random = new SecureRandom();
//        int otpValue = 100000 + random.nextInt(900000);
//        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
//        OTP otp = OTP.builder()
//                .otp(passwordEncoder.encode(String.valueOf(otpValue)))
//                .userEmail(userEmail)
//                .expiry(expiry)
//                .isUsed(false)
//                .build();
//        otpRepo.save(otp);
//        return String.valueOf(otpValue);
//    }
    @Transactional
    public String otpGenerate(String userEmail){
        return generateInternal(userEmail, null);
    }

    //this is specifically for Email Updates
    @Transactional
    public String otpGenerateForEmailUpdate(String currentUserEmail, String newEmail){
        return generateInternal(currentUserEmail, newEmail);
    }


    public void sendOTPEmail(String userEmail, String otp)
            throws UnsupportedEncodingException, MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("revakh.support@example.com", "Revakh Support");
        helper.setTo(userEmail);

        String subject = "Your One-Time Password (OTP) – Expires in 5 Minutes";

        String content = "<p>Hello " + ",</p>"
                + "<p>For security reasons, please use the following "
                + "One Time Password (OTP) to verify your account:</p>"
                + "<h2><b>" + otp + "</b></h2>"
                + "<p>This OTP will expire in 5 minutes.</p>"
                + "<br>"
                + "<p>Thank you,<br>Revakh Security Team</p>";

        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }

    private String generateInternal(String userEmail, String pendingEmail) {
        otpRepo.deleteByUserEmail(userEmail); // Clear old OTPs

        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        OTP otp = OTP.builder()
                .otp(passwordEncoder.encode(String.valueOf(otpValue)))
                .userEmail(userEmail)       // Tied to CURRENT user
                .pendingNewEmail(pendingEmail) // Storing the NEW email safely
                .expiry(expiry)
                .isUsed(false)
                .build();

        otpRepo.save(otp);
        return String.valueOf(otpValue);
    }

    // 3. UPDATED Validation Logic
    public boolean isOtpValid(String userEmail, String otpFromUser) {
        OTP otp = otpRepo.findByUserEmail(userEmail);
        System.out.println("Validating for: " + userEmail);
        System.out.println("OTP from User: [" + otpFromUser + "]");
        if (otp != null) {
            System.out.println("Stored Encoded OTP: " + otp.getOtp());
            System.out.println("Is Expired? " + otp.getExpiry().isBefore(LocalDateTime.now()));
        } else {
            System.out.println("No OTP found in DB for this email!");
        }
        if (otp == null) return false;
        if (otp.isUsed()) return false;
        if (otp.getExpiry().isBefore(LocalDateTime.now())) return false;

        if (passwordEncoder.matches(otpFromUser, otp.getOtp())) {
            otp.setUsed(true);
            otpRepo.save(otp);
            return true;
        }
        return false;
    }

    // 4. NEW METHOD: Retrieve the pending email securely
    public String getPendingEmail(String userEmail) {
        OTP otp = otpRepo.findByUserEmail(userEmail);
        // Safety check: ensure an OTP actually exists
        if (otp != null) {
            return otp.getPendingNewEmail();
        }
        return null;
    }

    public String generateResetJWT(String userEmail){
       return jwtService.generateResetToken(userEmail);
    }
}
