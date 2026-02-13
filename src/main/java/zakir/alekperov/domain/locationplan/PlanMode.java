package zakir.alekperov.domain.locationplan;

/**
 * Режим работы с ситуационным планом.
 * Enum для явного определения типа плана.
 */
public enum PlanMode {
    /**
     * Ручное рисование плана с указанием координат зданий.
     */
    MANUAL_DRAWING,
    
    /**
     * Загруженное изображение готового плана.
     */
    UPLOADED_IMAGE
}
