package finance_service.revakh.exceptions.CategoryExceptions;

public class CategoryExistsException extends RuntimeException{
    public CategoryExistsException(String message){
        super(message);
    }
}
