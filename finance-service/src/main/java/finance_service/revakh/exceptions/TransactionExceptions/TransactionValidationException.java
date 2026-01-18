package finance_service.revakh.exceptions.TransactionExceptions;

public class TransactionValidationException extends RuntimeException{
    public TransactionValidationException(String message){
        super(message);
    }
}
