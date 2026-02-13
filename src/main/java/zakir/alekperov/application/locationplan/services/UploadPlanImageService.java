package zakir.alekperov.application.locationplan.services;

import zakir.alekperov.application.locationplan.commands.UploadPlanImageCommand;
import zakir.alekperov.application.locationplan.usecases.UploadPlanImageUseCase;
import zakir.alekperov.domain.locationplan.LocationPlan;
import zakir.alekperov.domain.locationplan.LocationPlanRepository;
import zakir.alekperov.domain.locationplan.PlanImage;
import zakir.alekperov.domain.shared.PassportId;

import java.time.LocalDate;

public final class UploadPlanImageService implements UploadPlanImageUseCase {
    private final LocationPlanRepository locationPlanRepository;
    
    public UploadPlanImageService(LocationPlanRepository locationPlanRepository) {
        if (locationPlanRepository == null) {
            throw new IllegalArgumentException("Репозиторий не может быть null");
        }
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(UploadPlanImageCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }
        if (command.imageData() == null || command.imageData().length == 0) {
            throw new IllegalArgumentException("Данные изображения не могут быть пустыми");
        }
        if (command.fileName() == null || command.fileName().isBlank()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        
        PassportId passportId = PassportId.fromString(command.passportId());
        
        PlanImage planImage = new PlanImage(
            command.imageData(),
            command.fileName()
        );
        
        String notes = command.notes() != null ? command.notes() : "";
        
        LocationPlan plan = LocationPlan.createWithUploadedImage(
            passportId,
            planImage,
            command.planDate(),
            notes
        );
        
        locationPlanRepository.save(plan);
    }
}
