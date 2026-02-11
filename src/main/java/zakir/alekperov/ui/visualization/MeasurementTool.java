package zakir.alekperov.ui.visualization;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import zakir.alekperov.application.locationplan.LocationPlanDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Инструмент для измерения расстояний, периметров и площадей на плане.
 * Поддерживает работу с реальными геодезическими координатами МСК-67.
 */
public class MeasurementTool {
    
    private static final Color MEASUREMENT_LINE_COLOR = Color.rgb(255, 87, 34);
    private static final Color MEASUREMENT_POINT_COLOR = Color.rgb(255, 87, 34);
    private static final Color MEASUREMENT_TEXT_COLOR = Color.rgb(255, 87, 34);
    private static final Color MEASUREMENT_TEXT_BG = Color.rgb(255, 255, 255, 0.9);
    private static final double MEASUREMENT_LINE_WIDTH = 2.5;
    private static final double MEASUREMENT_POINT_RADIUS = 5.0;
    
    private boolean active = false;
    private MeasurementMode mode = MeasurementMode.DISTANCE;
    private List<MeasurementPoint> points = new ArrayList<>();
    
    // Смещение для преобразования реальных координат в локальные
    private double originX = 0.0;
    private double originY = 0.0;
    
    public enum MeasurementMode {
        DISTANCE,      // Расстояние между двумя точками
        PERIMETER,     // Периметр здания
        AREA          // Площадь здания
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            clearMeasurement();
        }
    }
    
    public MeasurementMode getMode() {
        return mode;
    }
    
    public void setMode(MeasurementMode mode) {
        this.mode = mode;
        clearMeasurement();
    }
    
    /**
     * Установить смещение origin для работы с большими координатами.
     */
    public void setOrigin(double originX, double originY) {
        this.originX = originX;
        this.originY = originY;
    }
    
    public void addPoint(double worldX, double worldY) {
        if (!active) return;
        
        switch (mode) {
            case DISTANCE:
                // Для расстояния нужны только 2 точки
                if (points.size() >= 2) {
                    points.clear();
                }
                points.add(new MeasurementPoint(worldX, worldY));
                break;
                
            case PERIMETER:
            case AREA:
                // Для периметра и площади можно добавлять множество точек
                points.add(new MeasurementPoint(worldX, worldY));
                break;
        }
    }
    
    public void clearMeasurement() {
        points.clear();
    }
    
    public boolean hasPoints() {
        return !points.isEmpty();
    }
    
    public int getPointCount() {
        return points.size();
    }
    
    /**
     * Вычислить расстояние между двумя точками в метрах.
     */
    public Double calculateDistance() {
        if (points.size() < 2) {
            return null;
        }
        
        MeasurementPoint p1 = points.get(0);
        MeasurementPoint p2 = points.get(1);
        
        return Math.sqrt(
            Math.pow(p2.worldX - p1.worldX, 2) + 
            Math.pow(p2.worldY - p1.worldY, 2)
        );
    }
    
    /**
     * Вычислить периметр полигона в метрах.
     */
    public Double calculatePerimeter() {
        if (points.size() < 2) {
            return null;
        }
        
        double perimeter = 0.0;
        for (int i = 0; i < points.size(); i++) {
            MeasurementPoint p1 = points.get(i);
            MeasurementPoint p2 = points.get((i + 1) % points.size());
            
            perimeter += Math.sqrt(
                Math.pow(p2.worldX - p1.worldX, 2) + 
                Math.pow(p2.worldY - p1.worldY, 2)
            );
        }
        
        return perimeter;
    }
    
    /**
     * Вычислить площадь полигона в квадратных метрах (формула Гаусса).
     */
    public Double calculateArea() {
        if (points.size() < 3) {
            return null;
        }
        
        double area = 0.0;
        for (int i = 0; i < points.size(); i++) {
            MeasurementPoint p1 = points.get(i);
            MeasurementPoint p2 = points.get((i + 1) % points.size());
            
            area += (p1.worldX * p2.worldY) - (p2.worldX * p1.worldY);
        }
        
        return Math.abs(area) / 2.0;
    }
    
    /**
     * Вычислить параметры для здания.
     */
    public BuildingMeasurements measureBuilding(LocationPlanDTO.BuildingCoordinatesDTO building) {
        if (building == null || building.points().isEmpty()) {
            return null;
        }
        
        try {
            List<MeasurementPoint> buildingPoints = new ArrayList<>();
            for (LocationPlanDTO.CoordinatePointDTO point : building.points()) {
                double x = Double.parseDouble(point.x());
                double y = Double.parseDouble(point.y());
                buildingPoints.add(new MeasurementPoint(x, y));
            }
            
            // Периметр
            double perimeter = 0.0;
            for (int i = 0; i < buildingPoints.size(); i++) {
                MeasurementPoint p1 = buildingPoints.get(i);
                MeasurementPoint p2 = buildingPoints.get((i + 1) % buildingPoints.size());
                
                perimeter += Math.sqrt(
                    Math.pow(p2.worldX - p1.worldX, 2) + 
                    Math.pow(p2.worldY - p1.worldY, 2)
                );
            }
            
            // Площадь
            double area = 0.0;
            for (int i = 0; i < buildingPoints.size(); i++) {
                MeasurementPoint p1 = buildingPoints.get(i);
                MeasurementPoint p2 = buildingPoints.get((i + 1) % buildingPoints.size());
                
                area += (p1.worldX * p2.worldY) - (p2.worldX * p1.worldY);
            }
            area = Math.abs(area) / 2.0;
            
            return new BuildingMeasurements(perimeter, area);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Отрисовать измерения на Canvas.
     */
    public void draw(GraphicsContext gc, CanvasTransform transform) {
        if (!active || points.isEmpty()) {
            return;
        }
        
        gc.save();
        
        // Отрисовать линии между точками
        gc.setStroke(MEASUREMENT_LINE_COLOR);
        gc.setLineWidth(MEASUREMENT_LINE_WIDTH / transform.getScale());
        gc.setLineDashes(5.0 / transform.getScale());
        
        if (mode == MeasurementMode.DISTANCE && points.size() == 2) {
            // Прямая линия между двумя точками
            MeasurementPoint p1 = points.get(0);
            MeasurementPoint p2 = points.get(1);
            
            // Преобразуем в локальные координаты
            double localX1 = p1.worldX - originX;
            double localY1 = p1.worldY - originY;
            double localX2 = p2.worldX - originX;
            double localY2 = p2.worldY - originY;
            
            gc.strokeLine(localX1, localY1, localX2, localY2);
            
            // Текст с расстоянием в центре
            double midX = (localX1 + localX2) / 2.0;
            double midY = (localY1 + localY2) / 2.0;
            Double distance = calculateDistance();
            if (distance != null) {
                drawMeasurementText(gc, transform, midX, midY, 
                    String.format("%.2f м", distance));
            }
        } else if ((mode == MeasurementMode.PERIMETER || mode == MeasurementMode.AREA) && points.size() > 1) {
            // Полигон
            for (int i = 0; i < points.size(); i++) {
                MeasurementPoint p1 = points.get(i);
                MeasurementPoint p2 = points.get((i + 1) % points.size());
                
                // Преобразуем в локальные координаты
                double localX1 = p1.worldX - originX;
                double localY1 = p1.worldY - originY;
                double localX2 = p2.worldX - originX;
                double localY2 = p2.worldY - originY;
                
                gc.strokeLine(localX1, localY1, localX2, localY2);
            }
            
            // Текст с результатом в центре полигона
            double centerX = 0.0;
            double centerY = 0.0;
            for (MeasurementPoint p : points) {
                centerX += (p.worldX - originX);
                centerY += (p.worldY - originY);
            }
            centerX /= points.size();
            centerY /= points.size();
            
            String measurementText = "";
            if (mode == MeasurementMode.PERIMETER) {
                Double perimeter = calculatePerimeter();
                if (perimeter != null) {
                    measurementText = String.format("P = %.2f м", perimeter);
                }
            } else if (mode == MeasurementMode.AREA) {
                Double area = calculateArea();
                if (area != null) {
                    measurementText = String.format("S = %.2f м²", area);
                }
            }
            
            if (!measurementText.isEmpty()) {
                drawMeasurementText(gc, transform, centerX, centerY, measurementText);
            }
        }
        
        gc.setLineDashes(); // Сбросить пунктир
        
        // Отрисовать точки
        gc.setFill(MEASUREMENT_POINT_COLOR);
        for (MeasurementPoint point : points) {
            double radius = MEASUREMENT_POINT_RADIUS / transform.getScale();
            // Преобразуем в локальные координаты
            double localX = point.worldX - originX;
            double localY = point.worldY - originY;
            
            gc.fillOval(
                localX - radius,
                localY - radius,
                radius * 2,
                radius * 2
            );
        }
        
        gc.restore();
    }
    
    private void drawMeasurementText(GraphicsContext gc, CanvasTransform transform, 
                                    double localX, double localY, String text) {
        double fontSize = 12.0 / transform.getScale();
        gc.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        
        // Размеры текста (приблизительно)
        double textWidth = text.length() * fontSize * 0.6;
        double textHeight = fontSize * 1.2;
        
        // Фон для текста
        gc.setFill(MEASUREMENT_TEXT_BG);
        gc.fillRect(
            localX - textWidth / 2 - 2 / transform.getScale(),
            localY - textHeight / 2 - 2 / transform.getScale(),
            textWidth + 4 / transform.getScale(),
            textHeight + 4 / transform.getScale()
        );
        
        // Текст
        gc.setFill(MEASUREMENT_TEXT_COLOR);
        gc.fillText(text, localX - textWidth / 2, localY + fontSize / 3);
    }
    
    /**
     * Точка измерения в реальных координатах МСК-67.
     */
    public static class MeasurementPoint {
        public final double worldX;  // Реальные координаты МСК-67
        public final double worldY;  // Реальные координаты МСК-67
        
        public MeasurementPoint(double worldX, double worldY) {
            this.worldX = worldX;
            this.worldY = worldY;
        }
    }
    
    /**
     * Результат измерения здания.
     */
    public static class BuildingMeasurements {
        public final double perimeter;
        public final double area;
        
        public BuildingMeasurements(double perimeter, double area) {
            this.perimeter = perimeter;
            this.area = area;
        }
    }
}
