package zakir.alekperov.application.locationplan.services;

import zakir.alekperov.application.locationplan.commands.UploadPlanImageCommand;
import zakir.alekperov.application.locationplan.usecases.UploadPlanImageUseCase;
import zakir.alekperov.domain.locationplan.LocationPlan;
import zakir.alekperov.domain.locationplan.LocationPlanRepository;
import zakir.alekperov.domain.locationplan.PlanImage;
import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.shared.ValidationException;

/**
 * Сервис для загрузки изображения ситуационного плана.
 * Реализация Use Case в Application слое.
 * 
 * Ответственность:
 * - Преобразование DTO в domain модели
 * - Вызов domain логики
 * - Вызов репозитория для персистентности
 * - Обработка исключений
 */
public final class UploadPlanImageService implements UploadPlanImageUseCase {
    
    private final LocationPlanRepository locationPlanRepository;
    
    /**
     * Создать сервис.
     * 
     * @param locationPlanRepository репозиторий для работы с планами
     */
    public UploadPlanImageService(LocationPlanRepository locationPlanRepository) {
        if (locationPlanRepository == null) {
            throw new IllegalArgumentException("locationPlanRepository не может быть null");
        }
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(UploadPlanImageCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }
        
        try {
            // Преобразование DTO в domain модели
            PassportId passportId = new PassportId(command.passportId());
            PlanImage planImage = new PlanImage(command.imageData(), command.fileName());
            
            // Создание domain модели
            LocationPlan locationPlan = LocationPlan.createWithUploadedImage(
                passportId,
                planImage,
                command.planDate(),
                command.notes()
            );
            
            // Сохранение через репозиторий
            locationPlanRepository.save(locationPlan);
            
        } catch (ValidationException e) {
            // Пробрасываем ValidationException как есть
            throw e;
            
        } catch (Exception e) {
            // Оборачиваем технические исключения
            throw new RuntimeException("Ошибка загрузки изображения: " + e.getMessage(), e);
        }
    }
}
