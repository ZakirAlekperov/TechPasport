package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.ValidationException;
import java.util.Objects;

public final class PlanScale {
    private static final int[] ALLOWED_DENOMINATORS = {100, 200, 500, 1000, 2000, 5000};
    
    private final int denominator;
    
    private PlanScale(int denominator) {
        if (denominator <= 0) {
            throw new ValidationException("Знаменатель масштаба должен быть больше 0");
        }
        if (!isAllowedDenominator(denominator)) {
            throw new ValidationException(
                "Недопустимый масштаб. Допустимые: 1:100, 1:200, 1:500, 1:1000, 1:2000, 1:5000"
            );
        }
        this.denominator = denominator;
    }
    
    public static PlanScale fromDenominator(int denominator) {
        return new PlanScale(denominator);
    }
    
    public static PlanScale fromString(String scaleStr) {
        if (scaleStr == null || scaleStr.isBlank()) {
            throw new ValidationException("Строка масштаба не может быть пустой");
        }
        
        String cleaned = scaleStr.trim().replace("1:", "").replace(":", "");
        try {
            int denominator = Integer.parseInt(cleaned);
            return new PlanScale(denominator);
        } catch (NumberFormatException e) {
            throw new ValidationException("Неверный формат масштаба: " + scaleStr);
        }
    }
    
    private static boolean isAllowedDenominator(int denominator) {
        for (int allowed : ALLOWED_DENOMINATORS) {
            if (allowed == denominator) {
                return true;
            }
        }
        return false;
    }
    
    public int getDenominator() {
        return denominator;
    }
    
    public String format() {
        return "1:" + denominator;
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
        return format();
    }
}
