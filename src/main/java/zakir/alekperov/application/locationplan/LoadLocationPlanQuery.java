package zakir.alekperov.application.locationplan;

public final class LoadLocationPlanQuery {
    private final String passportId;
    
    public LoadLocationPlanQuery(String passportId) {
        this.passportId = passportId;
    }
    
    public String getPassportId() {
        return passportId;
    }
}
