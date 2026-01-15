package Revakh.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    //validates the token
    public void validateToken(final String token) {
        Jwts.parserBuilder()    //creates a parser
                .setSigningKey(getSignKey()) //gives the parser the secret key to use it
                .build() //finalizes the parser with all the settings
                .parseClaimsJws(token); // checks if the jtw token is valid or not and then throws an exception
    }

    // does the same thing as validation but then after that extracts all the claims (basically its entire payload)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody(); // Standard for 0.12.x
    }

    //used to extract the userId from the token
    public String extractUserId(String token) {
        return extractClaim(token, claims -> String.valueOf(claims.get("userId")));
    }

    //extract all the claims first then get the required claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    //reads the key from the application properties and then converts it into bytes that the JWT library can understand and use
    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

/*
*
This is how a jwt claims look like
Claims {
  "sub": "john_doe",        // Subject (usually the username)
  "iat": 1516239022,        // Issued at (timestamp)
  "exp": 1516242622,        // Expiration (timestamp)
  "role": "admin"           // Custom claim
}
*
* */