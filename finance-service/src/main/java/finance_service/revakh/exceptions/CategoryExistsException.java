package finance_service.revakh.exceptions;

public class CategoryExistsException extends RuntimeException{
    public CategoryExistsException(String message){
        super(message);
    }
}
