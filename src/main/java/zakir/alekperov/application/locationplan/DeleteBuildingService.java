package zakir.alekperov.application.locationplan;

import zakir.alekperov.domain.locationplan.BuildingLitera;
import zakir.alekperov.domain.locationplan.LocationPlan;
import zakir.alekperov.domain.locationplan.LocationPlanRepository;
import zakir.alekperov.domain.shared.PassportId;

import java.util.Optional;

public final class DeleteBuildingService implements DeleteBuildingUseCase {
    private final LocationPlanRepository locationPlanRepository;
    
    public DeleteBuildingService(LocationPlanRepository locationPlanRepository) {
        if (locationPlanRepository == null) {
            throw new IllegalArgumentException("Репозиторий не может быть null");
        }
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(DeleteBuildingCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }
        
        PassportId passportId = PassportId.fromString(command.passportId());
        
        Optional<LocationPlan> planOptional = locationPlanRepository.findById(passportId);
        if (planOptional.isEmpty()) {
            throw new IllegalStateException("План не найден для passport_id: " + command.passportId());
        }
        
        LocationPlan plan = planOptional.get();
        
        BuildingLitera litera = new BuildingLitera(command.litera());
        boolean found = plan.getBuildings().stream()
            .anyMatch(b -> b.getLitera().equals(litera));
        
        if (!found) {
            throw new IllegalStateException(
                "Здание с литерой '" + command.litera() + "' не найдено в плане"
            );
        }
        
        plan.removeBuilding(litera);
        
        locationPlanRepository.save(plan);
    }
}
