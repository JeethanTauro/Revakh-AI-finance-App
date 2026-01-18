package finance_service.revakh.exceptions.TransactionExceptions;

public class OptimisticRetryFailedException extends RuntimeException {
    public OptimisticRetryFailedException(String message) {
        super(message);
    }
}