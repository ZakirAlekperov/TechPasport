package zakir.alekperov.infrastructure.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public final class TransactionTemplate {
    private final DatabaseConnection databaseConnection;
    
    public TransactionTemplate(DatabaseConnection databaseConnection) {
        if (databaseConnection == null) {
            throw new IllegalArgumentException("DatabaseConnection не может быть null");
        }
        this.databaseConnection = databaseConnection;
    }
    
    public <T> T executeInTransaction(Function<Connection, T> operation) {
        Connection connection = null;
        try {
            connection = databaseConnection.getConnection();
            T result = operation.apply(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            rollback(connection);
            throw new RuntimeException("Ошибка выполнения транзакции", e);
        } catch (Exception e) {
            rollback(connection);
            throw new RuntimeException("Неожиданная ошибка в транзакции", e);
        }
    }
    
    public void executeInTransaction(java.util.function.Consumer<Connection> operation) {
        executeInTransaction(connection -> {
            operation.accept(connection);
            return null;
        });
    }
    
    private void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Ошибка rollback: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
        }
    }
}
