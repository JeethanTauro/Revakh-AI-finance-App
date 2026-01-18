package finance_service.revakh.exceptions.TransactionExceptions;

public class TransactionNotFound extends RuntimeException{
    public TransactionNotFound(String message){
        super(message);
    }
}
