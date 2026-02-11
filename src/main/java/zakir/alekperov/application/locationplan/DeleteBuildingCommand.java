package zakir.alekperov.application.locationplan;

/**
 * Команда для удаления здания из ситуационного плана.
 */
public final class DeleteBuildingCommand {
    private final String passportId;
    private final String litera;
    
    public DeleteBuildingCommand(String passportId, String litera) {
        this.passportId = passportId;
        this.litera = litera;
    }
    
    public String getPassportId() {
        return passportId;
    }
    
    public String getLitera() {
        return litera;
    }
}
