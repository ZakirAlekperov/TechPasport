package zakir.alekperov.application.locationplan;

import java.util.Optional;

public interface LoadLocationPlanUseCase {
    Optional<LocationPlanDTO> execute(LoadLocationPlanQuery query);
}
