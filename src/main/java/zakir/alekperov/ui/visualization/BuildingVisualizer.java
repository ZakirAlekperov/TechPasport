package zakir.alekperov.ui.visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import zakir.alekperov.application.locationplan.LocationPlanDTO;

import java.util.List;

/**
 * Класс для визуализации зданий на Canvas.
 * Отрисовывает контуры зданий, точки, литеры и сетку.
 */
public class BuildingVisualizer {
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    
    // Цвета
    private static final Color BACKGROUND_COLOR = Color.rgb(250, 250, 250);
    private static final Color GRID_COLOR = Color.rgb(220, 220, 220);
    private static final Color BUILDING_STROKE_COLOR = Color.rgb(33, 150, 243);
    private static final Color BUILDING_FILL_COLOR = Color.rgb(33, 150, 243, 0.1);
    private static final Color POINT_COLOR = Color.rgb(255, 87, 34);
    private static final Color TEXT_COLOR = Color.rgb(33, 33, 33);
    
    // Параметры отрисовки
    private static final double PADDING = 40.0;
    private static final double POINT_RADIUS = 4.0;
    private static final double BUILDING_STROKE_WIDTH = 2.0;
    
    public BuildingVisualizer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }
    
    /**
     * Отрисовать все здания.
     */
    public void draw(List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        // Очистить canvas
        clearCanvas();
        
        if (buildings == null || buildings.isEmpty()) {
            return;
        }
        
        // Найти границы всех зданий
        Bounds bounds = calculateBounds(buildings);
        
        if (bounds == null) {
            return;
        }
        
        // Рассчитать масштаб
        double scale = calculateScale(bounds);
        
        // Отрисовать сетку
        drawGrid(bounds, scale);
        
        // Отрисовать каждое здание
        for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
            drawBuilding(building, bounds, scale);
        }
        
        // Отрисовать информацию о масштабе
        drawScaleInfo(bounds, scale);
    }
    
    /**
     * Очистить canvas.
     */
    private void clearCanvas() {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    /**
     * Найти границы всех зданий.
     */
    private Bounds calculateBounds(List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
            for (LocationPlanDTO.CoordinatePointDTO point : building.points()) {
                try {
                    double x = Double.parseDouble(point.x());
                    double y = Double.parseDouble(point.y());
                    
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                } catch (NumberFormatException e) {
                    // Пропустить некорректные координаты
                }
            }
        }
        
        if (minX == Double.MAX_VALUE) {
            return null;
        }
        
        return new Bounds(minX, maxX, minY, maxY);
    }
    
    /**
     * Рассчитать масштаб для отображения.
     */
    private double calculateScale(Bounds bounds) {
        double width = bounds.maxX - bounds.minX;
        double height = bounds.maxY - bounds.minY;
        
        double availableWidth = canvas.getWidth() - 2 * PADDING;
        double availableHeight = canvas.getHeight() - 2 * PADDING;
        
        double scaleX = availableWidth / width;
        double scaleY = availableHeight / height;
        
        return Math.min(scaleX, scaleY);
    }
    
    /**
     * Преобразовать координаты в пиксели canvas.
     */
    private double toCanvasX(double x, Bounds bounds, double scale) {
        return PADDING + (x - bounds.minX) * scale;
    }
    
    private double toCanvasY(double y, Bounds bounds, double scale) {
        // Инвертировать Y, так как canvas имеет начало сверху
        return canvas.getHeight() - PADDING - (y - bounds.minY) * scale;
    }
    
    /**
     * Отрисовать сетку.
     */
    private void drawGrid(Bounds bounds, double scale) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);
        
        double gridSize = 10.0; // Шаг сетки в единицах координат
        
        // Вертикальные линии
        for (double x = Math.floor(bounds.minX / gridSize) * gridSize; x <= bounds.maxX; x += gridSize) {
            double canvasX = toCanvasX(x, bounds, scale);
            gc.strokeLine(canvasX, PADDING, canvasX, canvas.getHeight() - PADDING);
        }
        
        // Горизонтальные линии
        for (double y = Math.floor(bounds.minY / gridSize) * gridSize; y <= bounds.maxY; y += gridSize) {
            double canvasY = toCanvasY(y, bounds, scale);
            gc.strokeLine(PADDING, canvasY, canvas.getWidth() - PADDING, canvasY);
        }
    }
    
    /**
     * Отрисовать одно здание.
     */
    private void drawBuilding(LocationPlanDTO.BuildingCoordinatesDTO building, Bounds bounds, double scale) {
        List<LocationPlanDTO.CoordinatePointDTO> points = building.points();
        
        if (points.isEmpty()) {
            return;
        }
        
        // Преобразовать координаты в пиксели
        double[] xPoints = new double[points.size()];
        double[] yPoints = new double[points.size()];
        
        for (int i = 0; i < points.size(); i++) {
            try {
                double x = Double.parseDouble(points.get(i).x());
                double y = Double.parseDouble(points.get(i).y());
                
                xPoints[i] = toCanvasX(x, bounds, scale);
                yPoints[i] = toCanvasY(y, bounds, scale);
            } catch (NumberFormatException e) {
                return;
            }
        }
        
        // Отрисовать заливку полигона
        gc.setFill(BUILDING_FILL_COLOR);
        gc.fillPolygon(xPoints, yPoints, points.size());
        
        // Отрисовать контур
        gc.setStroke(BUILDING_STROKE_COLOR);
        gc.setLineWidth(BUILDING_STROKE_WIDTH);
        gc.strokePolygon(xPoints, yPoints, points.size());
        
        // Отрисовать точки
        gc.setFill(POINT_COLOR);
        for (int i = 0; i < xPoints.length; i++) {
            gc.fillOval(xPoints[i] - POINT_RADIUS, yPoints[i] - POINT_RADIUS, 
                       POINT_RADIUS * 2, POINT_RADIUS * 2);
        }
        
        // Отрисовать литеру в центре здания
        double centerX = 0;
        double centerY = 0;
        for (int i = 0; i < xPoints.length; i++) {
            centerX += xPoints[i];
            centerY += yPoints[i];
        }
        centerX /= xPoints.length;
        centerY /= yPoints.length;
        
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("System", FontWeight.BOLD, 16));
        gc.fillText(building.litera(), centerX - 5, centerY + 5);
    }
    
    /**
     * Отрисовать информацию о масштабе.
     */
    private void drawScaleInfo(Bounds bounds, double scale) {
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
        
        String info = String.format("Диапазон: X[%.1f..%.1f], Y[%.1f..%.1f]", 
                                   bounds.minX, bounds.maxX, bounds.minY, bounds.maxY);
        gc.fillText(info, 10, canvas.getHeight() - 10);
    }
    
    /**
     * Класс для хранения границ координат.
     */
    private static class Bounds {
        final double minX;
        final double maxX;
        final double minY;
        final double maxY;
        
        Bounds(double minX, double maxX, double minY, double maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
}
