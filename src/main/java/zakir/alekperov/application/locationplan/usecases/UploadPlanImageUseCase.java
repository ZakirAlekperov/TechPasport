package zakir.alekperov.application.locationplan.usecases;

import zakir.alekperov.application.locationplan.commands.UploadPlanImageCommand;

/**
 * Use Case для загрузки изображения ситуационного плана.
 * 
 * Контракт:
 * - Принимает команду с данными изображения
 * - Валидирует изображение через domain модель
 * - Сохраняет через репозиторий
 * - Выбрасывает ValidationException при невалидном изображении
 * - Выбрасывает RuntimeException при технических ошибках
 * 
 * Архитектурное решение:
 * - Интерфейс в Application слое
 * - Реализация в Service классе
 * - Одна ответственность (загрузка изображения)
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
