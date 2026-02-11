package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.PassportId;
import java.util.Optional;

public interface LocationPlanRepository {
    void save(LocationPlan plan);
    Optional<LocationPlan> findByPassportId(PassportId passportId);
    void update(LocationPlan plan);
    boolean existsByPassportId(PassportId passportId);
    void delete(PassportId passportId);
}
