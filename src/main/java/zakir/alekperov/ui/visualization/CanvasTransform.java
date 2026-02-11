package zakir.alekperov.ui.visualization;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

/**
 * Управление трансформациями Canvas (zoom, pan).
 * Хранит текущий масштаб и смещение, применяет их к GraphicsContext.
 */
public class CanvasTransform {
    
    // Текущий масштаб (1.0 = 100%)
    private double scale = 1.0;
    
    // Смещение по X и Y
    private double translateX = 0.0;
    private double translateY = 0.0;
    
    // Ограничения масштаба
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 10.0;
    
    // Шаг изменения масштаба
    private static final double SCALE_DELTA = 0.1;
    
    /**
     * Применить текущие трансформации к GraphicsContext.
     */
    public void apply(GraphicsContext gc) {
        Affine transform = new Affine();
        transform.appendTranslation(translateX, translateY);
        transform.appendScale(scale, scale);
        gc.setTransform(transform);
    }
    
    /**
     * Сбросить трансформации.
     */
    public void reset() {
        scale = 1.0;
        translateX = 0.0;
        translateY = 0.0;
    }
    
    /**
     * Увеличить масштаб с центром в точке (centerX, centerY).
     */
    public void zoomIn(double centerX, double centerY) {
        zoom(centerX, centerY, 1.0 + SCALE_DELTA);
    }
    
    /**
     * Уменьшить масштаб с центром в точке (centerX, centerY).
     */
    public void zoomOut(double centerX, double centerY) {
        zoom(centerX, centerY, 1.0 - SCALE_DELTA);
    }
    
    /**
     * Изменить масштаб с заданным фактором относительно точки (centerX, centerY).
     * 
     * @param centerX X координата центра масштабирования (в координатах Canvas)
     * @param centerY Y координата центра масштабирования (в координатах Canvas)
     * @param factor Множитель масштаба (> 1 для увеличения, < 1 для уменьшения)
     */
    public void zoom(double centerX, double centerY, double factor) {
        double oldScale = scale;
        double newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale * factor));
        
        if (newScale == oldScale) {
            return; // Достигнут предел масштабирования
        }
        
        // Пересчитываем смещение так, чтобы точка под курсором оставалась на месте
        // Формула: новое_смещение = центр - (центр - старое_смещение) * (новый_масштаб / старый_масштаб)
        double scaleFactor = newScale / oldScale;
        translateX = centerX - (centerX - translateX) * scaleFactor;
        translateY = centerY - (centerY - translateY) * scaleFactor;
        
        scale = newScale;
    }
    
    /**
     * Изменить масштаб на основе прокрутки колеса мыши.
     * 
     * @param centerX X координата центра масштабирования
     * @param centerY Y координата центра масштабирования
     * @param deltaY Значение прокрутки (положительное - вверх, отрицательное - вниз)
     */
    public void zoomByScroll(double centerX, double centerY, double deltaY) {
        // deltaY > 0 означает прокрутку вверх (zoom in)
        // deltaY < 0 означает прокрутку вниз (zoom out)
        double factor = deltaY > 0 ? 1.1 : 0.9;
        zoom(centerX, centerY, factor);
    }
    
    /**
     * Переместить Canvas на заданное расстояние.
     */
    public void pan(double dx, double dy) {
        translateX += dx;
        translateY += dy;
    }
    
    /**
     * Установить масштаб так, чтобы область с заданными границами поместилась в Canvas.
     * 
     * @param canvasWidth Ширина Canvas
     * @param canvasHeight Высота Canvas
     * @param minX Минимальная X координата области
     * @param minY Минимальная Y координата области
     * @param maxX Максимальная X координата области
     * @param maxY Максимальная Y координата области
     * @param padding Отступ от краёв (в пикселях)
     */
    public void fitBounds(double canvasWidth, double canvasHeight,
                         double minX, double minY, double maxX, double maxY,
                         double padding) {
        
        double contentWidth = maxX - minX;
        double contentHeight = maxY - minY;
        
        if (contentWidth <= 0 || contentHeight <= 0) {
            reset();
            return;
        }
        
        // Доступная область с учётом отступов
        double availableWidth = canvasWidth - 2 * padding;
        double availableHeight = canvasHeight - 2 * padding;
        
        // Вычисляем масштаб, чтобы вместить контент
        double scaleX = availableWidth / contentWidth;
        double scaleY = availableHeight / contentHeight;
        scale = Math.min(scaleX, scaleY);
        scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
        
        // Центрируем контент
        double scaledWidth = contentWidth * scale;
        double scaledHeight = contentHeight * scale;
        
        translateX = (canvasWidth - scaledWidth) / 2 - minX * scale;
        translateY = (canvasHeight - scaledHeight) / 2 - minY * scale;
    }
    
    /**
     * Преобразовать координаты Canvas в мировые координаты.
     */
    public double[] canvasToWorld(double canvasX, double canvasY) {
        double worldX = (canvasX - translateX) / scale;
        double worldY = (canvasY - translateY) / scale;
        return new double[]{worldX, worldY};
    }
    
    /**
     * Преобразовать мировые координаты в координаты Canvas.
     */
    public double[] worldToCanvas(double worldX, double worldY) {
        double canvasX = worldX * scale + translateX;
        double canvasY = worldY * scale + translateY;
        return new double[]{canvasX, canvasY};
    }
    
    // Геттеры
    public double getScale() {
        return scale;
    }
    
    public double getTranslateX() {
        return translateX;
    }
    
    public double getTranslateY() {
        return translateY;
    }
    
    /**
     * Получить информацию о текущем масштабе в процентах.
     */
    public String getScalePercent() {
        return String.format("%.0f%%", scale * 100);
    }
}
