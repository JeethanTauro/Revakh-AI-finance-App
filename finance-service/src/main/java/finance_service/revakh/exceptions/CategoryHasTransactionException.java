package finance_service.revakh.exceptions;

public class CategoryHasTransactionException extends RuntimeException{
    public CategoryHasTransactionException(String message){
        super(message);
    }
}
