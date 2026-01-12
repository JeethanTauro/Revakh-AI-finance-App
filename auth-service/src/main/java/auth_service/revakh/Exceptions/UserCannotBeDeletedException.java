package auth_service.revakh.Exceptions;

public class UserCannotBeDeletedException extends RuntimeException {
    public UserCannotBeDeletedException(String message) {
        super(message);
    }
}
