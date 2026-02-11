package zakir.alekperov.domain.shared;

/**
 * Базовое исключение для доменного слоя.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
