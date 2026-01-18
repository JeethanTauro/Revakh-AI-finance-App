package finance_service.revakh.advice;

import finance_service.revakh.exceptions.BudgetExceptions.*;
import finance_service.revakh.exceptions.CategoryExceptions.*;
import finance_service.revakh.exceptions.FinanceUserExceptions.*;
import finance_service.revakh.exceptions.TransactionExceptions.*;
import finance_service.revakh.exceptions.WalletExceptions.*;
import finance_service.revakh.exceptions.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildResponse(String title, HttpStatus status, String message, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .title(title)
                .status(status.value())
                .details(message)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(error, status);
    }

    // --- USER EXCEPTIONS ---
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest req) {
        return buildResponse("User Not Found", HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException ex, WebRequest req) {
        return buildResponse("Identity Conflict", HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    // --- WALLET EXCEPTIONS ---
    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex, WebRequest req) {
        return buildResponse("Wallet Missing", HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(WalletValidationException.class)
    public ResponseEntity<ErrorResponse> handleWalletValidation(WalletValidationException ex, WebRequest req) {
        return buildResponse("Wallet Validation Issues", HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // --- TRANSACTION EXCEPTIONS ---
    @ExceptionHandler(TransactionNotFound.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFound(TransactionNotFound ex, WebRequest req) {
        return buildResponse("Transaction Missing", HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(TransactionValidationException.class)
    public ResponseEntity<ErrorResponse> handleTransactionValidation(TransactionValidationException ex, WebRequest req) {
        return buildResponse("Invalid Transaction", HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(OptimisticRetryFailedException.class)
    public ResponseEntity<ErrorResponse> handleConcurrency(OptimisticRetryFailedException ex, WebRequest req) {
        return buildResponse("System Busy", HttpStatus.CONFLICT, ex.getMessage(), req);
    }
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex,WebRequest req){
        return buildResponse("Insufficient Balance",HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    // --- BUDGET EXCEPTIONS ---
    @ExceptionHandler(BudgetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBudgetNotFound(BudgetNotFoundException ex, WebRequest req) {
        return buildResponse("Budget Missing", HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BudgetAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBudgetExists(BudgetAlreadyExistsException ex, WebRequest req) {
        return buildResponse("Budget Conflict", HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(BudgetValidationException.class)
    public ResponseEntity<ErrorResponse> handleBudgetValidation(BudgetValidationException ex, WebRequest req) {
        return buildResponse("Budget Validation Issues", HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }


    // --- CATEGORY EXCEPTIONS ---
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex, WebRequest req) {
        return buildResponse("Category Missing", HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(CategoryExistsException.class)
    public ResponseEntity<ErrorResponse> handleCategoryAlreadyExists(CategoryExistsException ex, WebRequest req) {
        return buildResponse("Category Conflict", HttpStatus.CONFLICT, ex.getMessage(), req);
    }


    // --- GLOBAL FALLBACK ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest req) {
        return buildResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected financial error occurred", req);
    }
}