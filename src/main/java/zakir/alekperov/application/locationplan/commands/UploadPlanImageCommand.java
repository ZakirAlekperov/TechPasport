package zakir.alekperov.application.locationplan.commands;

import java.time.LocalDate;

/**
 * Команда для загрузки изображения ситуационного плана.
 * 
 * Immutable DTO для передачи данных от UI к Application слою.
 * 
 * Архитектурное решение:
 * - Record для неизменяемости
 * - Проверка null в компактном конструкторе
 * - Нет бизнес-логики (валидация в domain)
 */
public record UploadPlanImageCommand(
    String passportId,
    byte[] imageData,
    String fileName,
    LocalDate planDate,
    String notes
) {
    /**
     * Компактный конструктор record - проверка null для обязательных полей.
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
