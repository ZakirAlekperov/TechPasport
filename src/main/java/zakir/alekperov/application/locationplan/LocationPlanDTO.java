package zakir.alekperov.application.locationplan;

import java.time.LocalDate;
import java.util.List;

public record LocationPlanDTO(
    String passportId,
    int scaleDenominator,
    String executorName,
    LocalDate planDate,
    String notes,
    String imagePath,
    List<BuildingCoordinatesDTO> buildings
) {
    public record BuildingCoordinatesDTO(
        String litera,
        String description,
        List<CoordinatePointDTO> points
    ) {}
    
    public record CoordinatePointDTO(
        String x,
        String y
    ) {}
}
