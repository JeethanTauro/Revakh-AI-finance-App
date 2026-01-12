package finance_service.revakh.exceptions;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String msg) {
        super(msg);
    }
}