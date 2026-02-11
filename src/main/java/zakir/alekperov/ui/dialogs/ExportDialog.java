package zakir.alekperov.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Optional;

/**
 * Диалог настройки экспорта ситуационного плана.
 */
public class ExportDialog extends Dialog<ExportDialog.ExportSettings> {
    
    private final ComboBox<String> formatComboBox;
    private final ComboBox<String> qualityComboBox;
    private final ComboBox<String> sizeComboBox;
    private final CheckBox legendCheckBox;
    private final CheckBox gridCheckBox;
    private final TextField filePathField;
    private final Button browseButton;
    
    public ExportDialog() {
        setTitle("Экспорт ситуационного плана");
        setHeaderText("Настройте параметры экспорта");
        
        // Создание элементов управления
        formatComboBox = new ComboBox<>();
        formatComboBox.getItems().addAll("PNG (рекомендуется)", "JPEG", "PDF");
        formatComboBox.setValue("PNG (рекомендуется)");
        
        qualityComboBox = new ComboBox<>();
        qualityComboBox.getItems().addAll(
            "Стандартное (72 DPI)",
            "Высокое (150 DPI)",
            "Печатное (300 DPI)",
            "Максимальное (600 DPI)"
        );
        qualityComboBox.setValue("Высокое (150 DPI)");
        
        sizeComboBox = new ComboBox<>();
        sizeComboBox.getItems().addAll(
            "Текущий размер",
            "A4 (210×297 мм)",
            "A3 (297×420 мм)",
            "A2 (420×594 мм)",
            "A1 (594×841 мм)"
        );
        sizeComboBox.setValue("A4 (210×297 мм)");
        
        legendCheckBox = new CheckBox("Добавить легенду (масштаб, координаты, дата)");
        legendCheckBox.setSelected(true);
        
        gridCheckBox = new CheckBox("Включить координатную сетку");
        gridCheckBox.setSelected(true);
        
        filePathField = new TextField();
        filePathField.setPromptText("Выберите файл для сохранения...");
        filePathField.setPrefWidth(300);
        
        browseButton = new Button("Обзор...");
        browseButton.setOnAction(e -> handleBrowse());
        
        // Компоновка
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        
        int row = 0;
        grid.add(new Label("Формат:"), 0, row);
        grid.add(formatComboBox, 1, row++);
        
        grid.add(new Label("Качество:"), 0, row);
        grid.add(qualityComboBox, 1, row++);
        
        grid.add(new Label("Размер:"), 0, row);
        grid.add(sizeComboBox, 1, row++);
        
        grid.add(new Label(""), 0, row);
        grid.add(legendCheckBox, 1, row++);
        
        grid.add(new Label(""), 0, row);
        grid.add(gridCheckBox, 1, row++);
        
        grid.add(new Label("Сохранить как:"), 0, row);
        VBox fileBox = new VBox(5, filePathField, browseButton);
        grid.add(fileBox, 1, row++);
        
        getDialogPane().setContent(grid);
        
        // Кнопки
        ButtonType exportButtonType = new ButtonType("Экспортировать", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(exportButtonType, ButtonType.CANCEL);
        
        // Валидация
        Button exportButton = (Button) getDialogPane().lookupButton(exportButtonType);
        exportButton.setDisable(true);
        
        filePathField.textProperty().addListener((obs, oldVal, newVal) -> {
            exportButton.setDisable(newVal == null || newVal.trim().isEmpty());
        });
        
        // Конвертер результата
        setResultConverter(dialogButton -> {
            if (dialogButton == exportButtonType) {
                return new ExportSettings(
                    new File(filePathField.getText()),
                    getFormatFromString(formatComboBox.getValue()),
                    getDpiFromString(qualityComboBox.getValue()),
                    sizeComboBox.getValue(),
                    legendCheckBox.isSelected(),
                    gridCheckBox.isSelected()
                );
            }
            return null;
        });
    }
    
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить ситуационный план");
        
        String format = formatComboBox.getValue();
        if (format.contains("PNG")) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG изображение", "*.png")
            );
            fileChooser.setInitialFileName("ситуационный_план.png");
        } else if (format.contains("JPEG")) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPEG изображение", "*.jpg", "*.jpeg")
            );
            fileChooser.setInitialFileName("ситуационный_план.jpg");
        } else if (format.contains("PDF")) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF документ", "*.pdf")
            );
            fileChooser.setInitialFileName("ситуационный_план.pdf");
        }
        
        File file = fileChooser.showSaveDialog(getDialogPane().getScene().getWindow());
        if (file != null) {
            filePathField.setText(file.getAbsolutePath());
        }
    }
    
    private ExportFormat getFormatFromString(String formatString) {
        if (formatString.contains("PNG")) return ExportFormat.PNG;
        if (formatString.contains("JPEG")) return ExportFormat.JPEG;
        if (formatString.contains("PDF")) return ExportFormat.PDF;
        return ExportFormat.PNG;
    }
    
    private int getDpiFromString(String qualityString) {
        if (qualityString.contains("72")) return 72;
        if (qualityString.contains("150")) return 150;
        if (qualityString.contains("300")) return 300;
        if (qualityString.contains("600")) return 600;
        return 150;
    }
    
    /**
     * Настройки экспорта.
     */
    public static class ExportSettings {
        private final File file;
        private final ExportFormat format;
        private final int dpi;
        private final String size;
        private final boolean includeLegend;
        private final boolean includeGrid;
        
        public ExportSettings(File file, ExportFormat format, int dpi, String size, 
                            boolean includeLegend, boolean includeGrid) {
            this.file = file;
            this.format = format;
            this.dpi = dpi;
            this.size = size;
            this.includeLegend = includeLegend;
            this.includeGrid = includeGrid;
        }
        
        public File getFile() { return file; }
        public ExportFormat getFormat() { return format; }
        public int getDpi() { return dpi; }
        public String getSize() { return size; }
        public boolean isIncludeLegend() { return includeLegend; }
        public boolean isIncludeGrid() { return includeGrid; }
    }
    
    /**
     * Формат экспорта.
     */
    public enum ExportFormat {
        PNG, JPEG, PDF
    }
}
