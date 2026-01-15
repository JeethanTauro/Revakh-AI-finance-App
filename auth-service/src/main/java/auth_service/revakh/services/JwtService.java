package auth_service.revakh.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final AuthenticationManager authenticationManager;

    /** Authenticate user (for login use only) */
    public boolean authenticateUser(String userName, String userPassword) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userName, userPassword)
        );
        return authentication.isAuthenticated();
    }

    /** Generate access token */
    public String generateToken(String userEmail, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",userId);
        return createToken(claims, userEmail, expiration);
    }

    /** Generate refresh token */
    public String generateRefreshToken(String userEmail) {
        long refreshExpiration = 7 * 24 * 60 * 60 * 1000; // 7 days
        return createToken(new HashMap<>(), userEmail, refreshExpiration);
    }

    /** Common token creator */
    private String createToken(Map<String, Object> claims, String subject, long expiryMillis) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiryMillis))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Extract username/email from token */
    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extract any claim */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Extract all claims */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Check token expiry */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /** Validate access token */
    public boolean validateToken(String token, UserDetails userDetails) {
        String userEmail = extractUserEmail(token);
        return userEmail.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /** Generate short-lived password reset token */
    public String generateResetToken(String userEmail) {
        long now = System.currentTimeMillis();
        long expiry = now + (5 * 60 * 1000); // 5 minutes

        return Jwts.builder()
                .setSubject(userEmail)
                .claim("purpose", "password_reset")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiry))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Validate reset token purpose and expiry */
    public boolean validateResetToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (!"password_reset".equals(claims.get("purpose"))) return false;
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /** Extract email from any token */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Get signing key from secret */
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /** Validate the refresh token*/
    public boolean validateRefreshToken(String token){
        try{
            Claims claims = extractAllClaims(token); //parsing the oken
            if(claims.getSubject() == null) return false;
            //expired refresh token
            return !claims.getExpiration().before(new Date());

        }
        catch (Exception e){
            return false;
        }
    }
    /** Generate new access token using a valid refresh token */
    public String generateNewAccessTokenFromRefreshToken(String refreshToken) {
        String userEmail = extractUserEmail(refreshToken);
        Long userId = extractClaim(refreshToken, claims -> claims.get("userId", Long.class));
        return generateToken(userEmail,userId); // access token (30 min)
    }
}
