package zakir.alekperov.ui.tabs.locationplan;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import zakir.alekperov.application.locationplan.*;
import zakir.alekperov.domain.shared.ValidationException;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.io.File;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Контроллер вкладки "Ситуационный план".
 * Зависит только от интерфейсов use cases из application слоя.
 */
public class LocationPlanTabController extends BaseTabController {
    
    private SaveLocationPlanUseCase saveLocationPlanUseCase;
    private LoadLocationPlanUseCase loadLocationPlanUseCase;
    private AddBuildingCoordinatesUseCase addBuildingCoordinatesUseCase;
    
    @FXML private ComboBox<String> scaleComboBox;
    @FXML private DatePicker creationDatePicker;
    @FXML private TextField authorField;
    @FXML private ImageView planImageView;
    @FXML private Label placeholderLabel;
    @FXML private Label imageInfoLabel;
    @FXML private TextArea notesArea;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button loadImageButton;
    @FXML private Button removeImageButton;
    @FXML private Button addCoordinatesButton;
    @FXML private ListView<String> buildingsListView;
    
    private File currentImageFile;
    private String currentPassportId;
    
    /**
     * Пустой конструктор для FXML.
     * Зависимости будут установлены через setDependencies().
     */
    public LocationPlanTabController() {
        // FXML требует пустого конструктора
    }
    
    /**
     * Конструктор с внедрением зависимостей.
     */
    public LocationPlanTabController(SaveLocationPlanUseCase saveLocationPlanUseCase,
                                    LoadLocationPlanUseCase loadLocationPlanUseCase,
                                    AddBuildingCoordinatesUseCase addBuildingCoordinatesUseCase) {
        setDependencies(saveLocationPlanUseCase, loadLocationPlanUseCase, addBuildingCoordinatesUseCase);
    }
    
    /**
     * Установить зависимости после создания (для FXML).
     */
    public void setDependencies(SaveLocationPlanUseCase saveLocationPlanUseCase,
                               LoadLocationPlanUseCase loadLocationPlanUseCase,
                               AddBuildingCoordinatesUseCase addBuildingCoordinatesUseCase) {
        if (saveLocationPlanUseCase == null) {
            throw new IllegalArgumentException("SaveLocationPlanUseCase не может быть null");
        }
        if (loadLocationPlanUseCase == null) {
            throw new IllegalArgumentException("LoadLocationPlanUseCase не может быть null");
        }
        if (addBuildingCoordinatesUseCase == null) {
            throw new IllegalArgumentException("AddBuildingCoordinatesUseCase не может быть null");
        }
        
        this.saveLocationPlanUseCase = saveLocationPlanUseCase;
        this.loadLocationPlanUseCase = loadLocationPlanUseCase;
        this.addBuildingCoordinatesUseCase = addBuildingCoordinatesUseCase;
    }
    
    @Override
    protected void setupBindings() {
        if (planImageView != null) {
            planImageView.imageProperty().addListener((obs, oldImage, newImage) -> {
                if (placeholderLabel != null) {
                    placeholderLabel.setVisible(newImage == null);
                }
                if (removeImageButton != null) {
                    removeImageButton.setDisable(newImage == null);
                }
            });
        }
        
        if (scaleComboBox != null) {
            scaleComboBox.getItems().addAll("100", "200", "500", "1000", "2000", "5000");
            scaleComboBox.setValue("500");
        }
    }
    
    @Override
    protected void loadInitialData() {
        if (creationDatePicker != null) {
            creationDatePicker.setValue(LocalDate.now());
        }
        if (placeholderLabel != null) {
            placeholderLabel.setVisible(true);
        }
        
        if (currentPassportId != null && saveLocationPlanUseCase != null) {
            loadLocationPlanData();
        }
    }
    
    public void setPassportId(String passportId) {
        if (passportId == null || passportId.isBlank()) {
            throw new IllegalArgumentException("ID паспорта не может быть пустым");
        }
        this.currentPassportId = passportId;
        
        if (loadLocationPlanUseCase != null) {
            loadLocationPlanData();
        }
    }
    
    private void loadLocationPlanData() {
        try {
            LoadLocationPlanQuery query = new LoadLocationPlanQuery(currentPassportId);
            Optional<LocationPlanDTO> planOptional = loadLocationPlanUseCase.execute(query);
            
            if (planOptional.isPresent()) {
                LocationPlanDTO plan = planOptional.get();
                
                if (scaleComboBox != null) {
                    scaleComboBox.setValue(String.valueOf(plan.scaleDenominator()));
                }
                if (authorField != null) {
                    authorField.setText(plan.executorName());
                }
                if (creationDatePicker != null) {
                    creationDatePicker.setValue(plan.planDate());
                }
                if (notesArea != null) {
                    notesArea.setText(plan.notes());
                }
                
                if (plan.imagePath() != null && !plan.imagePath().isBlank()) {
                    loadImageFromPath(plan.imagePath());
                }
                
                if (buildingsListView != null) {
                    buildingsListView.getItems().clear();
                    for (var building : plan.buildings()) {
                        String item = String.format("Литера %s: %s (%d точек)", 
                            building.litera(), building.description(), building.points().size());
                        buildingsListView.getItems().add(item);
                    }
                }
                
                System.out.println("✓ Данные ситуационного плана загружены");
            } else {
                System.out.println("ℹ️ Ситуационный план не найден, создается новый");
            }
            
        } catch (ValidationException e) {
            showError("Ошибка валидации", e.getMessage());
        } catch (Exception e) {
            showError("Ошибка загрузки", "Не удалось загрузить данные: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean validateData() {
        if (currentPassportId == null || currentPassportId.isBlank()) {
            showWarning("ID паспорта не установлен");
            return false;
        }
        
        if (scaleComboBox == null || scaleComboBox.getValue() == null || scaleComboBox.getValue().isBlank()) {
            showWarning("Укажите масштаб плана");
            if (scaleComboBox != null) scaleComboBox.requestFocus();
            return false;
        }
        
        if (creationDatePicker == null || creationDatePicker.getValue() == null) {
            showWarning("Укажите дату создания плана");
            if (creationDatePicker != null) creationDatePicker.requestFocus();
            return false;
        }
        
        return true;
    }
    
    @Override
    public void saveData() {
        if (saveLocationPlanUseCase == null) {
            showWarning("Зависимости не установлены");
            return;
        }
        
        if (!validateData()) {
            return;
        }
        
        try {
            SaveLocationPlanCommand command = new SaveLocationPlanCommand(
                currentPassportId,
                scaleComboBox.getValue(),
                authorField != null ? authorField.getText() : "",
                creationDatePicker.getValue(),
                notesArea != null ? notesArea.getText() : "",
                currentImageFile != null ? currentImageFile.getAbsolutePath() : null
            );
            
            saveLocationPlanUseCase.execute(command);
            
            showInfo("Ситуационный план сохранен успешно");
            
        } catch (ValidationException e) {
            showError("Ошибка валидации", e.getMessage());
        } catch (Exception e) {
            showError("Ошибка сохранения", "Не удалось сохранить: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void clearData() {
        if (scaleComboBox != null) scaleComboBox.setValue("500");
        if (creationDatePicker != null) creationDatePicker.setValue(LocalDate.now());
        if (authorField != null) authorField.clear();
        if (notesArea != null) notesArea.clear();
        if (planImageView != null) planImageView.setImage(null);
        currentImageFile = null;
        if (imageInfoLabel != null) imageInfoLabel.setText("Изображение не загружено");
        if (buildingsListView != null) buildingsListView.getItems().clear();
    }
    
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
            loadImageFromFile(selectedFile);
        }
    }
    
    @FXML
    private void handleRemoveImage() {
        if (planImageView != null && planImageView.getImage() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение");
            alert.setHeaderText("Удаление изображения");
            alert.setContentText("Вы уверены, что хотите удалить изображение?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    planImageView.setImage(null);
                    currentImageFile = null;
                    if (imageInfoLabel != null) {
                        imageInfoLabel.setText("Изображение не загружено");
                    }
                }
            });
        }
    }
    
    @FXML
    private void handleAddCoordinates() {
        showInfo("Функция добавления координат в разработке");
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
    
    private void loadImageFromFile(File file) {
        try {
            Image image = new Image(file.toURI().toString());
            if (planImageView != null) {
                planImageView.setImage(image);
            }
            currentImageFile = file;
            
            String fileName = file.getName();
            long fileSize = file.length() / 1024;
            if (imageInfoLabel != null) {
                imageInfoLabel.setText(String.format("%s (%.0f KB, %.0f×%.0f px)", 
                    fileName, (double) fileSize, image.getWidth(), image.getHeight()));
            }
            
        } catch (Exception e) {
            showError("Ошибка загрузки изображения", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadImageFromPath(String path) {
        File file = new File(path);
        if (file.exists()) {
            loadImageFromFile(file);
        } else {
            if (imageInfoLabel != null) {
                imageInfoLabel.setText("Изображение не найдено: " + path);
            }
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
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
