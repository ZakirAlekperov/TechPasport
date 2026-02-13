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
            CREATE TABLE IF NOT EXISTS location_plan (
                passport_id TEXT PRIMARY KEY,
                plan_mode TEXT NOT NULL DEFAULT 'MANUAL_DRAWING' CHECK (plan_mode IN ('MANUAL_DRAWING', 'UPLOADED_IMAGE')),
                scale_denominator INTEGER,
                executor_name TEXT,
                plan_date TEXT NOT NULL,
                notes TEXT,
                uploaded_image_data BLOB,
                uploaded_image_filename TEXT,
                uploaded_image_format TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (passport_id) REFERENCES passports(id) ON DELETE CASCADE
            );
            
            CREATE TABLE IF NOT EXISTS building_coordinates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                passport_id TEXT NOT NULL,
                litera TEXT NOT NULL,
                description TEXT,
                point_index INTEGER NOT NULL,
                x_coordinate REAL NOT NULL,
                y_coordinate REAL NOT NULL,
                FOREIGN KEY (passport_id) REFERENCES location_plan(passport_id) ON DELETE CASCADE
            );
            
            CREATE INDEX IF NOT EXISTS idx_location_plan_mode 
                ON location_plan(plan_mode);
            CREATE INDEX IF NOT EXISTS idx_building_coordinates_passport 
                ON building_coordinates(passport_id);
            CREATE INDEX IF NOT EXISTS idx_building_coordinates_litera 
                ON building_coordinates(passport_id, litera);
            
            INSERT INTO schema_version (version, description) 
            VALUES (2, 'Создание таблиц ситуационных планов');
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}
