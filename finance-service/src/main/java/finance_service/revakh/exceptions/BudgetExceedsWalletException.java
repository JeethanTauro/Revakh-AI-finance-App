package finance_service.revakh.exceptions;

public class BudgetExceedsWalletException extends RuntimeException{
    public BudgetExceedsWalletException(String message){
        super(message);
    }
}
