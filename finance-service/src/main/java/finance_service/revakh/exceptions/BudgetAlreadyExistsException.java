package finance_service.revakh.exceptions;

public class BudgetAlreadyExistsException extends RuntimeException{
    public BudgetAlreadyExistsException(String message){
        super(message);
    }
}
