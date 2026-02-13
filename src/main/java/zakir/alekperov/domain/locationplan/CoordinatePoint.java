package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.ValidationException;

import java.util.Objects;

/**
 * Точка координат в системе МСК-67.
 * Value Object - неизменяемый объект без идентичности.
 * 
 * Инварианты:
 * - Координаты X и Y не могут быть null
 * - Координаты должны быть валидными числами
 */
public final class CoordinatePoint {
    
    private final double x;
    private final double y;
    
    public CoordinatePoint(double x, double y) {
        this.x = validateCoordinate(x, "X");
        this.y = validateCoordinate(y, "Y");
    }
    
    public static CoordinatePoint fromStrings(String xStr, String yStr) {
        if (xStr == null || xStr.isBlank()) {
            throw new ValidationException("Координата X не может быть пустой");
        }
        if (yStr == null || yStr.isBlank()) {
            throw new ValidationException("Координата Y не может быть пустой");
        }
        
        try {
            double x = Double.parseDouble(xStr.trim());
            double y = Double.parseDouble(yStr.trim());
            return new CoordinatePoint(x, y);
        } catch (NumberFormatException e) {
            throw new ValidationException(
                "Некорректный формат координат. Ожидаются числа, получено: X='" + xStr + "', Y='" + yStr + "'"
            );
        }
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double distanceTo(CoordinatePoint other) {
        if (other == null) {
            throw new IllegalArgumentException("Точка не может быть null");
        }
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public String formatX() {
        return String.format("%.2f", x);
    }
    
    public String formatY() {
        return String.format("%.2f", y);
    }
    
    private double validateCoordinate(double value, String name) {
        if (Double.isNaN(value)) {
            throw new ValidationException("Координата " + name + " не может быть NaN");
        }
        if (Double.isInfinite(value)) {
            throw new ValidationException("Координата " + name + " не может быть бесконечной");
        }
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordinatePoint that = (CoordinatePoint) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    @Override
    public String toString() {
        return "(" + formatX() + ", " + formatY() + ")";
    }
}
