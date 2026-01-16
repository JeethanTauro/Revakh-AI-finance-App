package Revakh.api_gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    //these are the list of urls that are public and don't need a jwt token
    public static final List<String> whiteListUrls = List.of(
            "/api/auth/register",
            "/api/auth/register/verify",
            "/api/auth/login",
            "/api/auth/refresh-access",
            "/api/auth/reset-password/**",
            "/eureka"
    );

    // a predicate means the answer is true or false
    //the input is type of ServerHttpRequest
    // the output will be given to isSecured
    // request is basically the live request of the type ServerHttpRequest sent by the user
    public Predicate<ServerHttpRequest> isSecured=
            request-> whiteListUrls
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
                    //uri is basically the string objects from the stream
                    //if a match is found then noneMatch returns false
                    // so if the request is hit at /api/auth/login, it matches and false is returned , so its not secured

}
