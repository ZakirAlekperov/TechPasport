package zakir.alekperov.domain.passport;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Доменная модель технического паспорта.
 * 
 * Immutable объект с встроенной валидацией.
 */
public final class Passport {
    
    private final String id;
    private final String organizationName;
    private final String inventoryNumber;
    private final String cadastralNumber;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    private Passport(Builder builder) {
        this.id = builder.id;
        this.organizationName = builder.organizationName;
        this.inventoryNumber = builder.inventoryNumber;
        this.cadastralNumber = builder.cadastralNumber;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        
        validate();
    }
    
    private void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Идентификатор паспорта не может быть пустым");
        }
        if (organizationName == null || organizationName.isBlank()) {
            throw new IllegalArgumentException("Название организации не может быть пустым");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Дата создания не может быть null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("Дата обновления не может быть null");
        }
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public String getOrganizationName() {
        return organizationName;
    }
    
    public String getInventoryNumber() {
        return inventoryNumber;
    }
    
    public String getCadastralNumber() {
        return cadastralNumber;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Builder
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String organizationName = "Не указано"; // Значение по умолчанию
        private String inventoryNumber;
        private String cadastralNumber;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder organizationName(String organizationName) {
            this.organizationName = organizationName;
            return this;
        }
        
        public Builder inventoryNumber(String inventoryNumber) {
            this.inventoryNumber = inventoryNumber;
            return this;
        }
        
        public Builder cadastralNumber(String cadastralNumber) {
            this.cadastralNumber = cadastralNumber;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Passport build() {
            return new Passport(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passport passport = (Passport) o;
        return Objects.equals(id, passport.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Passport{" +
                "id='" + id + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", inventoryNumber='" + inventoryNumber + '\'' +
                ", cadastralNumber='" + cadastralNumber + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
