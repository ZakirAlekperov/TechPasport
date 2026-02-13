package zakir.alekperov.application.passport;

/**
 * Команда для создания нового технического паспорта.
 * 
 * Использует паттерн Command для инкапсуляции параметров.
 */
public record CreateNewPassportCommand(
    String organizationName
) {
    
    public CreateNewPassportCommand {
        if (organizationName == null || organizationName.isBlank()) {
            organizationName = "Не указано"; // Значение по умолчанию
        }
    }
}
