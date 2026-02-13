package zakir.alekperov.domain.locationplan;

import zakir.alekperov.domain.shared.ValidationException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Изображение ситуационного плана.
 * Value Object для хранения данных загруженного изображения.
 * 
 * Инварианты:
 * - Данные изображения не могут быть null или пустыми
 * - Имя файла не может быть пустым
 * - Формат должен быть поддерживаемым (PNG, JPG, JPEG)
 * - Размер не должен превышать 10 МБ
 */
public final class PlanImage {
    
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10 МБ
    private static final String[] ALLOWED_FORMATS = {"PNG", "JPG", "JPEG"};
    
    private final byte[] imageData;
    private final String fileName;
    private final String format;
    
    /**
     * Создать изображение плана.
     * 
     * @param imageData байты изображения
     * @param fileName имя файла
     * @throws ValidationException если данные невалидны
     */
    public PlanImage(byte[] imageData, String fileName) {
        this.imageData = validateImageData(imageData);
        this.fileName = validateFileName(fileName);
        this.format = extractFormat(fileName);
    }
    
    public byte[] getImageData() {
        return Arrays.copyOf(imageData, imageData.length);
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getFormat() {
        return format;
    }
    
    public long getSizeBytes() {
        return imageData.length;
    }
    
    public double getSizeMB() {
        return imageData.length / (1024.0 * 1024.0);
    }
    
    private byte[] validateImageData(byte[] data) {
        if (data == null || data.length == 0) {
            throw new ValidationException("Данные изображения не могут быть пустыми");
        }
        
        if (data.length > MAX_SIZE_BYTES) {
            double sizeMB = data.length / (1024.0 * 1024.0);
            throw new ValidationException(
                String.format("Размер изображения превышает максимально допустимый (%.2f МБ > 10 МБ)", sizeMB)
            );
        }
        
        return Arrays.copyOf(data, data.length);
    }
    
    private String validateFileName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Имя файла не может быть пустым");
        }
        return name.trim();
    }
    
    private String extractFormat(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            throw new ValidationException("Файл должен иметь расширение (.png, .jpg, .jpeg)");
        }
        
        String format = fileName.substring(dotIndex + 1).toUpperCase();
        
        boolean isAllowed = false;
        for (String allowed : ALLOWED_FORMATS) {
            if (format.equals(allowed)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new ValidationException(
                "Неподдерживаемый формат изображения: " + format + ". " +
                "Поддерживаются: PNG, JPG, JPEG"
            );
        }
        
        return format;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanImage planImage = (PlanImage) o;
        return Arrays.equals(imageData, planImage.imageData) && 
               Objects.equals(fileName, planImage.fileName);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(fileName);
        result = 31 * result + Arrays.hashCode(imageData);
        return result;
    }
    
    @Override
    public String toString() {
        return "PlanImage{" +
               "fileName='" + fileName + '\'' +
               ", format='" + format + '\'' +
               ", size=" + String.format("%.2f MB", getSizeMB()) +
               '}';
    }
}
