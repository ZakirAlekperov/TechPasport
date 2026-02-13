package zakir.alekperov.application.locationplan.services;

import zakir.alekperov.application.locationplan.commands.UploadPlanImageCommand;
import zakir.alekperov.application.locationplan.usecases.UploadPlanImageUseCase;
import zakir.alekperov.domain.locationplan.LocationPlan;
import zakir.alekperov.domain.locationplan.LocationPlanRepository;
import zakir.alekperov.domain.locationplan.PlanImage;
import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.shared.ValidationException;

public final class UploadPlanImageService implements UploadPlanImageUseCase {
    
    private final LocationPlanRepository locationPlanRepository;
    
    public UploadPlanImageService(LocationPlanRepository locationPlanRepository) {
        if (locationPlanRepository == null) {
            throw new IllegalArgumentException("locationPlanRepository не может быть null");
        }
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(UploadPlanImageCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Команда не может быть null");
        }
        
        try {
            PassportId passportId = PassportId.fromString(command.passportId());
            PlanImage planImage = new PlanImage(command.imageData(), command.fileName());
            
            LocationPlan locationPlan = LocationPlan.createWithUploadedImage(
                passportId,
                planImage,
                command.planDate(),
                command.notes()
            );
            
            locationPlanRepository.save(locationPlan);
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки изображения: " + e.getMessage(), e);
        }
    }
}
