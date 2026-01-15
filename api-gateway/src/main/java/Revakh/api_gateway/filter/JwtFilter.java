package Revakh.api_gateway.filter;

import Revakh.api_gateway.config.RouteValidator;
import Revakh.api_gateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.Ordered;

@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    public JwtFilter(RouteValidator validator, JwtUtil jwtUtil) {
        super(JwtFilter.Config.class);
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            System.out.println("GOT THE REQUEST");

            // 1. Check if route is secured
            if (validator.isSecured.test(request)) {

                // 2. Check for Authorization header (reactive compatible)
                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || authHeader.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authorization header");
                }

                if (!authHeader.startsWith("Bearer ")) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authorization format");
                }

                String token = authHeader.substring(7); // Extract token

                try {
                    // 3. Validate token (signature + expiry)
                    jwtUtil.validateToken(token);

                    // 4. Extract userId and add as header
                    String loggedInUser = jwtUtil.extractUserId(token);
                    System.out.println("GATEWAY SUCCESS: Found User ID [" + loggedInUser + "]. Injecting into header...");

                    // Mutate the request: ADD the new header, but the old ones stay by default
                    request = exchange.getRequest().mutate()
                            .header("userId", loggedInUser)
                            // The 'Authorization' header is already there; we don't need to delete it!
                            .build();

                    return chain.filter(exchange.mutate().request(request).build());

                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
                }
            }

            // 5. Continue the chain with the modified request
            return chain.filter(exchange.mutate().request(request).build());
        });
    }


    public static class Config {
        // Configuration properties can go here if needed
    }
}