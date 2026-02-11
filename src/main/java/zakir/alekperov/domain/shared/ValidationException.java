package zakir.alekperov.domain.shared;

/**
 * Исключение валидации доменных правил.
 */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
