package zakir.alekperov.application.locationplan;

import zakir.alekperov.domain.locationplan.LocationPlan;
import zakir.alekperov.domain.locationplan.LocationPlanRepository;
import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.shared.ValidationException;

import java.util.Optional;

/**
 * Сервис для удаления здания из ситуационного плана.
 */
public class DeleteBuildingService implements DeleteBuildingUseCase {
    
    private final LocationPlanRepository locationPlanRepository;
    
    public DeleteBuildingService(LocationPlanRepository locationPlanRepository) {
        if (locationPlanRepository == null) {
            throw new IllegalArgumentException("LocationPlanRepository не может быть null");
        }
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(DeleteBuildingCommand command) {
        // Валидация
        if (command == null) {
            throw new ValidationException("Команда не может быть null");
        }
        if (command.getPassportId() == null || command.getPassportId().isBlank()) {
            throw new ValidationException("ID паспорта не может быть пустым");
        }
        if (command.getLitera() == null || command.getLitera().isBlank()) {
            throw new ValidationException("Литера здания не может быть пустой");
        }
        
        // Загрузка плана
        PassportId passportId = PassportId.fromString(command.getPassportId());
        Optional<LocationPlan> planOptional = locationPlanRepository.findByPassportId(passportId);
        
        if (planOptional.isEmpty()) {
            throw new ValidationException("Ситуационный план не найден для паспорта: " + command.getPassportId());
        }
        
        LocationPlan plan = planOptional.get();
        
        // Проверка, что здание существует
        boolean exists = plan.getBuildingsCoordinates().stream()
            .anyMatch(b -> b.getLitera().equals(command.getLitera()));
        
        if (!exists) {
            throw new ValidationException("Здание с литерой " + command.getLitera() + " не найдено");
        }
        
        // Удаление здания
        plan.removeBuildingCoordinates(command.getLitera());
        
        // Сохранение
        locationPlanRepository.update(plan);
        
        System.out.println("✓ Здание с литерой " + command.getLitera() + " успешно удалено");
    }
}
