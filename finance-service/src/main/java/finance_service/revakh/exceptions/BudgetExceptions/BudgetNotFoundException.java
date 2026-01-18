package finance_service.revakh.exceptions.BudgetExceptions;

public class BudgetNotFoundException extends RuntimeException{
    public BudgetNotFoundException(String message){
        super(message);
    }
}
