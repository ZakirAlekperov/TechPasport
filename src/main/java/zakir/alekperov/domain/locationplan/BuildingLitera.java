package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.ValidationException;

import java.util.Objects;

/**
 * Литера здания (обозначение на плане).
 * Value Object - неизменяемый объект.
 * 
 * Инварианты:
 * - Литера не может быть пустой
 * - Литера может содержать только буквы латиницы или кириллицы и цифры
 * - Максимальная длина 10 символов
 */
public final class BuildingLitera {
    
    private static final int MAX_LENGTH = 10;
    private static final String ALLOWED_PATTERN = "^[A-Za-zА-Яа-яЁё0-9]+$";
    
    private final String value;
    
    public BuildingLitera(String value) {
        this.value = validateValue(value);
    }
    
    public String getValue() {
        return value;
    }
    
    private String validateValue(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Литера здания не может быть пустой");
        }
        
        String trimmed = value.trim();
        
        if (trimmed.length() > MAX_LENGTH) {
            throw new ValidationException(
                "Литера здания не может быть длиннее " + MAX_LENGTH + " символов, получено: " + trimmed.length()
            );
        }
        
        if (!trimmed.matches(ALLOWED_PATTERN)) {
            throw new ValidationException(
                "Литера здания может содержать только буквы и цифры, получено: '" + trimmed + "'"
            );
        }
        
        return trimmed;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildingLitera that = (BuildingLitera) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
