package zakir.alekperov.infrastructure.database;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String DATABASE_NAME = "techpasport.db";
    private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";
    
    private final String databasePath;
    private Connection connection;
    
    public DatabaseConnection(String databaseDirectory) {
        if (databaseDirectory == null || databaseDirectory.isBlank()) {
            databaseDirectory = System.getProperty("user.home") + "/.techpasport";
        }
        
        Path dbPath = Paths.get(databaseDirectory, DATABASE_NAME);
        this.databasePath = dbPath.toAbsolutePath().toString();
        
        dbPath.getParent().toFile().mkdirs();
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(JDBC_URL_PREFIX + databasePath);
            connection.setAutoCommit(false);
            
            var stmt = connection.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.close();
        }
        return connection;
    }
    
    public String getDatabasePath() {
        return databasePath;
    }
    
    public void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Ошибка закрытия соединения: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
