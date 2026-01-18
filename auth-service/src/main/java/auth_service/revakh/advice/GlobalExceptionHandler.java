package auth_service.revakh.advice;


import auth_service.revakh.Exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ErrorResponse> handlerUserNotFound(UserNotFound userNotFound , WebRequest webRequest){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("User Not found")
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .details(userNotFound.getMessage())
                .path(webRequest.getDescription(false))
                .build();

        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlerUserAlreadyExists(UserAlreadyExistsException userAlreadyExistsException, WebRequest webRequest){
        ErrorResponse errorResponse =  ErrorResponse.builder()
                .title("Identity conflict")
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .path(webRequest.getDescription(false))
                .details(userAlreadyExistsException.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserLoginException.class)
    public ResponseEntity<ErrorResponse> handleUserLoginException(UserLoginException userLoginException, WebRequest webRequest){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Login failed")
                .details(userLoginException.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .path(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtpException(InvalidOtpException invalidOtpException, WebRequest webRequest){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Invalid otp")
                .details(invalidOtpException.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenException(RefreshTokenExpiredException refreshTokenExpiredException, WebRequest webRequest){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Session Expired")
                .details(refreshTokenExpiredException.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())//go back to login
                .timestamp(LocalDateTime.now())
                .path(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(GenericExeption.class)
    public ResponseEntity<ErrorResponse> handleGenericExceptions(GenericExeption genericExeption, WebRequest webRequest){
        ErrorResponse errorResponse = ErrorResponse.builder()
                .title("Server Error")
                .details(genericExeption.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }



}
