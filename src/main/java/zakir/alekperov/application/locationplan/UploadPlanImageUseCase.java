package zakir.alekperov.application.locationplan;

/**
 * Use Case для загрузки изображения ситуационного плана.
 * 
 * Контракт:
 * - Принимает команду с данными изображения
 * - Валидирует изображение через domain модель
 * - Сохраняет через репозиторий
 * - Выбрасывает ValidationException при невалидных данных
 * - Выбрасывает RuntimeException при технических ошибках
 */
public interface UploadPlanImageUseCase {
    
    /**
     * Выполнить загрузку изображения плана.
     * 
     * @param command команда с данными изображения
     * @throws zakir.alekperov.domain.shared.ValidationException если изображение невалидно
     * @throws RuntimeException если произошла техническая ошибка
     */
    void execute(UploadPlanImageCommand command);
}
