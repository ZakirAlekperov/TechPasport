package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BuildingCoordinates {
    private static final int MIN_POINTS = 3;
    
    private final String litera;
    private final String description;
    private final List<CoordinatePoint> points;
    
    private BuildingCoordinates(String litera, String description, List<CoordinatePoint> points) {
        if (litera == null || litera.isBlank()) {
            throw new ValidationException("Литера здания не может быть пустой");
        }
        if (points == null || points.size() < MIN_POINTS) {
            throw new ValidationException(
                "Здание должно иметь минимум " + MIN_POINTS + " координатные точки"
            );
        }
        
        this.litera = litera.trim();
        this.description = description != null ? description.trim() : "";
        this.points = new ArrayList<>(points);
    }
    
    public static BuildingCoordinates create(String litera, String description, List<CoordinatePoint> points) {
        return new BuildingCoordinates(litera, description, points);
    }
    
    public String getLitera() {
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
        return "Building{litera='" + litera + "', points=" + points.size() + "}";
    }
}
