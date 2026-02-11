package zakir.alekperov.ui.visualization;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import zakir.alekperov.application.locationplan.LocationPlanDTO;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Класс для визуализации зданий с поддержкой реальных геодезических координат (МСК-67).
 * Автоматически вычисляет локальное смещение для корректного отображения.
 */
public class BuildingVisualizer {
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final CanvasTransform transform;
    private final MeasurementTool measurementTool;
    private final DecimalFormat coordinateFormat = new DecimalFormat("#,##0.00");
    
    // Локальное смещение для работы с большими координатами
    private double originX = 0.0;
    private double originY = 0.0;
    
    // Цвета
    private static final Color BACKGROUND_COLOR = Color.rgb(250, 250, 250);
    private static final Color GRID_MAJOR_COLOR = Color.rgb(180, 180, 180);
    private static final Color GRID_MINOR_COLOR = Color.rgb(220, 220, 220);
    private static final Color GRID_TEXT_COLOR = Color.rgb(100, 100, 100);
    private static final Color BUILDING_STROKE_COLOR = Color.rgb(33, 150, 243);
    private static final Color BUILDING_FILL_COLOR = Color.rgb(33, 150, 243, 0.1);
    private static final Color HOVER_STROKE_COLOR = Color.rgb(255, 152, 0);
    private static final Color HOVER_FILL_COLOR = Color.rgb(255, 152, 0, 0.2);
    private static final Color SELECTED_STROKE_COLOR = Color.rgb(76, 175, 80);
    private static final Color SELECTED_FILL_COLOR = Color.rgb(76, 175, 80, 0.3);
    private static final Color POINT_COLOR = Color.rgb(255, 87, 34);
    private static final Color POINT_HOVER_COLOR = Color.rgb(255, 193, 7);
    private static final Color POINT_DRAG_COLOR = Color.rgb(33, 150, 243);
    private static final Color TEXT_COLOR = Color.rgb(33, 33, 33);
    
    // Параметры отрисовки
    private static final double PADDING = 40.0;
    private static final double POINT_RADIUS = 4.0;
    private static final double POINT_HOVER_RADIUS = 6.0;
    private static final double POINT_DRAG_RADIUS = 8.0;
    private static final double POINT_CLICK_THRESHOLD = 15.0;
    private static final double BUILDING_STROKE_WIDTH = 2.0;
    private static final double HOVER_STROKE_WIDTH = 3.0;
    private static final double SELECTED_STROKE_WIDTH = 3.5;
    
    // Настройки сетки
    private boolean gridVisible = true;
    private double gridSize = 10.0;
    
    // Состояние выделения
    private String selectedBuildingLitera = null;
    private String hoveredBuildingLitera = null;
    
    // Состояние редактирования точек
    private PointHandle hoveredPoint = null;
    private PointHandle draggingPoint = null;
    
    public BuildingVisualizer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.transform = new CanvasTransform();
        this.measurementTool = new MeasurementTool();
    }
    
    public CanvasTransform getTransform() {
        return transform;
    }
    
    public MeasurementTool getMeasurementTool() {
        return measurementTool;
    }
    
    public double getOriginX() {
        return originX;
    }
    
    public double getOriginY() {
        return originY;
    }
    
    // Управление сеткой
    public void setGridVisible(boolean visible) {
        this.gridVisible = visible;
    }
    
    public boolean isGridVisible() {
        return gridVisible;
    }
    
    public void setGridSize(double size) {
        this.gridSize = Math.max(1.0, size);
    }
    
    public double getGridSize() {
        return gridSize;
    }
    
    public void setSelectedBuilding(String litera) {
        this.selectedBuildingLitera = litera;
    }
    
    public String getSelectedBuilding() {
        return selectedBuildingLitera;
    }
    
    public void clearSelection() {
        this.selectedBuildingLitera = null;
    }
    
    public void setHoveredBuilding(String litera) {
        this.hoveredBuildingLitera = litera;
    }
    
    public void setHoveredPoint(PointHandle point) {
        this.hoveredPoint = point;
    }
    
    public PointHandle getHoveredPoint() {
        return hoveredPoint;
    }
    
    public void startDraggingPoint(PointHandle point) {
        this.draggingPoint = point;
    }
    
    public PointHandle stopDraggingPoint() {
        PointHandle point = this.draggingPoint;
        this.draggingPoint = null;
        return point;
    }
    
    public PointHandle getDraggingPoint() {
        return draggingPoint;
    }
    
    public boolean isDraggingPoint() {
        return draggingPoint != null;
    }
    
    public void updateDraggingPoint(double canvasX, double canvasY) {
        if (draggingPoint != null) {
            double[] worldCoords = transform.canvasToWorld(canvasX, canvasY);
            // Добавляем смещение origin для получения реальных координат
            draggingPoint.worldX = worldCoords[0] + originX;
            draggingPoint.worldY = worldCoords[1] + originY;
        }
    }
    
    public PointHandle findPointAt(double canvasX, double canvasY, List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        if (buildings == null || buildings.isEmpty()) {
            return null;
        }
        
        if (selectedBuildingLitera == null) {
            return null;
        }
        
        LocationPlanDTO.BuildingCoordinatesDTO selectedBuilding = null;
        for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
            if (building.litera().equals(selectedBuildingLitera)) {
                selectedBuilding = building;
                break;
            }
        }
        
        if (selectedBuilding == null) {
            return null;
        }
        
        List<LocationPlanDTO.CoordinatePointDTO> points = selectedBuilding.points();
        double minDistance = Double.MAX_VALUE;
        PointHandle closestPoint = null;
        
        for (int i = 0; i < points.size(); i++) {
            try {
                double worldX = Double.parseDouble(points.get(i).x());
                double worldY = Double.parseDouble(points.get(i).y());
                
                // Преобразуем реальные координаты в локальные
                double localX = worldX - originX;
                double localY = worldY - originY;
                
                double[] canvasCoords = transform.worldToCanvas(localX, localY);
                double distance = Math.sqrt(
                    Math.pow(canvasCoords[0] - canvasX, 2) + 
                    Math.pow(canvasCoords[1] - canvasY, 2)
                );
                
                if (distance < POINT_CLICK_THRESHOLD && distance < minDistance) {
                    minDistance = distance;
                    closestPoint = new PointHandle(selectedBuildingLitera, i, worldX, worldY);
                }
            } catch (NumberFormatException e) {
                // Пропустить
            }
        }
        
        return closestPoint;
    }
    
    public String findBuildingAt(double canvasX, double canvasY, List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        if (buildings == null || buildings.isEmpty()) {
            return null;
        }
        
        double[] worldCoords = transform.canvasToWorld(canvasX, canvasY);
        // Добавляем смещение для получения реальных координат
        double worldX = worldCoords[0] + originX;
        double worldY = worldCoords[1] + originY;
        
        for (int i = buildings.size() - 1; i >= 0; i--) {
            LocationPlanDTO.BuildingCoordinatesDTO building = buildings.get(i);
            if (isPointInBuilding(worldX, worldY, building)) {
                return building.litera();
            }
        }
        
        return null;
    }
    
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
    
    public void draw(List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        clearCanvas();
        
        if (buildings == null || buildings.isEmpty()) {
            return;
        }
        
        Bounds bounds = calculateBounds(buildings);
        if (bounds == null) {
            return;
        }
        
        // Вычисляем локальное смещение (origin) для работы с большими координатами
        originX = bounds.minX;
        originY = bounds.minY;
        
        // Синхронизируем origin с MeasurementTool
        measurementTool.setOrigin(originX, originY);
        
        // Создаем локальные границы (относительно origin)
        double localWidth = bounds.maxX - bounds.minX;
        double localHeight = bounds.maxY - bounds.minY;
        
        if (transform.getScale() == 1.0 && transform.getTranslateX() == 0.0 && transform.getTranslateY() == 0.0) {
            transform.fitBounds(
                canvas.getWidth(), 
                canvas.getHeight(), 
                0, 0,  // Локальные координаты начинаются с 0
                localWidth, 
                localHeight, 
                PADDING
            );
        }
        
        gc.save();
        transform.apply(gc);
        
        if (gridVisible) {
            drawGrid(bounds);
        }
        
        // Отрисовать здания
        for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
            String litera = building.litera();
            if (!litera.equals(selectedBuildingLitera) && !litera.equals(hoveredBuildingLitera)) {
                drawBuilding(building, BuildingState.NORMAL);
            }
        }
        
        if (hoveredBuildingLitera != null && !hoveredBuildingLitera.equals(selectedBuildingLitera)) {
            for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
                if (building.litera().equals(hoveredBuildingLitera)) {
                    drawBuilding(building, BuildingState.HOVERED);
                    break;
                }
            }
        }
        
        if (selectedBuildingLitera != null) {
            for (LocationPlanDTO.BuildingCoordinatesDTO building : buildings) {
                if (building.litera().equals(selectedBuildingLitera)) {
                    drawBuilding(building, BuildingState.SELECTED);
                    break;
                }
            }
        }
        
        // Отрисовать измерения
        measurementTool.draw(gc, transform);
        
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
        // Работаем в локальных координатах (относительно origin)
        double localMinX = 0;
        double localMaxX = bounds.maxX - bounds.minX;
        double localMinY = 0;
        double localMaxY = bounds.maxY - bounds.minY;
        
        double startX = Math.floor(localMinX / gridSize) * gridSize;
        double endX = Math.ceil(localMaxX / gridSize) * gridSize;
        double startY = Math.floor(localMinY / gridSize) * gridSize;
        double endY = Math.ceil(localMaxY / gridSize) * gridSize;
        
        double fontSize = 8.0 / transform.getScale();
        gc.setFont(Font.font("System", FontWeight.NORMAL, fontSize));
        gc.setFill(GRID_TEXT_COLOR);
        
        // Вертикальные линии
        for (double localX = startX; localX <= endX; localX += gridSize) {
            boolean isMajor = (Math.abs(localX) % (gridSize * 5) < 0.01);
            
            gc.setStroke(isMajor ? GRID_MAJOR_COLOR : GRID_MINOR_COLOR);
            gc.setLineWidth((isMajor ? 1.0 : 0.5) / transform.getScale());
            gc.strokeLine(localX, localMinY, localX, localMaxY);
            
            if (isMajor) {
                // Показываем РЕАЛЬНЫЕ координаты (с учетом origin)
                double realX = localX + originX;
                String label = coordinateFormat.format(realX);
                gc.fillText(label, localX + 1 / transform.getScale(), localMinY + 10 / transform.getScale());
            }
        }
        
        // Горизонтальные линии
        for (double localY = startY; localY <= endY; localY += gridSize) {
            boolean isMajor = (Math.abs(localY) % (gridSize * 5) < 0.01);
            
            gc.setStroke(isMajor ? GRID_MAJOR_COLOR : GRID_MINOR_COLOR);
            gc.setLineWidth((isMajor ? 1.0 : 0.5) / transform.getScale());
            gc.strokeLine(localMinX, localY, localMaxX, localY);
            
            if (isMajor) {
                // Показываем РЕАЛЬНЫЕ координаты (с учетом origin)
                double realY = localY + originY;
                String label = coordinateFormat.format(realY);
                gc.fillText(label, localMinX + 1 / transform.getScale(), localY - 2 / transform.getScale());
            }
        }
    }
    
    private void drawBuilding(LocationPlanDTO.BuildingCoordinatesDTO building, BuildingState state) {
        List<LocationPlanDTO.CoordinatePointDTO> points = building.points();
        
        if (points.isEmpty()) {
            return;
        }
        
        double[] xPoints = new double[points.size()];
        double[] yPoints = new double[points.size()];
        
        for (int i = 0; i < points.size(); i++) {
            try {
                double realX, realY;
                
                if (draggingPoint != null && 
                    draggingPoint.buildingLitera.equals(building.litera()) && 
                    draggingPoint.pointIndex == i) {
                    realX = draggingPoint.worldX;
                    realY = draggingPoint.worldY;
                } else {
                    realX = Double.parseDouble(points.get(i).x());
                    realY = Double.parseDouble(points.get(i).y());
                }
                
                // Преобразуем в локальные координаты
                xPoints[i] = realX - originX;
                yPoints[i] = realY - originY;
            } catch (NumberFormatException e) {
                return;
            }
        }
        
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
        
        gc.setFill(fillColor);
        gc.fillPolygon(xPoints, yPoints, points.size());
        
        gc.setStroke(strokeColor);
        gc.setLineWidth(strokeWidth / transform.getScale());
        gc.strokePolygon(xPoints, yPoints, points.size());
        
        for (int i = 0; i < xPoints.length; i++) {
            Color pointColor = POINT_COLOR;
            double pointRadius = POINT_RADIUS / transform.getScale();
            
            if (draggingPoint != null && 
                draggingPoint.buildingLitera.equals(building.litera()) && 
                draggingPoint.pointIndex == i) {
                pointColor = POINT_DRAG_COLOR;
                pointRadius = POINT_DRAG_RADIUS / transform.getScale();
            }
            else if (hoveredPoint != null && 
                     hoveredPoint.buildingLitera.equals(building.litera()) && 
                     hoveredPoint.pointIndex == i) {
                pointColor = POINT_HOVER_COLOR;
                pointRadius = POINT_HOVER_RADIUS / transform.getScale();
            }
            
            gc.setFill(pointColor);
            gc.fillOval(
                xPoints[i] - pointRadius, 
                yPoints[i] - pointRadius, 
                pointRadius * 2, 
                pointRadius * 2
            );
        }
        
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
        
        String boundsInfo = String.format("МСК-67: X[%s..%s], Y[%s..%s]", 
            coordinateFormat.format(bounds.minX),
            coordinateFormat.format(bounds.maxX),
            coordinateFormat.format(bounds.minY),
            coordinateFormat.format(bounds.maxY));
        gc.fillText(boundsInfo, 10, canvas.getHeight() - 20);
        
        String zoomInfo = String.format("Масштаб: %s | Сетка: %s (шаг %.0fм) | Измерение: %s", 
                                       transform.getScalePercent(), 
                                       gridVisible ? "ВКЛ" : "ВЫКЛ",
                                       gridSize,
                                       measurementTool.isActive() ? "ВКЛ" : "ВЫКЛ");
        gc.fillText(zoomInfo, 10, canvas.getHeight() - 5);
    }
    
    public Bounds getBounds(List<LocationPlanDTO.BuildingCoordinatesDTO> buildings) {
        return calculateBounds(buildings);
    }
    
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
    
    public static class PointHandle {
        public final String buildingLitera;
        public final int pointIndex;
        public double worldX;  // Реальные координаты МСК-67
        public double worldY;  // Реальные координаты МСК-67
        
        public PointHandle(String buildingLitera, int pointIndex, double worldX, double worldY) {
            this.buildingLitera = buildingLitera;
            this.pointIndex = pointIndex;
            this.worldX = worldX;
            this.worldY = worldY;
        }
    }
}
