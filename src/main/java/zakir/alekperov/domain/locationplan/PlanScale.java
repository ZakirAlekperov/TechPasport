package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.ValidationException;

import java.util.Objects;

/**
 * Масштаб ситуационного плана.
 * Value Object - неизменяемый объект.
 * 
 * Инварианты:
 * - Знаменатель масштаба должен быть положительным числом
 * - Знаменатель должен быть одним из стандартных значений: 100, 200, 500, 1000, 2000, 5000
 */
public final class PlanScale {
    
    private static final int[] ALLOWED_SCALES = {100, 200, 500, 1000, 2000, 5000};
    
    private final int denominator;
    
    public PlanScale(int denominator) {
        this.denominator = validateDenominator(denominator);
    }
    
    public static PlanScale fromString(String denominatorStr) {
        if (denominatorStr == null || denominatorStr.isBlank()) {
            throw new ValidationException("Масштаб не может быть пустым");
        }
        
        try {
            int denominator = Integer.parseInt(denominatorStr.trim());
            return new PlanScale(denominator);
        } catch (NumberFormatException e) {
            throw new ValidationException(
                "Некорректный формат масштаба. Ожидается целое число, получено: '" + denominatorStr + "'"
            );
        }
    }
    
    public int getDenominator() {
        return denominator;
    }
    
    public String toDisplayString() {
        return "1:" + denominator;
    }
    
    public double paperDistanceToRealMeters(double paperMillimeters) {
        return paperMillimeters * denominator / 1000.0;
    }
    
    public double realMetersToPaperDistance(double realMeters) {
        return realMeters * 1000.0 / denominator;
    }
    
    private int validateDenominator(int denominator) {
        if (denominator <= 0) {
            throw new ValidationException(
                "Знаменатель масштаба должен быть положительным числом, получено: " + denominator
            );
        }
        
        boolean isAllowed = false;
        for (int allowed : ALLOWED_SCALES) {
            if (denominator == allowed) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new ValidationException(
                "Неподдерживаемый масштаб: 1:" + denominator + ". " +
                "Допустимые значения: 1:100, 1:200, 1:500, 1:1000, 1:2000, 1:5000"
            );
        }
        
        return denominator;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanScale planScale = (PlanScale) o;
        return denominator == planScale.denominator;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(denominator);
    }
    
    @Override
    public String toString() {
        return toDisplayString();
    }
}
