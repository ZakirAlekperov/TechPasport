package zakir.alekperov.ui.dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import zakir.alekperov.model.Owner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Диалог для добавления или редактирования правообладателя
 */
public class OwnerDialog extends Dialog<Owner> {
    
    private final TextField fullNameField;
    private final ComboBox<String> ownerTypeField;
    private final ComboBox<String> documentTypeField;
    private final TextField documentNumberField;
    private final DatePicker documentDatePicker;  // Изменено на DatePicker
    private final TextField registrationNumberField;
    private final DatePicker registrationDatePicker;  // Изменено на DatePicker
    private final ComboBox<String> shareField;
    private final TextField customShareField;
    
    private final Owner editingOwner;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    public OwnerDialog(Owner owner, Window ownerWindow) {  // Добавлен параметр ownerWindow
        this.editingOwner = owner;
        
        // Устанавливаем владельца окна для правильного позиционирования
        initOwner(ownerWindow);
        
        setTitle(owner == null ? "Добавить правообладателя" : "Редактировать правообладателя");
        setHeaderText(owner == null ? 
            "Заполните информацию о правообладателе" : 
            "Редактирование: " + owner.getFullName());
        
        // Создаем поля ввода
        fullNameField = new TextField();
        fullNameField.setPromptText("Иванов Иван Иванович");
        fullNameField.setPrefWidth(350);
        
        ownerTypeField = new ComboBox<>(FXCollections.observableArrayList(
            "Физическое лицо",
            "Юридическое лицо"
        ));
        ownerTypeField.setValue("Физическое лицо");
        ownerTypeField.setPrefWidth(200);
        
        documentTypeField = new ComboBox<>(FXCollections.observableArrayList(
            "Свидетельство о праве собственности",
            "Договор купли-продажи",
            "Договор дарения",
            "Договор мены",
            "Свидетельство о праве на наследство",
            "Решение суда",
            "Договор участия в долевом строительстве",
            "Акт приема-передачи",
            "Другое"
        ));
        documentTypeField.setEditable(true);
        documentTypeField.setPromptText("Выберите или введите");
        documentTypeField.setPrefWidth(350);
        
        documentNumberField = new TextField();
        documentNumberField.setPromptText("77-АА-123456");
        documentNumberField.setPrefWidth(200);
        
        // DatePicker для даты документа
        documentDatePicker = new DatePicker();
        documentDatePicker.setPromptText("Выберите дату");
        documentDatePicker.setPrefWidth(200);
        configureDatePicker(documentDatePicker);
        
        registrationNumberField = new TextField();
        registrationNumberField.setPromptText("77:01:0001001:1234");
        registrationNumberField.setPrefWidth(200);
        
        // DatePicker для даты регистрации
        registrationDatePicker = new DatePicker();
        registrationDatePicker.setPromptText("Выберите дату");
        registrationDatePicker.setPrefWidth(200);
        configureDatePicker(registrationDatePicker);
        
        shareField = new ComboBox<>(FXCollections.observableArrayList(
            "1 (100%)",
            "1/2 (50%)",
            "1/3 (33.33%)",
            "1/4 (25%)",
            "2/3 (66.67%)",
            "3/4 (75%)",
            "Другое"
        ));
        shareField.setValue("1 (100%)");
        shareField.setPrefWidth(150);
        
        customShareField = new TextField();
        customShareField.setPromptText("0.5");
        customShareField.setPrefWidth(100);
        customShareField.setDisable(true);
        
        // Валидация
        setupValidation();
        
        // Заполняем данные при редактировании
        if (owner != null) {
            fillFields(owner);
        }
        
        // Создаем layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        int row = 0;
        
        // ФИО / Наименование
        grid.add(new Label("ФИО / Наименование организации:*"), 0, row);
        grid.add(fullNameField, 1, row++);
        
        // Тип правообладателя
        grid.add(new Label("Тип правообладателя:*"), 0, row);
        grid.add(ownerTypeField, 1, row++);
        
        // Тип документа
        grid.add(new Label("Тип документа:*"), 0, row);
        grid.add(documentTypeField, 1, row++);
        
        // Номер документа
        grid.add(new Label("Номер документа:*"), 0, row);
        grid.add(documentNumberField, 1, row++);
        
        // Дата документа - используем DatePicker
        grid.add(new Label("Дата документа:*"), 0, row);
        grid.add(documentDatePicker, 1, row++);
        
        // Номер регистрации
        grid.add(new Label("Номер гос. регистрации:"), 0, row);
        grid.add(registrationNumberField, 1, row++);
        
        // Дата регистрации - используем DatePicker
        grid.add(new Label("Дата гос. регистрации:"), 0, row);
        grid.add(registrationDatePicker, 1, row++);
        
        // Доля в праве
        grid.add(new Label("Доля в праве:*"), 0, row);
        HBox shareBox = new HBox(5);
        shareBox.getChildren().addAll(shareField, customShareField);
        grid.add(shareBox, 1, row++);
        
        // Примечание
        Label noteLabel = new Label("* - обязательные поля");
        noteLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");
        grid.add(noteLabel, 0, row, 2, 1);
        
        getDialogPane().setContent(grid);
        
        // Кнопки
        ButtonType saveButton = new ButtonType(
            owner == null ? "Добавить" : "Сохранить", 
            ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);
        
        // Блокируем закрытие при невалидных данных
        final Button saveBtn = (Button) getDialogPane().lookupButton(saveButton);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateInput()) {
                event.consume();
            }
        });
        
        saveBtn.setOnAction(e -> {
            if (validateInput()) {
                setResult(createOwner());
            }
        });
    }
    
    /**
     * Настройка DatePicker для русской локализации и формата
     */
    private void configureDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, DATE_FORMATTER);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });
    }
    
    private void setupValidation() {
        // Доля: только цифры с точкой
        customShareField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                customShareField.setText(oldVal);
            }
        });
        
        // Активация поля для ввода произвольной доли
        shareField.valueProperty().addListener((obs, oldVal, newVal) -> {
            customShareField.setDisable(!"Другое".equals(newVal));
            if (!"Другое".equals(newVal)) {
                customShareField.clear();
            }
        });
    }
    
    private void fillFields(Owner owner) {
        fullNameField.setText(owner.getFullName());
        ownerTypeField.setValue(owner.getOwnerType());
        documentTypeField.setValue(owner.getDocumentType());
        documentNumberField.setText(owner.getDocumentNumber());
        
        // Парсим дату документа
        if (!owner.getDocumentDate().isBlank()) {
            try {
                LocalDate date = LocalDate.parse(owner.getDocumentDate(), DATE_FORMATTER);
                documentDatePicker.setValue(date);
            } catch (Exception e) {
                // Игнорируем ошибку парсинга
            }
        }
        
        registrationNumberField.setText(owner.getRegistrationNumber());
        
        // Парсим дату регистрации
        if (!owner.getRegistrationDate().isBlank()) {
            try {
                LocalDate date = LocalDate.parse(owner.getRegistrationDate(), DATE_FORMATTER);
                registrationDatePicker.setValue(date);
            } catch (Exception e) {
                // Игнорируем ошибку парсинга
            }
        }
        
        // Определяем долю
        String formatted = owner.getFormattedShare();
        switch (formatted) {
            case "1":
                shareField.setValue("1 (100%)");
                break;
            case "1/2":
                shareField.setValue("1/2 (50%)");
                break;
            case "1/3":
                shareField.setValue("1/3 (33.33%)");
                break;
            case "1/4":
                shareField.setValue("1/4 (25%)");
                break;
            case "2/3":
                shareField.setValue("2/3 (66.67%)");
                break;
            case "3/4":
                shareField.setValue("3/4 (75%)");
                break;
            default:
                shareField.setValue("Другое");
                customShareField.setDisable(false);
                customShareField.setText(String.format("%.4f", owner.getShareSize()));
        }
    }
    
    private boolean validateInput() {
        // ФИО
        if (fullNameField.getText().isBlank()) {
            showError("Укажите ФИО или наименование организации");
            return false;
        }
        
        // Тип документа
        if (documentTypeField.getValue() == null || documentTypeField.getValue().isBlank()) {
            showError("Укажите тип документа");
            return false;
        }
        
        // Номер документа
        if (documentNumberField.getText().isBlank()) {
            showError("Укажите номер документа");
            return false;
        }
        
        // Дата документа
        if (documentDatePicker.getValue() == null) {
            showError("Укажите дату документа");
            return false;
        }
        
        // Доля
        if ("Другое".equals(shareField.getValue())) {
            if (customShareField.getText().isBlank()) {
                showError("Укажите долю в праве");
                return false;
            }
            
            try {
                double share = Double.parseDouble(customShareField.getText());
                if (share <= 0 || share > 1) {
                    showError("Доля должна быть больше 0 и не больше 1");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Доля должна быть числом");
                return false;
            }
        }
        
        return true;
    }
    
    private Owner createOwner() {
        String fullName = fullNameField.getText().trim();
        String ownerType = ownerTypeField.getValue();
        String documentType = documentTypeField.getValue();
        String documentNumber = documentNumberField.getText().trim();
        String documentDate = documentDatePicker.getValue() != null ? 
            DATE_FORMATTER.format(documentDatePicker.getValue()) : "";
        String registrationNumber = registrationNumberField.getText().trim();
        String registrationDate = registrationDatePicker.getValue() != null ? 
            DATE_FORMATTER.format(registrationDatePicker.getValue()) : "";
        
        // Определяем долю
        double share;
        String selectedShare = shareField.getValue();
        switch (selectedShare) {
            case "1 (100%)":
                share = 1.0;
                break;
            case "1/2 (50%)":
                share = 0.5;
                break;
            case "1/3 (33.33%)":
                share = 0.3333;
                break;
            case "1/4 (25%)":
                share = 0.25;
                break;
            case "2/3 (66.67%)":
                share = 0.6667;
                break;
            case "3/4 (75%)":
                share = 0.75;
                break;
            default: // "Другое"
                share = Double.parseDouble(customShareField.getText());
        }
        
        return new Owner(fullName, ownerType, documentType, documentNumber,
                        documentDate, registrationNumber, registrationDate, share);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(getOwner());  // Привязываем к текущему окну
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
