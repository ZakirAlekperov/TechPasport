package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class CoordinatePoint {
    private static final int SCALE = 2;
    
    private final BigDecimal x;
    private final BigDecimal y;
    
    private CoordinatePoint(BigDecimal x, BigDecimal y) {
        if (x == null || y == null) {
            throw new ValidationException("Координаты не могут быть null");
        }
        this.x = x.setScale(SCALE, RoundingMode.HALF_UP);
        this.y = y.setScale(SCALE, RoundingMode.HALF_UP);
    }
    
    public static CoordinatePoint of(BigDecimal x, BigDecimal y) {
        return new CoordinatePoint(x, y);
    }
    
    public static CoordinatePoint fromStrings(String xStr, String yStr) {
        if (xStr == null || xStr.isBlank() || yStr == null || yStr.isBlank()) {
            throw new ValidationException("Координаты не могут быть пустыми");
        }
        
        try {
            BigDecimal x = new BigDecimal(xStr.trim().replace(',', '.'));
            BigDecimal y = new BigDecimal(yStr.trim().replace(',', '.'));
            return new CoordinatePoint(x, y);
        } catch (NumberFormatException e) {
            throw new ValidationException("Неверный формат координат: x=" + xStr + ", y=" + yStr);
        }
    }
    
    public BigDecimal getX() {
        return x;
    }
    
    public BigDecimal getY() {
        return y;
    }
    
    public String formatX() {
        return x.toPlainString();
    }
    
    public String formatY() {
        return y.toPlainString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordinatePoint that = (CoordinatePoint) o;
        return x.compareTo(that.x) == 0 && y.compareTo(that.y) == 0;
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
