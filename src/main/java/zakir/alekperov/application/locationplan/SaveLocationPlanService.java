package zakir.alekperov.application.locationplan;

import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.shared.ValidationException;
import zakir.alekperov.domain.locationplan.*;

public final class SaveLocationPlanService implements SaveLocationPlanUseCase {
    private final LocationPlanRepository locationPlanRepository;
    
    public SaveLocationPlanService(LocationPlanRepository locationPlanRepository) {
        if (locationPlanRepository == null) {
            throw new IllegalArgumentException("LocationPlanRepository не может быть null");
        }
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(SaveLocationPlanCommand command) {
        validateCommand(command);
        
        PassportId passportId = PassportId.fromString(command.getPassportId());
        PlanScale scale = PlanScale.fromString(command.getScaleDenominator());
        
        boolean planExists = locationPlanRepository.existsByPassportId(passportId);
        
        if (planExists) {
            LocationPlan existingPlan = locationPlanRepository.findByPassportId(passportId)
                .orElseThrow(() -> new IllegalStateException("План существует, но не найден"));
            
            existingPlan.updateScale(scale);
            existingPlan.updateExecutor(command.getExecutorName());
            existingPlan.updatePlanDate(command.getPlanDate());
            existingPlan.updateNotes(command.getNotes());
            existingPlan.setImagePath(command.getImagePath());
            
            locationPlanRepository.update(existingPlan);
        } else {
            LocationPlan newPlan = LocationPlan.create(passportId, scale, command.getExecutorName());
            newPlan.updatePlanDate(command.getPlanDate());
            newPlan.updateNotes(command.getNotes());
            newPlan.setImagePath(command.getImagePath());
            
            locationPlanRepository.save(newPlan);
        }
    }
    
    private void validateCommand(SaveLocationPlanCommand command) {
        if (command == null) {
            throw new ValidationException("Команда не может быть null");
        }
        if (command.getPassportId() == null || command.getPassportId().isBlank()) {
            throw new ValidationException("ID паспорта обязателен");
        }
        if (command.getScaleDenominator() == null || command.getScaleDenominator().isBlank()) {
            throw new ValidationException("Масштаб плана обязателен");
        }
    }
}
