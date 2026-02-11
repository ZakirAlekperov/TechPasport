package zakir.alekperov.application.locationplan;

import java.util.List;

public final class AddBuildingCoordinatesCommand {
    private final String passportId;
    private final String litera;
    private final String description;
    private final List<CoordinatePointData> points;
    
    public AddBuildingCoordinatesCommand(String passportId, String litera, String description,
                                        List<CoordinatePointData> points) {
        this.passportId = passportId;
        this.litera = litera;
        this.description = description;
        this.points = points;
    }
    
    public String getPassportId() { return passportId; }
    public String getLitera() { return litera; }
    public String getDescription() { return description; }
    public List<CoordinatePointData> getPoints() { return points; }
    
    public static record CoordinatePointData(String x, String y) {}
}
