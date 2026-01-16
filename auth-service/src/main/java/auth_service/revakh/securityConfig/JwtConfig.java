package auth_service.revakh.securityConfig;

import auth_service.revakh.services.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        IpAddressMatcher hasIpAddress = new IpAddressMatcher("127.0.0.1");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. Public Endpoints (Gateway doesn't check these)
                        .requestMatchers("/api/auth/register", "/api/auth/register/verify",
                                "/api/auth/login", "/api/auth/reset-password/**","api/auth/refresh-access").permitAll()

                        // 2. Internal Endpoints (Gateway verified these, so we permit them here)
                        // We trust these because the Gateway injected the 'userId' header.
                        .requestMatchers("/api/auth/update-email/**", "/api/auth/update-password",
                                "/api/auth/user-delete").permitAll()

                        // 3. Swagger/Docs
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }
}
