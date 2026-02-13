package zakir.alekperov.domain.passport;

import java.util.Optional;

/**
 * Репозиторий для работы с техническими паспортами.
 * Domain-слой: только контракт, без реализации.
 */
public interface PassportRepository {
    
    /**
     * Создать новый паспорт в системе.
     * 
     * @param passport данные нового паспорта
     * @throws IllegalArgumentException если паспорт невалиден
     * @throws RuntimeException если произошла техническая ошибка
     */
    void create(Passport passport);
    
    /**
     * Найти паспорт по ID.
     * 
     * @param passportId идентификатор паспорта
     * @return Optional с паспортом или пустой, если не найден
     */
    Optional<Passport> findById(String passportId);
    
    /**
     * Проверить существование паспорта.
     * 
     * @param passportId идентификатор паспорта
     * @return true если паспорт существует
     */
    boolean exists(String passportId);
    
    /**
     * Удалить паспорт.
     * 
     * @param passportId идентификатор паспорта
     */
    void delete(String passportId);
}
