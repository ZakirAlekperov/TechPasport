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
 * 
 * Архитектурное решение:
 * - Immutable Value Object
 * - Валидация допустимых значений
 * - Методы преобразования между масштабом и реальными размерами
 */
public final class PlanScale {
    
    private static final int[] ALLOWED_SCALES = {100, 200, 500, 1000, 2000, 5000};
    
    private final int denominator;
    
    /**
     * Создать масштаб плана.
     * 
     * @param denominator знаменатель масштаба (например, 500 для масштаба 1:500)
     * @throws ValidationException если масштаб невалиден
     */
    public PlanScale(int denominator) {
        this.denominator = validateDenominator(denominator);
    }
    
    /**
     * Создать масштаб из строки.
     * 
     * @param denominatorStr строковое значение знаменателя
     * @throws ValidationException если строка не может быть преобразована
     */
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
    
    /**
     * Получить текстовое представление масштаба.
     */
    public String toDisplayString() {
        return "1:" + denominator;
    }
    
    /**
     * Преобразовать расстояние на бумаге (в мм) в реальное расстояние (в метрах).
     */
    public double paperDistanceToRealMeters(double paperMillimeters) {
        return paperMillimeters * denominator / 1000.0;
    }
    
    /**
     * Преобразовать реальное расстояние (в метрах) в расстояние на бумаге (в мм).
     */
    public double realMetersToPaperDistance(double realMeters) {
        return realMeters * 1000.0 / denominator;
    }
    
    // === Валидация инвариантов ===
    
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
