package zakir.alekperov.domain.shared;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object для идентификатора технического паспорта.
 * Инвариант: ID не может быть null или пустым.
 */
public final class PassportId {
    private final String value;
    
    private PassportId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID паспорта не может быть пустым");
        }
        this.value = value.trim();
    }
    
    public static PassportId generate() {
        return new PassportId(UUID.randomUUID().toString());
    }
    
    public static PassportId fromString(String value) {
        return new PassportId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassportId that = (PassportId) o;
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
