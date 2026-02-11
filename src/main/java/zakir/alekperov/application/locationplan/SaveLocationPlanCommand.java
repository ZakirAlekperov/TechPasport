package zakir.alekperov.application.locationplan;

import java.time.LocalDate;

public final class SaveLocationPlanCommand {
    private final String passportId;
    private final String scaleDenominator;
    private final String executorName;
    private final LocalDate planDate;
    private final String notes;
    private final String imagePath;
    
    public SaveLocationPlanCommand(String passportId, String scaleDenominator, String executorName,
                                  LocalDate planDate, String notes, String imagePath) {
        this.passportId = passportId;
        this.scaleDenominator = scaleDenominator;
        this.executorName = executorName;
        this.planDate = planDate;
        this.notes = notes;
        this.imagePath = imagePath;
    }
    
    public String getPassportId() { return passportId; }
    public String getScaleDenominator() { return scaleDenominator; }
    public String getExecutorName() { return executorName; }
    public LocalDate getPlanDate() { return planDate; }
    public String getNotes() { return notes; }
    public String getImagePath() { return imagePath; }
}
