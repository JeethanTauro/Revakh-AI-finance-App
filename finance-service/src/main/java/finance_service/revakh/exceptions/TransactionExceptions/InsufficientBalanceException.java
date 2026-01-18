package finance_service.revakh.exceptions.TransactionExceptions;


public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

