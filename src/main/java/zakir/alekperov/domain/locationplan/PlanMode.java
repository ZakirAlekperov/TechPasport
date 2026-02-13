package zakir.alekperov.domain.locationplan;

/**
 * Режим работы с ситуационным планом.
 * Enum для явного определения типа плана.
 * 
 * Архитектурное решение:
 * - Явное перечисление без magic strings
 * - Два взаимоисключающих режима работы
 * - Проверка режима через equals, не через instanceof
 */
public enum PlanMode {
    
    /**
     * Ручное рисование плана с указанием координат зданий.
     * Требует: масштаб, исполнитель, координаты точек.
     */
    MANUAL_DRAWING,
    
    /**
     * Загруженное изображение готового плана.
     * Требует: файл изображения (PNG, JPG, JPEG).
     */
    UPLOADED_IMAGE;
    
    /**
     * Проверить, является ли режим ручным рисованием.
     */
    public boolean isManualDrawing() {
        return this == MANUAL_DRAWING;
    }
    
    /**
     * Проверить, является ли режим загруженным изображением.
     */
    public boolean isUploadedImage() {
        return this == UPLOADED_IMAGE;
    }
}
