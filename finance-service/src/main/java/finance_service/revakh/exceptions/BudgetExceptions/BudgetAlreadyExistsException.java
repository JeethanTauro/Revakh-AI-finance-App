package finance_service.revakh.exceptions.BudgetExceptions;

public class BudgetAlreadyExistsException extends RuntimeException{
    public BudgetAlreadyExistsException(String message){
        super(message);
    }
}
