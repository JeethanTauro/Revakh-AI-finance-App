package auth_service.revakh.Exceptions;

public class GenericExeption extends RuntimeException {
    public GenericExeption() {
        super("Error from our side, try again");
    }
}
