package zakir.alekperov.ui.tabs.commoninfo;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import zakir.alekperov.service.address.AddressSuggestion;
import zakir.alekperov.service.address.DaDataService;
import zakir.alekperov.ui.dialogs.AddressValidationDialog;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.time.Year;
import java.util.List;
import java.util.Optional;

public class CommonInfoTabController extends BaseTabController {
    
    // АДРЕС
    @FXML private TextField regionField;
    @FXML private TextField districtField;
    @FXML private TextField cityField;
    @FXML private TextField cityDistrictField;
    @FXML private TextField streetField;
    @FXML private TextField houseField;
    @FXML private TextField buildingField;
    
    @FXML private VBox validatedAddressBox;
    @FXML private Label validatedAddressLabel;
    @FXML private Label postalCodeLabel;
    
    // ХАРАКТЕРИСТИКИ
    @FXML private ComboBox<String> purposeField;
    @FXML private ComboBox<String> actualUseField;
    @FXML private TextField buildYearField;
    
    // ПЛОЩАДИ И ЭТАЖНОСТЬ
    @FXML private TextField totalAreaField;
    @FXML private TextField livingAreaField;
    @FXML private Spinner<Integer> aboveGroundFloorsSpinner;
    @FXML private Spinner<Integer> undergroundFloorsSpinner;
    
    // ПРИМЕЧАНИЕ
    @FXML private TextArea remarksField;
    
    private DaDataService daDataService;
    private AddressSuggestion validatedAddress;
    
    @Override
    protected void setupBindings() {
        daDataService = new DaDataService();
        
        // Инициализация выпадающих списков
        initializeComboBoxes();
        
        // Инициализация спиннеров
        initializeSpinners();
        
        // Валидация полей
        setupFieldValidation();
        
        // Скрываем блок проверенного адреса при изменении полей
        setupAddressChangeListeners();
    }
    
    private void initializeComboBoxes() {
        // Назначение
        purposeField.setItems(FXCollections.observableArrayList(
            "Жилое",
            "Нежилое",
            "Жилое с коммерческими помещениями",
            "Вспомогательное (гараж, сарай и т.п.)"
        ));
        purposeField.getSelectionModel().selectFirst();
        
        // Фактическое использование
        actualUseField.setItems(FXCollections.observableArrayList(
            "По назначению",
            "Не по назначению"
        ));
        actualUseField.getSelectionModel().selectFirst();
    }
    
    private void initializeSpinners() {
        // Надземные этажи: от 1 до 10
        SpinnerValueFactory<Integer> aboveGroundFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        aboveGroundFloorsSpinner.setValueFactory(aboveGroundFactory);
        aboveGroundFloorsSpinner.setEditable(true);
        
        // Подземные этажи: от 0 до 3
        SpinnerValueFactory<Integer> undergroundFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0);
        undergroundFloorsSpinner.setValueFactory(undergroundFactory);
        undergroundFloorsSpinner.setEditable(true);
    }
    
    private void setupFieldValidation() {
        // Валидация года постройки - только цифры
        buildYearField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                buildYearField.setText(oldVal);
            }
        });
        
        // Валидация площадей - только числа с точкой
        totalAreaField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                totalAreaField.setText(oldVal);
            }
        });
        
        livingAreaField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                livingAreaField.setText(oldVal);
            }
        });
    }
    
    private void setupAddressChangeListeners() {
        regionField.textProperty().addListener((obs, old, val) -> hideValidatedAddress());
        districtField.textProperty().addListener((obs, old, val) -> hideValidatedAddress());
        cityField.textProperty().addListener((obs, old, val) -> hideValidatedAddress());
        cityDistrictField.textProperty().addListener((obs, old, val) -> hideValidatedAddress());
        streetField.textProperty().addListener((obs, old, val) -> hideValidatedAddress());
        houseField.textProperty().addListener((obs, old, val) -> hideValidatedAddress());
        buildingField.textProperty().addListener((obs, old, val) -> hideValidatedAddress());
    }
    
    private void hideValidatedAddress() {
        validatedAddressBox.setVisible(false);
        validatedAddressBox.setManaged(false);
        validatedAddress = null;
    }
    
    @FXML
    private void handleSave() {
        if (validateAllFields() && validateAndCorrectAddress()) {
            saveData();
        }
    }
    
    /**
     * Валидация всех полей формы
     */
    private boolean validateAllFields() {
        // Проверка адреса
        if (regionField.getText().isBlank()) {
            showWarning("Укажите субъект РФ");
            regionField.requestFocus();
            return false;
        }
        if (cityField.getText().isBlank()) {
            showWarning("Укажите город или населенный пункт");
            cityField.requestFocus();
            return false;
        }
        if (streetField.getText().isBlank()) {
            showWarning("Укажите улицу");
            streetField.requestFocus();
            return false;
        }
        if (houseField.getText().isBlank()) {
            showWarning("Укажите номер дома");
            houseField.requestFocus();
            return false;
        }
        
        // Проверка назначения
        if (purposeField.getValue() == null) {
            showWarning("Выберите назначение объекта");
            purposeField.requestFocus();
            return false;
        }
        
        // Проверка фактического использования
        if (actualUseField.getValue() == null) {
            showWarning("Выберите фактическое использование");
            actualUseField.requestFocus();
            return false;
        }
        
        // Проверка года постройки
        if (buildYearField.getText().isBlank()) {
            showWarning("Укажите год постройки");
            buildYearField.requestFocus();
            return false;
        }
        
        int buildYear;
        try {
            buildYear = Integer.parseInt(buildYearField.getText());
            int currentYear = Year.now().getValue();
            
            if (buildYear < 1800 || buildYear > currentYear + 1) {
                showWarning("Год постройки должен быть между 1800 и " + (currentYear + 1));
                buildYearField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showWarning("Год постройки должен быть числом");
            buildYearField.requestFocus();
            return false;
        }
        
        // Проверка общей площади
        if (totalAreaField.getText().isBlank()) {
            showWarning("Укажите общую площадь");
            totalAreaField.requestFocus();
            return false;
        }
        
        double totalArea;
        try {
            totalArea = Double.parseDouble(totalAreaField.getText());
            if (totalArea <= 0 || totalArea > 10000) {
                showWarning("Общая площадь должна быть больше 0 и меньше 10000 кв.м");
                totalAreaField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showWarning("Общая площадь должна быть числом");
            totalAreaField.requestFocus();
            return false;
        }
        
        // Проверка жилой площади
        if (livingAreaField.getText().isBlank()) {
            showWarning("Укажите жилую площадь");
            livingAreaField.requestFocus();
            return false;
        }
        
        double livingArea;
        try {
            livingArea = Double.parseDouble(livingAreaField.getText());
            if (livingArea <= 0) {
                showWarning("Жилая площадь должна быть больше 0");
                livingAreaField.requestFocus();
                return false;
            }
            if (livingArea > totalArea) {
                showWarning("Жилая площадь не может быть больше общей площади");
                livingAreaField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showWarning("Жилая площадь должна быть числом");
            livingAreaField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Проверяет адрес через API и предлагает исправления
     */
    private boolean validateAndCorrectAddress() {
        // Формируем полный адрес для проверки
        String fullAddress = buildFullAddressQuery();
        
        // Показываем прогресс
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Проверка адреса");
        progressAlert.setHeaderText("Проверяем адрес через базу ФИАС...");
        progressAlert.setContentText("Пожалуйста, подождите...");
        progressAlert.show();
        
        // Запрашиваем проверку через API
        List<AddressSuggestion> suggestions = daDataService.getSuggestions(fullAddress, 5);
        
        progressAlert.close();
        
        if (suggestions.isEmpty()) {
            showError("Адрес не найден", 
                "Указанный адрес не найден в базе ФИАС.\n\n" +
                "Проверьте правильность написания:\n" +
                "- Название региона\n" +
                "- Название города\n" +
                "- Название улицы\n" +
                "- Номер дома");
            return false;
        }
        
        // Проверяем точное совпадение
        AddressSuggestion exactMatch = findExactMatch(suggestions, fullAddress);
        
        if (exactMatch != null) {
            validatedAddress = exactMatch;
            showValidatedAddress(exactMatch);
            return true;
        }
        
        // Точного совпадения нет - показываем диалог выбора
        AddressValidationDialog dialog = new AddressValidationDialog(fullAddress, suggestions);
        Optional<AddressSuggestion> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            validatedAddress = result.get();
            fillFieldsFromSuggestion(validatedAddress);
            showValidatedAddress(validatedAddress);
            return true;
        }
        
        return false;
    }
    
    private String buildFullAddressQuery() {
        StringBuilder address = new StringBuilder();
        
        if (!regionField.getText().isBlank()) {
            address.append(regionField.getText());
        }
        if (!districtField.getText().isBlank()) {
            address.append(", ").append(districtField.getText());
        }
        if (!cityField.getText().isBlank()) {
            address.append(", ").append(cityField.getText());
        }
        if (!cityDistrictField.getText().isBlank()) {
            address.append(", ").append(cityDistrictField.getText());
        }
        if (!streetField.getText().isBlank()) {
            address.append(", ").append(streetField.getText());
        }
        if (!houseField.getText().isBlank()) {
            address.append(", д. ").append(houseField.getText());
        }
        if (!buildingField.getText().isBlank()) {
            address.append(", корп. ").append(buildingField.getText());
        }
        
        return address.toString();
    }
    
    private AddressSuggestion findExactMatch(List<AddressSuggestion> suggestions, String query) {
        String normalizedQuery = normalizeAddress(query);
        
        for (AddressSuggestion suggestion : suggestions) {
            String normalizedSuggestion = normalizeAddress(suggestion.getUnrestricted());
            
            if (normalizedQuery.equals(normalizedSuggestion)) {
                return suggestion;
            }
        }
        
        return null;
    }
    
    private String normalizeAddress(String address) {
        return address.toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("[.,]", "")
                .trim();
    }
    
    private void fillFieldsFromSuggestion(AddressSuggestion suggestion) {
        if (suggestion.getRegion() != null) {
            regionField.setText(suggestion.getRegion());
        }
        if (suggestion.getCity() != null) {
            cityField.setText(suggestion.getCity());
        }
        if (suggestion.getStreet() != null) {
            streetField.setText(suggestion.getStreet());
        }
        if (suggestion.getHouse() != null) {
            houseField.setText(suggestion.getHouse());
        }
    }
    
    private void showValidatedAddress(AddressSuggestion address) {
        validatedAddressLabel.setText(address.getUnrestricted());
        
        if (address.getPostalCode() != null) {
            postalCodeLabel.setText("Почтовый индекс: " + address.getPostalCode());
        } else {
            postalCodeLabel.setText("");
        }
        
        validatedAddressBox.setVisible(true);
        validatedAddressBox.setManaged(true);
    }
    
    @FXML
    private void handleClear() {
        clearData();
    }
    
    @Override
    public boolean validateData() {
        return validatedAddress != null;
    }
    
    @Override
    public void saveData() {
        if (validatedAddress == null) {
            showWarning("Сначала проверьте адрес");
            return;
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=== ОБЩИЕ СВЕДЕНИЯ ===\n\n");
        
        report.append("АДРЕС:\n");
        report.append(validatedAddress.getUnrestricted()).append("\n");
        report.append("Индекс: ").append(validatedAddress.getPostalCode() != null ? 
            validatedAddress.getPostalCode() : "не указан").append("\n");
        report.append("ФИАС ID: ").append(validatedAddress.getFiasId()).append("\n\n");
        
        report.append("ХАРАКТЕРИСТИКИ:\n");
        report.append("Назначение: ").append(purposeField.getValue()).append("\n");
        report.append("Фактическое использование: ").append(actualUseField.getValue()).append("\n");
        report.append("Год постройки: ").append(buildYearField.getText()).append("\n\n");
        
        report.append("ПЛОЩАДИ И ЭТАЖНОСТЬ:\n");
        report.append("Общая площадь: ").append(totalAreaField.getText()).append(" кв.м\n");
        report.append("Жилая площадь: ").append(livingAreaField.getText()).append(" кв.м\n");
        report.append("Надземных этажей: ").append(aboveGroundFloorsSpinner.getValue()).append("\n");
        report.append("Подземных этажей: ").append(undergroundFloorsSpinner.getValue()).append("\n");
        
        if (remarksField.getText() != null && !remarksField.getText().isBlank()) {
            report.append("\nПРИМЕЧАНИЕ:\n");
            report.append(remarksField.getText()).append("\n");
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сохранено");
        alert.setHeaderText("Раздел 1. Общие сведения сохранены");
        alert.setContentText(report.toString());
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
        
        System.out.println(report);
    }
    
    @Override
    public void clearData() {
        // Адрес
        regionField.clear();
        districtField.clear();
        cityField.clear();
        cityDistrictField.clear();
        streetField.clear();
        houseField.clear();
        buildingField.clear();
        
        // Характеристики
        purposeField.getSelectionModel().selectFirst();
        actualUseField.getSelectionModel().selectFirst();
        buildYearField.clear();
        
        // Площади и этажность
        totalAreaField.clear();
        livingAreaField.clear();
        aboveGroundFloorsSpinner.getValueFactory().setValue(1);
        undergroundFloorsSpinner.getValueFactory().setValue(0);
        
        // Примечание
        remarksField.clear();
        
        hideValidatedAddress();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
