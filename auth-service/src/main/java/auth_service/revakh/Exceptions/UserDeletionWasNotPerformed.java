package auth_service.revakh.Exceptions;

public class UserDeletionWasNotPerformed extends RuntimeException {
    public UserDeletionWasNotPerformed() {
        super("Server error couldn't delete");
    }
}
