package auth_service.revakh.dto;


import lombok.Builder;

//short time jwt ot for otp
//otp is to allow the user to change the passwrd
//jwt is for the server to know , for whom to reset the password for cuz the jwt has basiclly the user name and stuff
@Builder
public class ShortTImeJWT {
    private String jwtToken;
}
