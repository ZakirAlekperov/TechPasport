package zakir.alekperov.ui.tabs.improvement;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import zakir.alekperov.ui.tabs.base.BaseTabController;

/**
 * Контроллер вкладки "Благоустройство".
 */
public class ImprovementTabController extends BaseTabController {
    
    // Водоснабжение
    @FXML
    private CheckBox centralWaterCheckBox;
    @FXML
    private CheckBox autonomousWaterCheckBox;
    @FXML
    private CheckBox noWaterCheckBox;
    @FXML
    private TextField waterAreaField;
    
    // Канализация
    @FXML
    private CheckBox centralSewerageCheckBox;
    @FXML
    private CheckBox autonomousSewerageCheckBox;
    @FXML
    private CheckBox noSewerageCheckBox;
    @FXML
    private TextField sewerageAreaField;
    
    // Отопление
    @FXML
    private CheckBox centralHeatingCheckBox;
    @FXML
    private CheckBox autonomousHeatingCheckBox;
    @FXML
    private CheckBox stoveHeatingCheckBox;
    @FXML
    private CheckBox noHeatingCheckBox;
    @FXML
    private TextField heatingAreaField;
    
    // Горячее водоснабжение
    @FXML
    private CheckBox centralHotWaterCheckBox;
    @FXML
    private CheckBox autonomousHotWaterCheckBox;
    @FXML
    private CheckBox noHotWaterCheckBox;
    @FXML
    private TextField hotWaterAreaField;
    
    // Газоснабжение
    @FXML
    private CheckBox centralGasCheckBox;
    @FXML
    private CheckBox autonomousGasCheckBox;
    @FXML
    private CheckBox noGasCheckBox;
    @FXML
    private TextField gasAreaField;
    
    // Электроснабжение
    @FXML
    private CheckBox electricityCheckBox;
    @FXML
    private TextField electricityAreaField;
    @FXML
    private TextField electricityPowerField;
    
    @FXML
    private TextArea notesArea;
    
    @Override
    protected void setupBindings() {
        // Взаимоисключающие чекбоксы для водоснабжения
        setupMutuallyExclusive(centralWaterCheckBox, autonomousWaterCheckBox, noWaterCheckBox);
        
        // Взаимоисключающие чекбоксы для канализации
        setupMutuallyExclusive(centralSewerageCheckBox, autonomousSewerageCheckBox, noSewerageCheckBox);
        
        // Взаимоисключающие чекбоксы для отопления
        setupMutuallyExclusive(centralHeatingCheckBox, autonomousHeatingCheckBox, 
                              stoveHeatingCheckBox, noHeatingCheckBox);
        
        // Взаимоисключающие чекбоксы для ГВС
        setupMutuallyExclusive(centralHotWaterCheckBox, autonomousHotWaterCheckBox, noHotWaterCheckBox);
        
        // Взаимоисключающие чекбоксы для газа
        setupMutuallyExclusive(centralGasCheckBox, autonomousGasCheckBox, noGasCheckBox);
    }
    
    @Override
    protected void setupValidation() {
        // Валидация числовых полей
        addDecimalValidation(waterAreaField);
        addDecimalValidation(sewerageAreaField);
        addDecimalValidation(heatingAreaField);
        addDecimalValidation(hotWaterAreaField);
        addDecimalValidation(gasAreaField);
        addDecimalValidation(electricityAreaField);
        addDecimalValidation(electricityPowerField);
    }
    
    @Override
    public boolean validateData() {
        // Минимальная валидация - хотя бы одна система должна быть выбрана
        boolean hasAnyUtility = 
            centralWaterCheckBox.isSelected() || autonomousWaterCheckBox.isSelected() ||
            centralSewerageCheckBox.isSelected() || autonomousSewerageCheckBox.isSelected() ||
            centralHeatingCheckBox.isSelected() || autonomousHeatingCheckBox.isSelected() || 
            stoveHeatingCheckBox.isSelected() ||
            centralHotWaterCheckBox.isSelected() || autonomousHotWaterCheckBox.isSelected() ||
            centralGasCheckBox.isSelected() || autonomousGasCheckBox.isSelected() ||
            electricityCheckBox.isSelected();
        
        if (!hasAnyUtility) {
            showWarning("Выберите хотя бы одну инженерную систему");
            return false;
        }
        
        return true;
    }
    
    @Override
    public void saveData() {
        if (validateData()) {
            System.out.println("Сохранение данных благоустройства...");
            printUtilityStatus();
            showInfo("Данные о благоустройстве сохранены успешно");
        }
    }
    
    @Override
    public void clearData() {
        // Водоснабжение
        centralWaterCheckBox.setSelected(false);
        autonomousWaterCheckBox.setSelected(false);
        noWaterCheckBox.setSelected(false);
        waterAreaField.clear();
        
        // Канализация
        centralSewerageCheckBox.setSelected(false);
        autonomousSewerageCheckBox.setSelected(false);
        noSewerageCheckBox.setSelected(false);
        sewerageAreaField.clear();
        
        // Отопление
        centralHeatingCheckBox.setSelected(false);
        autonomousHeatingCheckBox.setSelected(false);
        stoveHeatingCheckBox.setSelected(false);
        noHeatingCheckBox.setSelected(false);
        heatingAreaField.clear();
        
        // ГВС
        centralHotWaterCheckBox.setSelected(false);
        autonomousHotWaterCheckBox.setSelected(false);
        noHotWaterCheckBox.setSelected(false);
        hotWaterAreaField.clear();
        
        // Газ
        centralGasCheckBox.setSelected(false);
        autonomousGasCheckBox.setSelected(false);
        noGasCheckBox.setSelected(false);
        gasAreaField.clear();
        
        // Электричество
        electricityCheckBox.setSelected(true);
        electricityAreaField.clear();
        electricityPowerField.clear();
        
        notesArea.clear();
    }
    
    // Обработчики событий
    
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
    
    private void setupMutuallyExclusive(CheckBox... checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    for (CheckBox other : checkBoxes) {
                        if (other != checkBox) {
                            other.setSelected(false);
                        }
                    }
                }
            });
        }
    }
    
    private void addDecimalValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                field.setText(oldValue);
            }
        });
    }
    
    private void printUtilityStatus() {
        System.out.println("=== Благоустройство ===");
        System.out.println("Водоснабжение: " + getUtilityType(
            centralWaterCheckBox, autonomousWaterCheckBox, noWaterCheckBox) + 
            " (" + waterAreaField.getText() + " кв.м)");
        System.out.println("Канализация: " + getUtilityType(
            centralSewerageCheckBox, autonomousSewerageCheckBox, noSewerageCheckBox) + 
            " (" + sewerageAreaField.getText() + " кв.м)");
        System.out.println("Отопление: " + getHeatingType() + 
            " (" + heatingAreaField.getText() + " кв.м)");
        System.out.println("ГВС: " + getUtilityType(
            centralHotWaterCheckBox, autonomousHotWaterCheckBox, noHotWaterCheckBox) + 
            " (" + hotWaterAreaField.getText() + " кв.м)");
        System.out.println("Газ: " + getUtilityType(
            centralGasCheckBox, autonomousGasCheckBox, noGasCheckBox) + 
            " (" + gasAreaField.getText() + " кв.м)");
        System.out.println("Электричество: " + electricityAreaField.getText() + 
            " кв.м, " + electricityPowerField.getText() + " кВт");
    }
    
    private String getUtilityType(CheckBox central, CheckBox autonomous, CheckBox none) {
        if (central.isSelected()) return "Центральное";
        if (autonomous.isSelected()) return "Автономное";
        if (none.isSelected()) return "Отсутствует";
        return "Не указано";
    }
    
    private String getHeatingType() {
        if (centralHeatingCheckBox.isSelected()) return "Центральное";
        if (autonomousHeatingCheckBox.isSelected()) return "Автономное";
        if (stoveHeatingCheckBox.isSelected()) return "Печное";
        if (noHeatingCheckBox.isSelected()) return "Отсутствует";
        return "Не указано";
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
