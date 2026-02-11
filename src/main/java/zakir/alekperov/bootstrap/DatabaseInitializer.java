package zakir.alekperov.bootstrap;

import zakir.alekperov.infrastructure.database.DatabaseConnection;
import zakir.alekperov.infrastructure.database.DatabaseMigration;

import java.sql.SQLException;

public final class DatabaseInitializer {
    private final DatabaseConnection databaseConnection;
    
    public DatabaseInitializer(DatabaseConnection databaseConnection) {
        if (databaseConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection не может быть null");
        }
        this.databaseConnection = databaseConnection;
    }
    
    public void initialize() {
        System.out.println("Инициализация базы данных...");
        System.out.println("Путь к БД: " + databaseConnection.getDatabasePath());
        
        try {
            DatabaseMigration migration = new DatabaseMigration(databaseConnection);
            migration.migrate();
            
            System.out.println("База данных инициализирована успешно");
            
        } catch (SQLException e) {
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА: Не удалось инициализировать БД");
            e.printStackTrace();
            throw new RuntimeException("Ошибка инициализации БД", e);
        }
    }
}
