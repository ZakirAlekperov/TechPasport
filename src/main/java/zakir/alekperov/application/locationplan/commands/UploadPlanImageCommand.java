package zakir.alekperov.application.locationplan.commands;

import java.time.LocalDate;

/**
 * Команда для загрузки изображения ситуационного плана.
 * Immutable DTO для передачи данных от UI к Application слою.
 * 
 * Контракт:
 * - Все поля финальные (неизменяемые)
 * - Нет бизнес-логики
 * - Нет валидации (валидация в domain модели)
 * - Простая сериализация данных
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
     * @param passportId ID технического паспорта (строка)
     * @param imageData байты изображения
     * @param fileName имя файла изображения
     * @param planDate дата составления плана
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
