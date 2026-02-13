package zakir.alekperov.application.passport;

/**
 * Use Case для создания нового технического паспорта.
 * 
 * Контракт:
 * - Создает новый UUID
 * - Создает domain объект Passport
 * - Сохраняет через PassportRepository
 * - Возвращает ID созданного паспорта
 */
public interface CreateNewPassportUseCase {
    
    /**
     * Создать новый паспорт.
     * 
     * @param command команда с параметрами нового паспорта
     * @return ID созданного паспорта
     * @throws RuntimeException если произошла техническая ошибка
     */
    String execute(CreateNewPassportCommand command);
}
