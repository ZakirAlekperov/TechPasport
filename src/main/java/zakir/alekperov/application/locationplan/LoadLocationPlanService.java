package zakir.alekperov.application.locationplan;

import zakir.alekperov.domain.locationplan.*;
import zakir.alekperov.domain.shared.PassportId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LoadLocationPlanService implements LoadLocationPlanUseCase {
    private final LocationPlanRepository locationPlanRepository;
    
    public LoadLocationPlanService(LocationPlanRepository locationPlanRepository) {
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public Optional<LocationPlanDTO> execute(LoadLocationPlanQuery query) {
        PassportId passportId = PassportId.fromString(query.passportId());
        
        Optional<LocationPlan> planOptional = locationPlanRepository.findById(passportId);
        
        return planOptional.map(plan -> {
            List<LocationPlanDTO.BuildingCoordinatesDTO> buildingDTOs = plan.getBuildings().stream()
                .map(this::toBuildingDTO)
                .collect(Collectors.toList());
            
            int scaleDenominator = plan.getScale()
                .map(PlanScale::denominator)
                .orElse(500);
            
            String executorName = plan.getExecutorName().orElse("");
            
            return new LocationPlanDTO(
                plan.getPassportId().getValue(),
                scaleDenominator,
                executorName,
                plan.getPlanDate(),
                plan.getNotes(),
                null,
                buildingDTOs
            );
        });
    }
    
    private LocationPlanDTO.BuildingCoordinatesDTO toBuildingDTO(BuildingCoordinates building) {
        List<LocationPlanDTO.CoordinatePointDTO> pointDTOs = building.getPoints().stream()
            .map(p -> new LocationPlanDTO.CoordinatePointDTO(
                String.valueOf(p.x()),
                String.valueOf(p.y())
            ))
            .collect(Collectors.toList());
        
        return new LocationPlanDTO.BuildingCoordinatesDTO(
            building.getLitera().value(),
            building.getDescription(),
            pointDTOs
        );
    }
}
