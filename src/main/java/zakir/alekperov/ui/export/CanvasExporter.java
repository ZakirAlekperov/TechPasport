package zakir.alekperov.ui.export;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Transform;
import zakir.alekperov.ui.dialogs.ExportDialog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Экспорт Canvas в изображение с легендой и правильным масштабом.
 */
public class CanvasExporter {
    
    private static final int LEGEND_HEIGHT = 120;
    private static final int MARGIN = 20;
    
    // Максимальный размер изображения (пиксели)
    // Ограничение для предотвращения OutOfMemoryError
    private static final int MAX_DIMENSION = 4096;
    
    /**
     * Экспортировать Canvas с настройками.
     */
    public static void export(Canvas canvas, ExportDialog.ExportSettings settings,
                            String coordinateSystem, String scaleDenominator) throws IOException {
        
        // Рассчитать размеры
        double[] dimensions = calculateDimensions(settings.getSize(), settings.getDpi());
        double targetWidth = dimensions[0];
        double targetHeight = dimensions[1];
        
        // Увеличить высоту для легенды
        if (settings.isIncludeLegend()) {
            targetHeight += LEGEND_HEIGHT;
        }
        
        // Применить ограничение размера
        boolean wasLimited = false;
        double maxDim = Math.max(targetWidth, targetHeight);
        if (maxDim > MAX_DIMENSION) {
            double scale = MAX_DIMENSION / maxDim;
            targetWidth *= scale;
            targetHeight *= scale;
            wasLimited = true;
            System.out.println("⚠️ Размер изображения уменьшен до " + 
                (int)targetWidth + "×" + (int)targetHeight + 
                " для предотвращения ошибки памяти");
        }
        
        // Создать новый Canvas с нужным размером
        Canvas exportCanvas = new Canvas(targetWidth, targetHeight);
        GraphicsContext gc = exportCanvas.getGraphicsContext2D();
        
        // Белый фон
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, targetWidth, targetHeight);
        
        // Скопировать содержимое оригинального Canvas
        double scale = Math.min(
            targetWidth / canvas.getWidth(),
            (targetHeight - (settings.isIncludeLegend() ? LEGEND_HEIGHT : 0)) / canvas.getHeight()
        );
        
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(Transform.scale(scale, scale));
        params.setFill(Color.TRANSPARENT);
        
        WritableImage canvasSnapshot = canvas.snapshot(params, null);
        gc.drawImage(canvasSnapshot, 
            (targetWidth - canvasSnapshot.getWidth()) / 2, 
            MARGIN);
        
        // Добавить легенду
        if (settings.isIncludeLegend()) {
            drawLegend(gc, targetWidth, targetHeight, coordinateSystem, scaleDenominator);
        }
        
        // Создать снимок
        WritableImage finalImage = exportCanvas.snapshot(new SnapshotParameters(), null);
        
        // Сохранить
        saveImage(finalImage, settings.getFile(), settings.getFormat());
        
        String message = "✓ Ситуационный план экспортирован: " + settings.getFile().getAbsolutePath();
        if (wasLimited) {
            message += "\n  Размер был автоматически уменьшен до " + 
                (int)targetWidth + "×" + (int)targetHeight + " пикселей";
        }
        System.out.println(message);
    }
    
    /**
     * Рассчитать размеры изображения в пикселях.
     */
    private static double[] calculateDimensions(String sizeString, int dpi) {
        double widthMm, heightMm;
        
        if (sizeString.contains("A4")) {
            widthMm = 210;
            heightMm = 297;
        } else if (sizeString.contains("A3")) {
            widthMm = 297;
            heightMm = 420;
        } else if (sizeString.contains("A2")) {
            widthMm = 420;
            heightMm = 594;
        } else if (sizeString.contains("A1")) {
            widthMm = 594;
            heightMm = 841;
        } else {
            // Текущий размер - вернем стандартный
            widthMm = 210;
            heightMm = 297;
        }
        
        // Конвертация мм в пиксели: 1 inch = 25.4 mm
        double widthPx = (widthMm / 25.4) * dpi;
        double heightPx = (heightMm / 25.4) * dpi;
        
        return new double[]{widthPx, heightPx};
    }
    
    /**
     * Нарисовать легенду внизу изображения.
     */
    private static void drawLegend(GraphicsContext gc, double width, double height,
                                  String coordinateSystem, String scaleDenominator) {
        
        double legendY = height - LEGEND_HEIGHT + 10;
        
        // Разделительная линия
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);
        gc.strokeLine(MARGIN, legendY - 5, width - MARGIN, legendY - 5);
        
        // Заголовок
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText("СИТУАЦИОННЫЙ ПЛАН", MARGIN, legendY + 15);
        
        // Информация
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        int lineHeight = 18;
        int currentLine = 1;
        
        // Масштаб
        if (scaleDenominator != null && !scaleDenominator.isBlank()) {
            gc.fillText("Масштаб: 1:" + scaleDenominator, MARGIN, legendY + 15 + lineHeight * currentLine++);
        }
        
        // Система координат
        if (coordinateSystem != null && !coordinateSystem.isBlank()) {
            gc.fillText("Система координат: " + coordinateSystem, MARGIN, legendY + 15 + lineHeight * currentLine++);
        }
        
        // Дата
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        gc.fillText("Дата: " + date, MARGIN, legendY + 15 + lineHeight * currentLine++);
        
        // Примечание
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
        gc.setFill(Color.GRAY);
        gc.fillText("Создано в системе TechPasport", 
            width - 200, height - 10);
    }
    
    /**
     * Сохранить изображение в файл.
     */
    private static void saveImage(WritableImage image, File file, ExportDialog.ExportFormat format) 
            throws IOException {
        
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        
        switch (format) {
            case PNG:
                ImageIO.write(bufferedImage, "png", file);
                break;
                
            case JPEG:
                // Для JPEG нужно убрать прозрачность
                BufferedImage rgbImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
                );
                rgbImage.getGraphics().drawImage(bufferedImage, 0, 0, java.awt.Color.WHITE, null);
                ImageIO.write(rgbImage, "jpg", file);
                break;
                
            case PDF:
                // PDF экспорт требует дополнительной библиотеки (iText или PDFBox)
                // Пока сохраним как PNG
                System.out.println("⚠️ PDF экспорт еще не реализован, сохраняю как PNG");
                ImageIO.write(bufferedImage, "png", new File(file.getAbsolutePath().replace(".pdf", ".png")));
                break;
        }
    }
}
