package zakir.alekperov.application.locationplan;

import zakir.alekperov.domain.locationplan.LocationPlan;
import zakir.alekperov.domain.locationplan.LocationPlanRepository;
import zakir.alekperov.domain.locationplan.PlanScale;
import zakir.alekperov.domain.shared.PassportId;

import java.util.Optional;

public final class SaveLocationPlanService implements SaveLocationPlanUseCase {
    private final LocationPlanRepository locationPlanRepository;
    
    public SaveLocationPlanService(LocationPlanRepository locationPlanRepository) {
        this.locationPlanRepository = locationPlanRepository;
    }
    
    @Override
    public void execute(SaveLocationPlanCommand command) {
        PassportId passportId = PassportId.fromString(command.getPassportId());
        PlanScale scale = new PlanScale(Integer.parseInt(command.getScaleDenominator()));
        
        Optional<LocationPlan> existingPlanOpt = locationPlanRepository.findByPassportId(passportId);
        
        LocationPlan plan;
        if (existingPlanOpt.isPresent()) {
            plan = existingPlanOpt.get();
            if (plan.isManualDrawing()) {
                plan = LocationPlan.createManualDrawing(
                    passportId,
                    scale,
                    command.getExecutorName(),
                    command.getPlanDate(),
                    command.getNotes()
                );
                for (var building : existingPlanOpt.get().getBuildings()) {
                    plan.addBuilding(building);
                }
            }
        } else {
            plan = LocationPlan.createManualDrawing(
                passportId,
                scale,
                command.getExecutorName(),
                command.getPlanDate(),
                command.getNotes()
            );
        }
        
        locationPlanRepository.save(plan);
    }
}
