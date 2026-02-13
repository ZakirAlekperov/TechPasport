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
        PassportId passportId = PassportId.fromString(command.passportId());
        PlanScale scale = new PlanScale(Integer.parseInt(command.scaleDenominator()));
        
        Optional<LocationPlan> existingPlanOpt = locationPlanRepository.findById(passportId);
        
        LocationPlan plan;
        if (existingPlanOpt.isPresent()) {
            plan = existingPlanOpt.get();
            // Для существующего плана нужно пересоздать с новыми параметрами
            // так как LocationPlan - immutable aggregate
            if (plan.isManualDrawing()) {
                plan = LocationPlan.createManualDrawing(
                    passportId,
                    scale,
                    command.executorName(),
                    command.planDate(),
                    command.notes()
                );
                // Восстановим здания
                for (var building : existingPlanOpt.get().getBuildings()) {
                    plan.addBuilding(building);
                }
            }
            // Для UPLOADED_IMAGE оставляем как есть
        } else {
            plan = LocationPlan.createManualDrawing(
                passportId,
                scale,
                command.executorName(),
                command.planDate(),
                command.notes()
            );
        }
        
        locationPlanRepository.save(plan);
    }
}
