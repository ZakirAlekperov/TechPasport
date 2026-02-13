package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Координаты здания на ситуационном плане.
 * Entity с локальной идентичностью внутри агрегата LocationPlan.
 * 
 * Инварианты:
 * - Литера не может быть null
 * - Описание не может быть пустым
 * - Должно быть минимум 3 точки координат
 * - Координаты должны образовывать замкнутый контур
 */
public final class BuildingCoordinates {
    
    private final BuildingLitera litera;
    private final String description;
    private final List<CoordinatePoint> points;
    
    public BuildingCoordinates(
            BuildingLitera litera,
            String description,
            List<CoordinatePoint> points) {
        
        this.litera = validateLitera(litera);
        this.description = validateDescription(description);
        this.points = validatePoints(points);
    }
    
    public BuildingLitera getLitera() {
        return litera;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<CoordinatePoint> getPoints() {
        return Collections.unmodifiableList(points);
    }
    
    public int getPointsCount() {
        return points.size();
    }
    
    private BuildingLitera validateLitera(BuildingLitera litera) {
        if (litera == null) {
            throw new ValidationException("Литера здания не может быть null");
        }
        return litera;
    }
    
    private String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Описание здания не может быть пустым");
        }
        return description.trim();
    }
    
    private List<CoordinatePoint> validatePoints(List<CoordinatePoint> points) {
        if (points == null || points.isEmpty()) {
            throw new ValidationException("Список точек координат не может быть пустым");
        }
        
        if (points.size() < 3) {
            throw new ValidationException(
                "Для определения контура здания требуется минимум 3 точки, передано: " + points.size()
            );
        }
        
        for (CoordinatePoint point : points) {
            if (point == null) {
                throw new ValidationException("Точка координат не может быть null");
            }
        }
        
        return new ArrayList<>(points);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildingCoordinates that = (BuildingCoordinates) o;
        return Objects.equals(litera, that.litera);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(litera);
    }
    
    @Override
    public String toString() {
        return "BuildingCoordinates{" +
               "litera=" + litera +
               ", description='" + description + '\'' +
               ", pointsCount=" + points.size() +
               '}';
    }
}
