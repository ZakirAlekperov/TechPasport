package zakir.alekperov.application.locationplan;

import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.shared.ValidationException;
import zakir.alekperov.domain.locationplan.*;
import java.util.stream.Collectors;

public final class AddBuildingCoordinatesService implements AddBuildingCoordinatesUseCase {
    private final LocationPlanRepository locationPlanRepository;
    
    public AddBuildingCoordinatesService(LocationPlanRepository locationPlanRepository) {
        if (locationPlanRepository == null) {
            throw new IllegalArgumentException("LocationPlanRepository не может быть null");
        }
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(AddBuildingCoordinatesCommand command) {
        validateCommand(command);
        
        PassportId passportId = PassportId.fromString(command.getPassportId());
        
        LocationPlan plan = locationPlanRepository.findByPassportId(passportId)
            .orElseThrow(() -> new ValidationException(
                "Ситуационный план для паспорта " + passportId + " не найден"
            ));
        
        var points = command.getPoints().stream()
            .map(p -> CoordinatePoint.fromStrings(p.x(), p.y()))
            .collect(Collectors.toList());
        
        BuildingCoordinates coordinates = BuildingCoordinates.create(
            command.getLitera(),
            command.getDescription(),
            points
        );
        
        plan.addBuildingCoordinates(coordinates);
        locationPlanRepository.update(plan);
    }
    
    private void validateCommand(AddBuildingCoordinatesCommand command) {
        if (command == null) {
            throw new ValidationException("Команда не может быть null");
        }
        if (command.getPassportId() == null || command.getPassportId().isBlank()) {
            throw new ValidationException("ID паспорта обязателен");
        }
        if (command.getLitera() == null || command.getLitera().isBlank()) {
            throw new ValidationException("Литера здания обязательна");
        }
        if (command.getPoints() == null || command.getPoints().isEmpty()) {
            throw new ValidationException("Координаты здания обязательны");
        }
    }
}
