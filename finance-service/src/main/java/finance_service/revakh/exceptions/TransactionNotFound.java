package finance_service.revakh.exceptions;

public class TransactionNotFound extends RuntimeException{
    public TransactionNotFound(String message){
        super(message);
    }
}
