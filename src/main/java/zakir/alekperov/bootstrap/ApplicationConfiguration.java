package zakir.alekperov.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ApplicationConfiguration {
    private static final String DEFAULT_DATABASE_DIRECTORY = System.getProperty("user.home") + "/.techpasport";
    private static final String CONFIG_FILE = "application.properties";
    
    private final Properties properties;
    
    public ApplicationConfiguration() {
        this.properties = new Properties();
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("Конфигурация загружена из " + CONFIG_FILE);
            } else {
                System.out.println("Файл конфигурации не найден, используются значения по умолчанию");
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
        }
    }
    
    public String getDatabaseDirectory() {
        return properties.getProperty("database.directory", DEFAULT_DATABASE_DIRECTORY);
    }
    
    public String getApplicationName() {
        return properties.getProperty("application.name", "TechPasport");
    }
    
    public String getApplicationVersion() {
        return properties.getProperty("application.version", "1.0.0");
    }
}
