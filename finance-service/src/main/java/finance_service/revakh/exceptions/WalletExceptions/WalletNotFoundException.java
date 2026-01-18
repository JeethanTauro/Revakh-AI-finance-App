package finance_service.revakh.exceptions.WalletExceptions;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String msg) {
        super(msg);
    }
}