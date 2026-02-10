package zakir.alekperov.ui.tabs.commoninfo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import zakir.alekperov.ui.tabs.base.BaseTabController;

/**
 * Контроллер вкладки "Общие сведения".
 */
public class CommonInfoTabController extends BaseTabController {
    
    // Блок: Адрес объекта
    @FXML
    private TextField regionField;
    
    @FXML
    private TextField districtField;
    
    @FXML
    private TextField cityField;
    
    @FXML
    private TextField streetField;
    
    @FXML
    private TextField houseNumberField;
    
    @FXML
    private TextField buildingNumberField;
    
    // Блок: Основная информация
    @FXML
    private ComboBox<String> purposeComboBox;
    
    @FXML
    private ComboBox<String> actualUseComboBox;
    
    @FXML
    private TextField constructionYearField;
    
    // Блок: Площади
    @FXML
    private TextField totalAreaField;
    
    @FXML
    private TextField livingAreaField;
    
    @FXML
    private TextField aboveGroundFloorsField;
    
    @FXML
    private TextField undergroundFloorsField;
    
    // Блок: Реквизиты паспорта
    @FXML
    private TextField inventoryNumberField;
    
    @FXML
    private TextField cadastralNumberField;
    
    @FXML
    private DatePicker compilationDatePicker;
    
    @FXML
    private TextArea notesArea;
    
    @Override
    protected void setupBindings() {
        // Настройка списков выбора
        purposeComboBox.getItems().addAll(
            "Жилой дом",
            "Многоквартирный дом",
            "Нежилое здание",
            "Производственное здание"
        );
        
        actualUseComboBox.getItems().addAll(
            "По назначению",
            "Не используется",
            "Частично используется"
        );
    }
    
    @Override
    protected void setupValidation() {
        // Валидация числовых полей
        addNumericValidation(constructionYearField);
        addNumericValidation(aboveGroundFloorsField);
        addNumericValidation(undergroundFloorsField);
        
        // Валидация полей с площадью
        addDecimalValidation(totalAreaField);
        addDecimalValidation(livingAreaField);
    }
    
    @Override
    protected void loadInitialData() {
        // Значения по умолчанию
        if (purposeComboBox.getItems().size() > 0) {
            purposeComboBox.getSelectionModel().selectFirst();
        }
        if (actualUseComboBox.getItems().size() > 0) {
            actualUseComboBox.getSelectionModel().selectFirst();
        }
    }
    
    @Override
    public boolean validateData() {
        if (regionField.getText().isBlank()) {
            showError("Регион обязателен для заполнения");
            return false;
        }
        
        if (cityField.getText().isBlank()) {
            showError("Город обязателен для заполнения");
            return false;
        }
        
        if (streetField.getText().isBlank()) {
            showError("Улица обязательна для заполнения");
            return false;
        }
        
        if (houseNumberField.getText().isBlank()) {
            showError("Номер дома обязателен для заполнения");
            return false;
        }
        
        return true;
    }
    
    @Override
    public void saveData() {
        if (validateData()) {
            System.out.println("Сохранение общих сведений...");
            showInfo("Данные сохранены успешно");
        }
    }
    
    @Override
    public void clearData() {
        regionField.clear();
        districtField.clear();
        cityField.clear();
        streetField.clear();
        houseNumberField.clear();
        buildingNumberField.clear();
        constructionYearField.clear();
        totalAreaField.clear();
        livingAreaField.clear();
        aboveGroundFloorsField.clear();
        undergroundFloorsField.clear();
        inventoryNumberField.clear();
        cadastralNumberField.clear();
        notesArea.clear();
        purposeComboBox.getSelectionModel().clearSelection();
        actualUseComboBox.getSelectionModel().clearSelection();
        compilationDatePicker.setValue(null);
    }
    
    // Обработчики событий кнопок
    
    @FXML
    private void handleSave() {
        saveData();
    }
    
    @FXML
    private void handleClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Очистка данных");
        alert.setContentText("Вы уверены, что хотите очистить все поля?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearData();
            }
        });
    }
    
    // Вспомогательные методы
    
    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                field.setText(oldValue);
            }
        });
    }
    
    private void addDecimalValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                field.setText(oldValue);
            }
        });
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Ошибка валидации");
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
