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
 * Отрисовывает контуры зданий, точки, литеры и сетку с поддержкой zoom/pan.
 */
public class BuildingVisualizer {
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final CanvasTransform transform;
    
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
        this.transform = new CanvasTransform();
    }
    
    /**
     * Получить объект управления трансформациями.
     */
    public CanvasTransform getTransform() {
        return transform;
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
        
        // Если трансформация в начальном состоянии, подогнать под границы
        if (transform.getScale() == 1.0 && transform.getTranslateX() == 0.0 && transform.getTranslateY() == 0.0) {
            transform.fitBounds(
                canvas.getWidth(), 
                canvas.getHeight(), 
                bounds.minX, 
                bounds.minY, 
                bounds.maxX, 
                bounds.maxY, 
                PADDING
            );
        }
        
        // Сохранить состояние GraphicsContext
        gc.save();
        
        // Применить трансформацию
        transform.apply(gc);
        
        // Отрисовать сетку
        drawGrid(bounds);
        
        // Отрисовать каждое здание
        for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
            drawBuilding(building);
        }
        
        // Восстановить состояние
        gc.restore();
        
        // Отрисовать информацию (без трансформации)
        drawScaleInfo(bounds);
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
     * Отрисовать сетку в мировых координатах.
     */
    private void drawGrid(Bounds bounds) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5 / transform.getScale()); // Толщина линии независима от масштаба
        
        double gridSize = 10.0; // Шаг сетки в единицах координат
        
        // Вертикальные линии
        for (double x = Math.floor(bounds.minX / gridSize) * gridSize; x <= bounds.maxX; x += gridSize) {
            gc.strokeLine(x, bounds.minY, x, bounds.maxY);
        }
        
        // Горизонтальные линии
        for (double y = Math.floor(bounds.minY / gridSize) * gridSize; y <= bounds.maxY; y += gridSize) {
            gc.strokeLine(bounds.minX, y, bounds.maxX, y);
        }
    }
    
    /**
     * Отрисовать одно здание в мировых координатах.
     */
    private void drawBuilding(LocationPlanDTO.BuildingCoordinatesDTO building) {
        List<LocationPlanDTO.CoordinatePointDTO> points = building.points();
        
        if (points.isEmpty()) {
            return;
        }
        
        // Преобразовать координаты
        double[] xPoints = new double[points.size()];
        double[] yPoints = new double[points.size()];
        
        for (int i = 0; i < points.size(); i++) {
            try {
                xPoints[i] = Double.parseDouble(points.get(i).x());
                yPoints[i] = Double.parseDouble(points.get(i).y());
            } catch (NumberFormatException e) {
                return;
            }
        }
        
        // Отрисовать заливку полигона
        gc.setFill(BUILDING_FILL_COLOR);
        gc.fillPolygon(xPoints, yPoints, points.size());
        
        // Отрисовать контур
        gc.setStroke(BUILDING_STROKE_COLOR);
        gc.setLineWidth(BUILDING_STROKE_WIDTH / transform.getScale());
        gc.strokePolygon(xPoints, yPoints, points.size());
        
        // Отрисовать точки
        gc.setFill(POINT_COLOR);
        double pointRadius = POINT_RADIUS / transform.getScale();
        for (int i = 0; i < xPoints.length; i++) {
            gc.fillOval(
                xPoints[i] - pointRadius, 
                yPoints[i] - pointRadius, 
                pointRadius * 2, 
                pointRadius * 2
            );
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
        gc.setFont(Font.font("System", FontWeight.BOLD, 16 / transform.getScale()));
        
        // Центрировать текст
        String text = building.litera();
        gc.fillText(text, centerX - (text.length() * 4) / transform.getScale(), centerY + 4 / transform.getScale());
    }
    
    /**
     * Отрисовать информацию о масштабе и трансформации.
     */
    private void drawScaleInfo(Bounds bounds) {
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
        
        String boundsInfo = String.format("Диапазон: X[%.1f..%.1f], Y[%.1f..%.1f]", 
                                         bounds.minX, bounds.maxX, bounds.minY, bounds.maxY);
        gc.fillText(boundsInfo, 10, canvas.getHeight() - 20);
        
        String zoomInfo = String.format("Масштаб: %s", transform.getScalePercent());
        gc.fillText(zoomInfo, 10, canvas.getHeight() - 5);
    }
    
    /**
     * Вернуть границы всех зданий (публичный метод).
     */
    public Bounds getBounds(List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        return calculateBounds(buildings);
    }
    
    /**
     * Класс для хранения границ координат.
     */
    public static class Bounds {
        public final double minX;
        public final double maxX;
        public final double minY;
        public final double maxY;
        
        public Bounds(double minX, double maxX, double minY, double maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
}
