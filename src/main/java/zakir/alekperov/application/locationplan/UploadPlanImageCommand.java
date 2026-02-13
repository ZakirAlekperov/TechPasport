package zakir.alekperov.application.locationplan;

import java.time.LocalDate;

/**
 * Команда для загрузки изображения ситуационного плана.
 * Immutable DTO для передачи данных от UI к Application слою.
 */
public record UploadPlanImageCommand(
    String passportId,
    byte[] imageData,
    String fileName,
    LocalDate planDate,
    String notes
) {
    /**
     * Создать команду.
     * 
     * @param passportId ID технического паспорта
     * @param imageData данные изображения (байты)
     * @param fileName имя файла с расширением
     * @param planDate дата создания плана
     * @param notes примечания (может быть null)
     */
    public UploadPlanImageCommand {
        if (passportId == null) {
            throw new IllegalArgumentException("passportId не может быть null");
        }
        if (imageData == null) {
            throw new IllegalArgumentException("imageData не может быть null");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("fileName не может быть null");
        }
        if (planDate == null) {
            throw new IllegalArgumentException("planDate не может быть null");
        }
    }
}
