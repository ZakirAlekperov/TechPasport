package zakir.alekperov.infrastructure.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseMigration {
    private final DatabaseConnection databaseConnection;
    
    public DatabaseMigration(DatabaseConnection databaseConnection) {
        if (databaseConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection не может быть null");
        }
        this.databaseConnection = databaseConnection;
    }
    
    public void migrate() throws SQLException {
        Connection connection = databaseConnection.getConnection();
        
        try {
            createVersionTable(connection);
            
            int currentVersion = getCurrentVersion(connection);
            
            if (currentVersion < 1) {
                applyMigration001(connection);
            }
            if (currentVersion < 2) {
                applyMigration002(connection);
            }
            
            connection.commit();
            
            System.out.println("Миграции БД применены успешно");
            
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("Ошибка применения миграций", e);
        }
    }
    
    private void createVersionTable(Connection connection) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS schema_version (
                version INTEGER PRIMARY KEY,
                applied_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                description TEXT
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
    
    private int getCurrentVersion(Connection connection) throws SQLException {
        String sql = "SELECT COALESCE(MAX(version), 0) FROM schema_version";
        
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    private void applyMigration001(Connection connection) throws SQLException {
        System.out.println("Применение миграции 001: Создание таблиц паспортов");
        
        String sql = """
            CREATE TABLE IF NOT EXISTS passports (
                id TEXT PRIMARY KEY,
                organization_name TEXT NOT NULL,
                inventory_number TEXT,
                cadastral_number TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            );
            
            INSERT INTO schema_version (version, description) 
            VALUES (1, 'Создание основных таблиц');
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    private void applyMigration002(Connection connection) throws SQLException {
        System.out.println("Применение миграции 002: Создание таблиц ситуационных планов");
        
        String sql = """
            CREATE TABLE IF NOT EXISTS location_plans (
                passport_id TEXT PRIMARY KEY,
                scale_denominator INTEGER NOT NULL,
                executor_name TEXT,
                plan_date TEXT NOT NULL,
                notes TEXT,
                image_path TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (passport_id) REFERENCES passports(id) ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS building_coordinates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                passport_id TEXT NOT NULL,
                litera TEXT NOT NULL,
                description TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (passport_id) REFERENCES passports(id) ON DELETE CASCADE,
                UNIQUE(passport_id, litera)
            );
            
            CREATE TABLE IF NOT EXISTS coordinate_points (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                building_coordinates_id INTEGER NOT NULL,
                point_number INTEGER NOT NULL,
                x TEXT NOT NULL,
                y TEXT NOT NULL,
                FOREIGN KEY (building_coordinates_id) REFERENCES building_coordinates(id) ON DELETE CASCADE,
                UNIQUE(building_coordinates_id, point_number)
            );
            
            CREATE INDEX IF NOT EXISTS idx_building_coordinates_passport 
                ON building_coordinates(passport_id);
            CREATE INDEX IF NOT EXISTS idx_coordinate_points_building 
                ON coordinate_points(building_coordinates_id);
            
            INSERT INTO schema_version (version, description) 
            VALUES (2, 'Создание таблиц ситуационных планов');
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}
