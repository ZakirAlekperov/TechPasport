package zakir.alekperov.ui.visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import zakir.alekperov.application.locationplan.LocationPlanDTO;

import java.util.List;

/**
 * Класс для визуализации зданий на Canvas с поддержкой выделения и hover.
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
    private static final Color HOVER_STROKE_COLOR = Color.rgb(255, 152, 0);
    private static final Color HOVER_FILL_COLOR = Color.rgb(255, 152, 0, 0.2);
    private static final Color SELECTED_STROKE_COLOR = Color.rgb(76, 175, 80);
    private static final Color SELECTED_FILL_COLOR = Color.rgb(76, 175, 80, 0.3);
    private static final Color POINT_COLOR = Color.rgb(255, 87, 34);
    private static final Color TEXT_COLOR = Color.rgb(33, 33, 33);
    
    // Параметры отрисовки
    private static final double PADDING = 40.0;
    private static final double POINT_RADIUS = 4.0;
    private static final double BUILDING_STROKE_WIDTH = 2.0;
    private static final double HOVER_STROKE_WIDTH = 3.0;
    private static final double SELECTED_STROKE_WIDTH = 3.5;
    
    // Состояние выделения
    private String selectedBuildingLitera = null;
    private String hoveredBuildingLitera = null;
    
    public BuildingVisualizer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.transform = new CanvasTransform();
    }
    
    public CanvasTransform getTransform() {
        return transform;
    }
    
    /**
     * Установить выделенное здание.
     */
    public void setSelectedBuilding(String litera) {
        this.selectedBuildingLitera = litera;
    }
    
    /**
     * Получить литеру выделенного здания.
     */
    public String getSelectedBuilding() {
        return selectedBuildingLitera;
    }
    
    /**
     * Снять выделение.
     */
    public void clearSelection() {
        this.selectedBuildingLitera = null;
    }
    
    /**
     * Установить здание под курсором (hover).
     */
    public void setHoveredBuilding(String litera) {
        this.hoveredBuildingLitera = litera;
    }
    
    /**
     * Найти здание в точке (в координатах Canvas).
     * @param canvasX X координата в Canvas
     * @param canvasY Y координата в Canvas
     * @param buildings Список зданий
     * @return Литера найденного здания или null
     */
    public String findBuildingAt(double canvasX, double canvasY, List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        if (buildings == null || buildings.isEmpty()) {
            return null;
        }
        
        // Преобразовать координаты Canvas в мировые
        double[] worldCoords = transform.canvasToWorld(canvasX, canvasY);
        double worldX = worldCoords[0];
        double worldY = worldCoords[1];
        
        // Проверить каждое здание (в обратном порядке, чтобы найти верхнее)
        for (int i = buildings.size() - 1; i >= 0; i--) {
            LocationPlanDTO.BuildingCoordinatesDTO building = buildings.get(i);
            if (isPointInBuilding(worldX, worldY, building)) {
                return building.litera();
            }
        }
        
        return null;
    }
    
    /**
     * Проверить, находится ли точка внутри здания (Ray casting algorithm).
     */
    private boolean isPointInBuilding(double x, double y, LocationPlanDTO.BuildingCoordinatesDTO building) {
        List<LocationPlanDTO.CoordinatePointDTO> points = building.points();
        if (points.size() < 3) {
            return false;
        }
        
        try {
            int n = points.size();
            boolean inside = false;
            
            double x1 = Double.parseDouble(points.get(n - 1).x());
            double y1 = Double.parseDouble(points.get(n - 1).y());
            
            for (int i = 0; i < n; i++) {
                double x2 = Double.parseDouble(points.get(i).x());
                double y2 = Double.parseDouble(points.get(i).y());
                
                if ((y2 > y) != (y1 > y)) {
                    double slope = (x2 - x1) / (y2 - y1);
                    if (x < slope * (y - y1) + x1) {
                        inside = !inside;
                    }
                }
                
                x1 = x2;
                y1 = y2;
            }
            
            return inside;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Отрисовать все здания.
     */
    public void draw(List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        clearCanvas();
        
        if (buildings == null || buildings.isEmpty()) {
            return;
        }
        
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
        
        gc.save();
        transform.apply(gc);
        
        drawGrid(bounds);
        
        // Сначала отрисовать обычные здания
        for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
            String litera = building.litera();
            if (!litera.equals(selectedBuildingLitera) && !litera.equals(hoveredBuildingLitera)) {
                drawBuilding(building, BuildingState.NORMAL);
            }
        }
        
        // Затем здание под курсором
        if (hoveredBuildingLitera != null) {
            for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
                if (building.litera().equals(hoveredBuildingLitera)) {
                    drawBuilding(building, BuildingState.HOVERED);
                    break;
                }
            }
        }
        
        // И наконец выделенное здание (поверх всех)
        if (selectedBuildingLitera != null) {
            for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
                if (building.litera().equals(selectedBuildingLitera)) {
                    drawBuilding(building, BuildingState.SELECTED);
                    break;
                }
            }
        }
        
        gc.restore();
        drawScaleInfo(bounds);
    }
    
    private void clearCanvas() {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
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
    
    private void drawGrid(Bounds bounds) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5 / transform.getScale());
        
        double gridSize = 10.0;
        
        for (double x = Math.floor(bounds.minX / gridSize) * gridSize; x <= bounds.maxX; x += gridSize) {
            gc.strokeLine(x, bounds.minY, x, bounds.maxY);
        }
        
        for (double y = Math.floor(bounds.minY / gridSize) * gridSize; y <= bounds.maxY; y += gridSize) {
            gc.strokeLine(bounds.minX, y, bounds.maxX, y);
        }
    }
    
    /**
     * Отрисовать здание с учётом его состояния.
     */
    private void drawBuilding(LocationPlanDTO.BuildingCoordinatesDTO building, BuildingState state) {
        List<LocationPlanDTO.CoordinatePointDTO> points = building.points();
        
        if (points.isEmpty()) {
            return;
        }
        
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
        
        // Выбрать цвета в зависимости от состояния
        Color fillColor, strokeColor;
        double strokeWidth;
        
        switch (state) {
            case HOVERED:
                fillColor = HOVER_FILL_COLOR;
                strokeColor = HOVER_STROKE_COLOR;
                strokeWidth = HOVER_STROKE_WIDTH;
                break;
            case SELECTED:
                fillColor = SELECTED_FILL_COLOR;
                strokeColor = SELECTED_STROKE_COLOR;
                strokeWidth = SELECTED_STROKE_WIDTH;
                break;
            default:
                fillColor = BUILDING_FILL_COLOR;
                strokeColor = BUILDING_STROKE_COLOR;
                strokeWidth = BUILDING_STROKE_WIDTH;
        }
        
        // Отрисовать заливку
        gc.setFill(fillColor);
        gc.fillPolygon(xPoints, yPoints, points.size());
        
        // Отрисовать контур
        gc.setStroke(strokeColor);
        gc.setLineWidth(strokeWidth / transform.getScale());
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
        
        // Отрисовать литеру
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
        
        String text = building.litera();
        gc.fillText(text, centerX - (text.length() * 4) / transform.getScale(), centerY + 4 / transform.getScale());
    }
    
    private void drawScaleInfo(Bounds bounds) {
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
        
        String boundsInfo = String.format("Диапазон: X[%.1f..%.1f], Y[%.1f..%.1f]", 
                                         bounds.minX, bounds.maxX, bounds.minY, bounds.maxY);
        gc.fillText(boundsInfo, 10, canvas.getHeight() - 20);
        
        String zoomInfo = String.format("Масштаб: %s", transform.getScalePercent());
        gc.fillText(zoomInfo, 10, canvas.getHeight() - 5);
    }
    
    public Bounds getBounds(List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        return calculateBounds(buildings);
    }
    
    /**
     * Состояние здания для отрисовки.
     */
    private enum BuildingState {
        NORMAL,
        HOVERED,
        SELECTED
    }
    
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
