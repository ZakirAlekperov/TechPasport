package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.shared.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LocationPlan {
    private final PassportId passportId;
    private PlanScale scale;
    private final List<BuildingCoordinates> buildingsCoordinates;
    private String executorName;
    private LocalDate planDate;
    private String notes;
    private String imagePath;
    
    private LocationPlan(PassportId passportId, PlanScale scale, List<BuildingCoordinates> buildingsCoordinates,
                        String executorName, LocalDate planDate, String notes, String imagePath) {
        if (passportId == null) {
            throw new ValidationException("ID паспорта не может быть null");
        }
        if (scale == null) {
            throw new ValidationException("Масштаб плана не может быть null");
        }
        if (planDate == null) {
            planDate = LocalDate.now();
        }
        
        this.passportId = passportId;
        this.scale = scale;
        this.buildingsCoordinates = buildingsCoordinates != null ? new ArrayList<>(buildingsCoordinates) : new ArrayList<>();
        this.executorName = executorName != null ? executorName : "";
        this.planDate = planDate;
        this.notes = notes != null ? notes : "";
        this.imagePath = imagePath;
    }
    
    public static LocationPlan create(PassportId passportId, PlanScale scale, String executorName) {
        return new LocationPlan(passportId, scale, new ArrayList<>(), executorName, LocalDate.now(), "", null);
    }
    
    public static LocationPlan restore(PassportId passportId, PlanScale scale, List<BuildingCoordinates> buildings,
                                      String executorName, LocalDate planDate, String notes, String imagePath) {
        return new LocationPlan(passportId, scale, buildings, executorName, planDate, notes, imagePath);
    }
    
    public void updateScale(PlanScale newScale) {
        if (newScale == null) {
            throw new ValidationException("Масштаб не может быть null");
        }
        this.scale = newScale;
    }
    
    public void updateExecutor(String executorName) {
        this.executorName = executorName != null ? executorName : "";
    }
    
    public void updatePlanDate(LocalDate planDate) {
        if (planDate == null) {
            throw new ValidationException("Дата плана не может быть null");
        }
        this.planDate = planDate;
    }
    
    public void updateNotes(String notes) {
        this.notes = notes != null ? notes : "";
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public void addBuildingCoordinates(BuildingCoordinates coordinates) {
        if (coordinates == null) {
            throw new ValidationException("Координаты здания не могут быть null");
        }
        
        // Проверка на дубликат литеры
        for (BuildingCoordinates existing : buildingsCoordinates) {
            if (existing.getLitera().equals(coordinates.getLitera())) {
                throw new ValidationException("Здание с литерой " + coordinates.getLitera() + " уже существует");
            }
        }
        
        buildingsCoordinates.add(coordinates);
    }
    
    public void removeBuildingCoordinates(String litera) {
        buildingsCoordinates.removeIf(b -> b.getLitera().equals(litera));
    }
    
    // Getters
    
    public PassportId getPassportId() {
        return passportId;
    }
    
    public PlanScale getScale() {
        return scale;
    }
    
    public List<BuildingCoordinates> getBuildingsCoordinates() {
        return Collections.unmodifiableList(buildingsCoordinates);
    }
    
    public int getBuildingsCount() {
        return buildingsCoordinates.size();
    }
    
    public String getExecutorName() {
        return executorName;
    }
    
    public LocalDate getPlanDate() {
        return planDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationPlan that = (LocationPlan) o;
        return Objects.equals(passportId, that.passportId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(passportId);
    }
    
    @Override
    public String toString() {
        return "LocationPlan{passportId=" + passportId + ", scale=" + scale + ", buildings=" + buildingsCoordinates.size() + "}";
    }
}
