package zakir.alekperov.ui.tabs.locationplan;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.io.File;
import java.time.LocalDate;

/**
 * Контроллер вкладки "Ситуационный план".
 */
public class LocationPlanTabController extends BaseTabController {
    
    @FXML
    private TextField scaleField;
    
    @FXML
    private DatePicker creationDatePicker;
    
    @FXML
    private TextField authorField;
    
    @FXML
    private ImageView planImageView;
    
    @FXML
    private Label placeholderLabel;
    
    @FXML
    private Label imageInfoLabel;
    
    @FXML
    private TextArea notesArea;
    
    private File currentImageFile;
    
    @Override
    protected void setupBindings() {
        // Показ/скрытие плейсхолдера
        planImageView.imageProperty().addListener((obs, oldImage, newImage) -> {
            placeholderLabel.setVisible(newImage == null);
        });
    }
    
    @Override
    protected void loadInitialData() {
        creationDatePicker.setValue(LocalDate.now());
        placeholderLabel.setVisible(true);
    }
    
    @Override
    public boolean validateData() {
        if (scaleField.getText().isBlank()) {
            showWarning("Укажите масштаб плана");
            return false;
        }
        return true;
    }
    
    @Override
    public void saveData() {
        if (validateData()) {
            System.out.println("Сохранение ситуационного плана...");
            System.out.println("Масштаб: " + scaleField.getText());
            if (currentImageFile != null) {
                System.out.println("Файл: " + currentImageFile.getAbsolutePath());
            }
            showInfo("Ситуационный план сохранен успешно");
        }
    }
    
    @Override
    public void clearData() {
        scaleField.clear();
        creationDatePicker.setValue(null);
        authorField.clear();
        notesArea.clear();
        planImageView.setImage(null);
        currentImageFile = null;
        imageInfoLabel.setText("Изображение не загружено");
    }
    
    // Обработчики событий
    
    @FXML
    private void handleLoadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение ситуационного плана");
        
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(planImageView.getScene().getWindow());
        
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                planImageView.setImage(image);
                currentImageFile = selectedFile;
                
                String fileName = selectedFile.getName();
                long fileSize = selectedFile.length() / 1024; // KB
                imageInfoLabel.setText(String.format("%s (%.0f KB, %.0f×%.0f px)", 
                    fileName, (double) fileSize, image.getWidth(), image.getHeight()));
                
            } catch (Exception e) {
                showError("Ошибка загрузки изображения: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRemoveImage() {
        if (planImageView.getImage() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение");
            alert.setHeaderText("Удаление изображения");
            alert.setContentText("Вы уверены, что хотите удалить изображение?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    planImageView.setImage(null);
                    currentImageFile = null;
                    imageInfoLabel.setText("Изображение не загружено");
                }
            });
        }
    }
    
    @FXML
    private void handleSave() {
        saveData();
    }
    
    @FXML
    private void handleClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Очистка данных");
        alert.setContentText("Вы уверены, что хотите очистить все данные?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearData();
            }
        });
    }
    
    // Вспомогательные методы
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
