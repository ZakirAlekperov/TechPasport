package zakir.alekperov.application.locationplan;

import zakir.alekperov.domain.locationplan.*;
import zakir.alekperov.domain.shared.PassportId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AddBuildingCoordinatesService implements AddBuildingCoordinatesUseCase {
    private final LocationPlanRepository locationPlanRepository;
    
    public AddBuildingCoordinatesService(LocationPlanRepository locationPlanRepository) {
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(AddBuildingCoordinatesCommand command) {
        PassportId passportId = PassportId.fromString(command.passportId());
        
        Optional<LocationPlan> planOptional = locationPlanRepository.findById(passportId);
        if (planOptional.isEmpty()) {
            throw new IllegalStateException("План не найден для passpor_id: " + command.passportId());
        }
        
        LocationPlan plan = planOptional.get();
        
        List<CoordinatePoint> points = command.points().stream()
            .map(p -> new CoordinatePoint(
                Double.parseDouble(p.x()),
                Double.parseDouble(p.y())
            ))
            .collect(Collectors.toList());
        
        BuildingLitera litera = new BuildingLitera(command.litera());
        BuildingCoordinates buildingCoordinates = new BuildingCoordinates(
            litera,
            command.description(),
            points
        );
        
        plan.addBuilding(buildingCoordinates);
        
        locationPlanRepository.save(plan);
    }
}
