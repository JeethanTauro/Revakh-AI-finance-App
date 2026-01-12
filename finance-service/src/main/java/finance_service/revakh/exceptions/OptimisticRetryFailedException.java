package finance_service.revakh.exceptions;

public class OptimisticRetryFailedException extends RuntimeException {
    public OptimisticRetryFailedException(String message) {
        super(message);
    }
}