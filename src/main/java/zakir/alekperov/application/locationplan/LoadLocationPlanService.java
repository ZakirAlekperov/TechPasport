package zakir.alekperov.application.locationplan;

import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.shared.ValidationException;
import zakir.alekperov.domain.locationplan.*;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LoadLocationPlanService implements LoadLocationPlanUseCase {
    private final LocationPlanRepository locationPlanRepository;
    
    public LoadLocationPlanService(LocationPlanRepository locationPlanRepository) {
        if (locationPlanRepository == null) {
            throw new IllegalArgumentException("LocationPlanRepository не может быть null");
        }
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public Optional<LocationPlanDTO> execute(LoadLocationPlanQuery query) {
        validateQuery(query);
        
        PassportId passportId = PassportId.fromString(query.getPassportId());
        
        return locationPlanRepository.findByPassportId(passportId)
            .map(this::mapToDTO);
    }
    
    private LocationPlanDTO mapToDTO(LocationPlan plan) {
        var buildingsDTO = plan.getBuildingsCoordinates().stream()
            .map(this::mapBuildingToDTO)
            .collect(Collectors.toList());
        
        return new LocationPlanDTO(
            plan.getPassportId().getValue(),
            plan.getScale().getDenominator(),
            plan.getExecutorName(),
            plan.getPlanDate(),
            plan.getNotes(),
            plan.getImagePath(),
            buildingsDTO
        );
    }
    
    private LocationPlanDTO.BuildingCoordinatesDTO mapBuildingToDTO(BuildingCoordinates building) {
        var pointsDTO = building.getPoints().stream()
            .map(point -> new LocationPlanDTO.CoordinatePointDTO(point.formatX(), point.formatY()))
            .collect(Collectors.toList());
        
        return new LocationPlanDTO.BuildingCoordinatesDTO(
            building.getLitera(),
            building.getDescription(),
            pointsDTO
        );
    }
    
    private void validateQuery(LoadLocationPlanQuery query) {
        if (query == null) {
            throw new ValidationException("Query не может быть null");
        }
        if (query.getPassportId() == null || query.getPassportId().isBlank()) {
            throw new ValidationException("ID паспорта обязателен");
        }
    }
}
