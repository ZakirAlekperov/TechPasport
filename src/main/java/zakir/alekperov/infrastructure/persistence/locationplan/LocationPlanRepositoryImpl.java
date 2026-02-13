package zakir.alekperov.infrastructure.persistence.locationplan;

import zakir.alekperov.domain.locationplan.*;
import zakir.alekperov.domain.shared.PassportId;
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
            boolean exists = existsByIdInternal(connection, plan.getPassportId());
            if (exists) {
                updateInternal(connection, plan);
            } else {
                insertInternal(connection, plan);
            }
        });
    }
    
    private void insertInternal(Connection connection, LocationPlan plan) throws SQLException {
        String sql = """
            INSERT INTO location_plan (
                passport_id, plan_mode, scale_denominator, executor_name, 
                plan_date, notes, uploaded_image_data, uploaded_image_filename, uploaded_image_format
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, plan.getPassportId().getValue());
            stmt.setString(2, plan.getPlanMode().name());
            
            if (plan.getScale().isPresent()) {
                stmt.setInt(3, plan.getScale().get().denominator());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            if (plan.getExecutorName().isPresent()) {
                stmt.setString(4, plan.getExecutorName().get());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }
            
            stmt.setString(5, plan.getPlanDate().toString());
            stmt.setString(6, plan.getNotes());
            
            if (plan.getUploadedImage().isPresent()) {
                PlanImage image = plan.getUploadedImage().get();
                stmt.setBytes(7, image.getImageData());
                stmt.setString(8, image.getFileName());
                stmt.setString(9, image.getFormat());
            } else {
                stmt.setNull(7, Types.BLOB);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.VARCHAR);
            }
            
            stmt.executeUpdate();
            saveBuildingCoordinates(connection, plan);
        }
    }
    
    private void updateInternal(Connection connection, LocationPlan plan) throws SQLException {
        String sql = """
            UPDATE location_plan
            SET plan_mode = ?,
                scale_denominator = ?,
                executor_name = ?,
                plan_date = ?,
                notes = ?,
                uploaded_image_data = ?,
                uploaded_image_filename = ?,
                uploaded_image_format = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE passport_id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, plan.getPlanMode().name());
            
            if (plan.getScale().isPresent()) {
                stmt.setInt(2, plan.getScale().get().denominator());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            if (plan.getExecutorName().isPresent()) {
                stmt.setString(3, plan.getExecutorName().get());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }
            
            stmt.setString(4, plan.getPlanDate().toString());
            stmt.setString(5, plan.getNotes());
            
            if (plan.getUploadedImage().isPresent()) {
                PlanImage image = plan.getUploadedImage().get();
                stmt.setBytes(6, image.getImageData());
                stmt.setString(7, image.getFileName());
                stmt.setString(8, image.getFormat());
            } else {
                stmt.setNull(6, Types.BLOB);
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.VARCHAR);
            }
            
            stmt.setString(9, plan.getPassportId().getValue());
            
            stmt.executeUpdate();
            deleteBuildingCoordinates(connection, plan.getPassportId());
            saveBuildingCoordinates(connection, plan);
        }
    }
    
    @Override
    public Optional<LocationPlan> findById(PassportId passportId) {
        return transactionTemplate.executeInTransaction(connection -> {
            String sql = """
                SELECT passport_id, plan_mode, scale_denominator, executor_name,
                       plan_date, notes, uploaded_image_data, uploaded_image_filename, uploaded_image_format
                FROM location_plan
                WHERE passport_id = ?
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, passportId.getValue());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToPlan(rs, connection));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Ошибка поиска плана", e);
            }
        });
    }
    
    private LocationPlan mapResultSetToPlan(ResultSet rs, Connection connection) throws SQLException {
        PassportId passportId = PassportId.fromString(rs.getString("passport_id"));
        PlanMode planMode = PlanMode.valueOf(rs.getString("plan_mode"));
        LocalDate planDate = LocalDate.parse(rs.getString("plan_date"));
        String notes = rs.getString("notes");
        
        if (planMode == PlanMode.UPLOADED_IMAGE) {
            byte[] imageData = rs.getBytes("uploaded_image_data");
            String fileName = rs.getString("uploaded_image_filename");
            PlanImage planImage = new PlanImage(imageData, fileName);
            
            return LocationPlan.createWithUploadedImage(passportId, planImage, planDate, notes);
        } else {
            int scaleDenom = rs.getInt("scale_denominator");
            String executorName = rs.getString("executor_name");
            
            PlanScale scale = new PlanScale(scaleDenom);
            LocationPlan plan = LocationPlan.createManualDrawing(
                passportId, scale, executorName, planDate, notes
            );
            
            List<BuildingCoordinates> buildings = loadBuildingCoordinates(connection, passportId);
            for (BuildingCoordinates building : buildings) {
                plan.addBuilding(building);
            }
            
            return plan;
        }
    }
    
    private void saveBuildingCoordinates(Connection connection, LocationPlan plan) throws SQLException {
        if (plan.getBuildings().isEmpty()) {
            return;
        }
        
        String sql = """
            INSERT INTO building_coordinates (passport_id, litera, description, point_index, x_coordinate, y_coordinate)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (BuildingCoordinates building : plan.getBuildings()) {
                int pointIndex = 0;
                for (CoordinatePoint point : building.getPoints()) {
                    stmt.setString(1, plan.getPassportId().getValue());
                    stmt.setString(2, building.getLitera().value());
                    stmt.setString(3, building.getDescription());
                    stmt.setInt(4, pointIndex++);
                    stmt.setDouble(5, point.x());
                    stmt.setDouble(6, point.y());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }
    
    private List<BuildingCoordinates> loadBuildingCoordinates(Connection connection, PassportId passportId) throws SQLException {
        String sql = """
            SELECT litera, description, point_index, x_coordinate, y_coordinate
            FROM building_coordinates
            WHERE passport_id = ?
            ORDER BY litera, point_index
            """;
        
        List<BuildingCoordinates> buildings = new ArrayList<>();
        String currentLitera = null;
        String currentDesc = null;
        List<CoordinatePoint> currentPoints = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passportId.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String litera = rs.getString("litera");
                    String description = rs.getString("description");
                    double x = rs.getDouble("x_coordinate");
                    double y = rs.getDouble("y_coordinate");
                    
                    if (currentLitera == null || !currentLitera.equals(litera)) {
                        if (currentLitera != null) {
                            buildings.add(new BuildingCoordinates(
                                new BuildingLitera(currentLitera),
                                currentDesc,
                                currentPoints
                            ));
                            currentPoints = new ArrayList<>();
                        }
                        currentLitera = litera;
                        currentDesc = description;
                    }
                    
                    currentPoints.add(new CoordinatePoint(x, y));
                }
                
                if (currentLitera != null) {
                    buildings.add(new BuildingCoordinates(
                        new BuildingLitera(currentLitera),
                        currentDesc,
                        currentPoints
                    ));
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
    
    private boolean existsByIdInternal(Connection connection, PassportId passportId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM location_plan WHERE passport_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passportId.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
}
