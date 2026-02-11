package zakir.alekperov.application.locationplan;

/**
 * Use case для удаления здания из ситуационного плана.
 */
public interface DeleteBuildingUseCase {
    /**
     * Удалить здание по его литере.
     * 
     * @param command команда с данными для удаления
     * @throws zakir.alekperov.domain.shared.ValidationException если валидация не прошла
     */
    void execute(DeleteBuildingCommand command);
}
