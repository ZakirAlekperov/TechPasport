package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.shared.ValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Ситуационный план здания.
 * Поддерживает два режима: ручное рисование и загруженное изображение.
 * 
 * Инварианты:
 * - PassportId не может быть null
 * - PlanMode не может быть null
 * - В режиме MANUAL_DRAWING должен быть масштаб и исполнитель
 * - В режиме UPLOADED_IMAGE должно быть изображение
 * - Нельзя одновременно иметь изображение и координаты зданий
 */
public final class LocationPlan {
    
    private final PassportId passportId;
    private final PlanMode mode;
    private final PlanScale scale;
    private final String executorName;
    private final LocalDate planDate;
    private final String notes;
    private final List<BuildingCoordinates> buildings;
    private final PlanImage uploadedImage;
    
    /**
     * Создать ситуационный план в режиме ручного рисования.
     */
    public static LocationPlan createManualDrawing(
            PassportId passportId,
            PlanScale scale,
            String executorName,
            LocalDate planDate,
            String notes) {
        
        return new LocationPlan(
            passportId,
            PlanMode.MANUAL_DRAWING,
            scale,
            executorName,
            planDate,
            notes,
            new ArrayList<>(),
            null
        );
    }
    
    /**
     * Создать ситуационный план с загруженным изображением.
     */
    public static LocationPlan createWithUploadedImage(
            PassportId passportId,
            PlanImage uploadedImage,
            LocalDate planDate,
            String notes) {
        
        return new LocationPlan(
            passportId,
            PlanMode.UPLOADED_IMAGE,
            null,
            null,
            planDate,
            notes,
            new ArrayList<>(),
            uploadedImage
        );
    }
    
    /**
     * Восстановить план из хранилища.
     */
    public LocationPlan(
            PassportId passportId,
            PlanMode mode,
            PlanScale scale,
            String executorName,
            LocalDate planDate,
            String notes,
            List<BuildingCoordinates> buildings,
            PlanImage uploadedImage) {
        
        this.passportId = validatePassportId(passportId);
        this.mode = validateMode(mode);
        this.planDate = validatePlanDate(planDate);
        this.notes = notes != null ? notes : "";
        
        if (mode == PlanMode.MANUAL_DRAWING) {
            this.scale = validateScale(scale);
            this.executorName = validateExecutorName(executorName);
            this.buildings = new ArrayList<>(buildings != null ? buildings : new ArrayList<>());
            this.uploadedImage = null;
            
        } else { // UPLOADED_IMAGE
            this.scale = scale;
            this.executorName = executorName;
            this.buildings = new ArrayList<>();
            this.uploadedImage = validateUploadedImage(uploadedImage);
        }
        
        validateModeConsistency();
    }
    
    /**
     * Добавить координаты здания (только для режима MANUAL_DRAWING).
     */
    public void addBuilding(BuildingCoordinates buildingCoordinates) {
        if (mode != PlanMode.MANUAL_DRAWING) {
            throw new ValidationException(
                "Нельзя добавлять координаты зданий в режиме загруженного изображения"
            );
        }
        
        if (buildingCoordinates == null) {
            throw new ValidationException("Координаты здания не могут быть null");
        }
        
        if (hasBuildingWithLitera(buildingCoordinates.getLitera())) {
            throw new ValidationException(
                "Здание с литерой '" + buildingCoordinates.getLitera().getValue() + "' уже существует"
            );
        }
        
        this.buildings.add(buildingCoordinates);
    }
    
    /**
     * Удалить здание по литере.
     */
    public void removeBuilding(BuildingLitera litera) {
        if (mode != PlanMode.MANUAL_DRAWING) {
            throw new ValidationException(
                "Нельзя удалять координаты зданий в режиме загруженного изображения"
            );
        }
        
        if (litera == null) {
            throw new ValidationException("Литера не может быть null");
        }
        
        boolean removed = buildings.removeIf(b -> b.getLitera().equals(litera));
        
        if (!removed) {
            throw new ValidationException(
                "Здание с литерой '" + litera.getValue() + "' не найдено"
            );
        }
    }
    
    public boolean hasBuildingWithLitera(BuildingLitera litera) {
        if (litera == null || mode != PlanMode.MANUAL_DRAWING) {
            return false;
        }
        return buildings.stream()
            .anyMatch(b -> b.getLitera().equals(litera));
    }
    
    /**
     * Проверить, является ли план ручным рисованием.
     */
    public boolean isManualDrawing() {
        return mode == PlanMode.MANUAL_DRAWING;
    }
    
    /**
     * Проверить, является ли план загруженным изображением.
     */
    public boolean isUploadedImage() {
        return mode == PlanMode.UPLOADED_IMAGE;
    }
    
    public List<BuildingCoordinates> getBuildings() {
        return Collections.unmodifiableList(buildings);
    }
    
    public Optional<PlanImage> getUploadedImage() {
        return Optional.ofNullable(uploadedImage);
    }
    
    public PassportId getPassportId() {
        return passportId;
    }
    
    public PlanMode getMode() {
        return mode;
    }
    
    public Optional<PlanScale> getScale() {
        return Optional.ofNullable(scale);
    }
    
    public Optional<String> getExecutorName() {
        return Optional.ofNullable(executorName);
    }
    
    public LocalDate getPlanDate() {
        return planDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    // === Валидация инвариантов ===
    
    private PassportId validatePassportId(PassportId id) {
        if (id == null) {
            throw new ValidationException("ID паспорта не может быть null");
        }
        return id;
    }
    
    private PlanMode validateMode(PlanMode mode) {
        if (mode == null) {
            throw new ValidationException("Режим плана не может быть null");
        }
        return mode;
    }
    
    private PlanScale validateScale(PlanScale scale) {
        if (scale == null) {
            throw new ValidationException("Масштаб плана не может быть null для режима ручного рисования");
        }
        return scale;
    }
    
    private String validateExecutorName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("ФИО исполнителя не может быть пустым для режима ручного рисования");
        }
        return name.trim();
    }
    
    private LocalDate validatePlanDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("Дата составления плана не может быть null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата составления плана не может быть в будущем");
        }
        return date;
    }
    
    private PlanImage validateUploadedImage(PlanImage image) {
        if (image == null) {
            throw new ValidationException("Изображение плана не может быть null для режима загруженного изображения");
        }
        return image;
    }
    
    private void validateModeConsistency() {
        if (mode == PlanMode.MANUAL_DRAWING && uploadedImage != null) {
            throw new ValidationException(
                "В режиме ручного рисования не может быть загруженного изображения"
            );
        }
        
        if (mode == PlanMode.UPLOADED_IMAGE && !buildings.isEmpty()) {
            throw new ValidationException(
                "В режиме загруженного изображения не может быть координат зданий"
            );
        }
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
        return "LocationPlan{" +
               "passportId=" + passportId +
               ", mode=" + mode +
               ", buildingsCount=" + buildings.size() +
               ", hasImage=" + (uploadedImage != null) +
               '}';
    }
}
