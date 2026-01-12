package finance_service.revakh.exceptions;

public class BudgetNotFoundException extends RuntimeException{
    public BudgetNotFoundException(String message){
        super(message);
    }
}
