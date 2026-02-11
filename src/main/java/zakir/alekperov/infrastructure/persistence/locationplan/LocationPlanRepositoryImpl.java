package zakir.alekperov.infrastructure.persistence.locationplan;

import zakir.alekperov.domain.shared.PassportId;
import zakir.alekperov.domain.locationplan.*;
import zakir.alekperov.infrastructure.database.TransactionTemplate;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LocationPlanRepositoryImpl implements LocationPlanRepository {
    private final TransactionTemplate transactionTemplate;
    
    public LocationPlanRepositoryImpl(TransactionTemplate transactionTemplate) {
        if (transactionTemplate == null) {
            throw new IllegalArgumentException("TransactionTemplate не может быть null");
        }
        this.transactionTemplate = transactionTemplate;
    }
    
    @Override
    public void save(LocationPlan plan) {
        transactionTemplate.executeInTransaction(connection -> {
            String sql = """
                INSERT INTO location_plans (
                    passport_id, scale_denominator, executor_name, 
                    plan_date, notes, image_path
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, plan.getPassportId().getValue());
                stmt.setInt(2, plan.getScale().getDenominator());
                stmt.setString(3, plan.getExecutorName());
                stmt.setString(4, plan.getPlanDate().toString());
                stmt.setString(5, plan.getNotes());
                stmt.setString(6, plan.getImagePath());
                
                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new RuntimeException("Не удалось сохранить ситуационный план");
                }
                
                saveBuildingCoordinates(connection, plan);
                
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка сохранения ситуационного плана", e);
            }
        });
    }
    
    @Override
    public Optional<LocationPlan> findByPassportId(PassportId passportId) {
        return transactionTemplate.executeInTransaction(connection -> {
            String sql = """
                SELECT passport_id, scale_denominator, executor_name,
                       plan_date, notes, image_path
                FROM location_plans
                WHERE passport_id = ?
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, passportId.getValue());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        LocationPlan plan = mapResultSetToPlan(rs, connection);
                        return Optional.of(plan);
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка поиска ситуационного плана", e);
            }
        });
    }
    
    @Override
    public void update(LocationPlan plan) {
        transactionTemplate.executeInTransaction(connection -> {
            String sql = """
                UPDATE location_plans
                SET scale_denominator = ?,
                    executor_name = ?,
                    plan_date = ?,
                    notes = ?,
                    image_path = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE passport_id = ?
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, plan.getScale().getDenominator());
                stmt.setString(2, plan.getExecutorName());
                stmt.setString(3, plan.getPlanDate().toString());
                stmt.setString(4, plan.getNotes());
                stmt.setString(5, plan.getImagePath());
                stmt.setString(6, plan.getPassportId().getValue());
                
                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new RuntimeException("План не найден для обновления");
                }
                
                deleteBuildingCoordinates(connection, plan.getPassportId());
                saveBuildingCoordinates(connection, plan);
                
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка обновления ситуационного плана", e);
            }
        });
    }
    
    @Override
    public boolean existsByPassportId(PassportId passportId) {
        return transactionTemplate.executeInTransaction(connection -> {
            String sql = "SELECT COUNT(*) FROM location_plans WHERE passport_id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, passportId.getValue());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                    return false;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка проверки существования плана", e);
            }
        });
    }
    
    @Override
    public void delete(PassportId passportId) {
        transactionTemplate.executeInTransaction(connection -> {
            String sql = "DELETE FROM location_plans WHERE passport_id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, passportId.getValue());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка удаления ситуационного плана", e);
            }
        });
    }
    
    private LocationPlan mapResultSetToPlan(ResultSet rs, Connection connection) throws SQLException {
        String passportIdStr = rs.getString("passport_id");
        int scaleDenominator = rs.getInt("scale_denominator");
        String executorName = rs.getString("executor_name");
        String planDateStr = rs.getString("plan_date");
        String notes = rs.getString("notes");
        String imagePath = rs.getString("image_path");
        
        PassportId passportId = PassportId.fromString(passportIdStr);
        PlanScale scale = PlanScale.fromDenominator(scaleDenominator);
        LocalDate planDate = LocalDate.parse(planDateStr);
        
        List<BuildingCoordinates> buildings = loadBuildingCoordinates(connection, passportId);
        
        return LocationPlan.restore(
            passportId, scale, buildings, executorName, planDate, notes, imagePath
        );
    }
    
    private void saveBuildingCoordinates(Connection connection, LocationPlan plan) throws SQLException {
        if (plan.getBuildingsCoordinates().isEmpty()) {
            return;
        }
        
        String buildingSql = """
            INSERT INTO building_coordinates (passport_id, litera, description)
            VALUES (?, ?, ?)
            """;
        
        String pointSql = """
            INSERT INTO coordinate_points (building_coordinates_id, point_number, x, y)
            VALUES (?, ?, ?, ?)
            """;
        
        try (PreparedStatement buildingStmt = connection.prepareStatement(buildingSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pointStmt = connection.prepareStatement(pointSql)) {
            
            for (BuildingCoordinates building : plan.getBuildingsCoordinates()) {
                buildingStmt.setString(1, plan.getPassportId().getValue());
                buildingStmt.setString(2, building.getLitera());
                buildingStmt.setString(3, building.getDescription());
                buildingStmt.executeUpdate();
                
                try (ResultSet generatedKeys = buildingStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long buildingId = generatedKeys.getLong(1);
                        
                        int pointNumber = 1;
                        for (CoordinatePoint point : building.getPoints()) {
                            pointStmt.setLong(1, buildingId);
                            pointStmt.setInt(2, pointNumber++);
                            pointStmt.setString(3, point.formatX());
                            pointStmt.setString(4, point.formatY());
                            pointStmt.addBatch();
                        }
                        pointStmt.executeBatch();
                    }
                }
            }
        }
    }
    
    private List<BuildingCoordinates> loadBuildingCoordinates(Connection connection, PassportId passportId) throws SQLException {
        List<BuildingCoordinates> buildings = new ArrayList<>();
        
        String buildingSql = """
            SELECT id, litera, description
            FROM building_coordinates
            WHERE passport_id = ?
            ORDER BY litera
            """;
        
        String pointsSql = """
            SELECT x, y
            FROM coordinate_points
            WHERE building_coordinates_id = ?
            ORDER BY point_number
            """;
        
        try (PreparedStatement buildingStmt = connection.prepareStatement(buildingSql);
             PreparedStatement pointsStmt = connection.prepareStatement(pointsSql)) {
            
            buildingStmt.setString(1, passportId.getValue());
            
            try (ResultSet buildingRs = buildingStmt.executeQuery()) {
                while (buildingRs.next()) {
                    long buildingId = buildingRs.getLong("id");
                    String litera = buildingRs.getString("litera");
                    String description = buildingRs.getString("description");
                    
                    List<CoordinatePoint> points = new ArrayList<>();
                    pointsStmt.setLong(1, buildingId);
                    
                    try (ResultSet pointsRs = pointsStmt.executeQuery()) {
                        while (pointsRs.next()) {
                            String x = pointsRs.getString("x");
                            String y = pointsRs.getString("y");
                            points.add(CoordinatePoint.fromStrings(x, y));
                        }
                    }
                    
                    if (!points.isEmpty()) {
                        buildings.add(BuildingCoordinates.create(litera, description, points));
                    }
                }
            }
        }
        
        return buildings;
    }
    
    private void deleteBuildingCoordinates(Connection connection, PassportId passportId) throws SQLException {
        String sql = "DELETE FROM building_coordinates WHERE passport_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passportId.getValue());
            stmt.executeUpdate();
        }
    }
}
