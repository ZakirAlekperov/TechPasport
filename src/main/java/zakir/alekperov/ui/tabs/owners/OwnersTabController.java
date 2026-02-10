package zakir.alekperov.ui.tabs.owners;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Контроллер вкладки "Сведения о правообладателях".
 */
public class OwnersTabController extends BaseTabController {
    
    @FXML
    private TableView<OwnerRecord> ownersTable;
    
    @FXML
    private TableColumn<OwnerRecord, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<OwnerRecord, String> documentNumberColumn;
    
    @FXML
    private TableColumn<OwnerRecord, String> nameColumn;
    
    @FXML
    private TableColumn<OwnerRecord, String> rightsTypeColumn;
    
    @FXML
    private TableColumn<OwnerRecord, String> shareColumn;
    
    @FXML
    private TableColumn<OwnerRecord, String> notesColumn;
    
    @FXML
    private Label totalOwnersLabel;
    
    @FXML
    private Label totalShareLabel;
    
    @FXML
    private Label shareStatusLabel;
    
    private final ObservableList<OwnerRecord> ownersList = 
            FXCollections.observableArrayList();
    
    @Override
    protected void setupBindings() {
        // Настройка колонок таблицы
        dateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().recordDateProperty());
        
        documentNumberColumn.setCellValueFactory(cellData -> 
            cellData.getValue().documentNumberProperty());
        
        nameColumn.setCellValueFactory(cellData -> 
            cellData.getValue().ownerNameProperty());
        
        rightsTypeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().rightsTypeProperty());
        
        shareColumn.setCellValueFactory(cellData -> 
            cellData.getValue().shareProperty());
        
        notesColumn.setCellValueFactory(cellData -> 
            cellData.getValue().notesProperty());
        
        // Привязка списка к таблице
        ownersTable.setItems(ownersList);
        
        // Обновление статистики при изменении списка
        ownersList.addListener((javafx.collections.ListChangeListener.Change<? extends OwnerRecord> c) -> {
            updateStatistics();
        });
    }
    
    @Override
    protected void loadInitialData() {
        updateStatistics();
    }
    
    @Override
    public boolean validateData() {
        // Правообладатели необязательны, но если есть - проверяем доли
        if (!ownersList.isEmpty()) {
            double totalShare = calculateTotalShare();
            if (totalShare > 1.0) {
                showWarning("Сумма долей превышает 1. Проверьте данные.");
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void saveData() {
        if (validateData()) {
            System.out.println("Сохранение сведений о правообладателях...");
            System.out.println("Всего записей: " + ownersList.size());
            showInfo("Данные о правообладателях сохранены успешно");
        }
    }
    
    @Override
    public void clearData() {
        ownersList.clear();
        updateStatistics();
    }
    
    // Обработчики событий
    
    @FXML
    private void handleAddOwner() {
        OwnerRecord newOwner = showOwnerDialog(null);
        if (newOwner != null) {
            ownersList.add(newOwner);
        }
    }
    
    @FXML
    private void handleEditOwner() {
        OwnerRecord selected = ownersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите правообладателя для редактирования");
            return;
        }
        
        OwnerRecord edited = showOwnerDialog(selected);
        if (edited != null) {
            int index = ownersList.indexOf(selected);
            ownersList.set(index, edited);
        }
    }
    
    @FXML
    private void handleDeleteOwner() {
        OwnerRecord selected = ownersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите правообладателя для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление правообладателя");
        alert.setContentText("Вы уверены, что хотите удалить запись о \"" + 
                           selected.getOwnerName() + "\"?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ownersList.remove(selected);
            }
        });
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
        alert.setContentText("Вы уверены, что хотите удалить все записи?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearData();
            }
        });
    }
    
    // Вспомогательные методы
    
    private void updateStatistics() {
        int count = ownersList.size();
        double totalShare = calculateTotalShare();
        
        totalOwnersLabel.setText(String.valueOf(count));
        
        if (count == 0) {
            totalShareLabel.setText("0/1");
            shareStatusLabel.setText("Не заполнено");
            shareStatusLabel.setStyle("-fx-text-fill: gray;");
        } else {
            totalShareLabel.setText(String.format("%.4f", totalShare));
            
            if (Math.abs(totalShare - 1.0) < 0.0001) {
                shareStatusLabel.setText("✓ Корректно");
                shareStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else if (totalShare < 1.0) {
                shareStatusLabel.setText("⚠ Неполное");
                shareStatusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                shareStatusLabel.setText("✗ Превышение");
                shareStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }
    }
    
    private double calculateTotalShare() {
        return ownersList.stream()
                .mapToDouble(this::parseShare)
                .sum();
    }
    
    private double parseShare(OwnerRecord owner) {
        String share = owner.getShare();
        if (share == null || share.trim().isEmpty()) {
            return 0.0;
        }
        
        try {
            // Если доля в формате "1/2" или "1/3"
            if (share.contains("/")) {
                String[] parts = share.split("/");
                double numerator = Double.parseDouble(parts[0].trim());
                double denominator = Double.parseDouble(parts[1].trim());
                return numerator / denominator;
            }
            // Если доля в десятичном формате "0.5"
            return Double.parseDouble(share.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private OwnerRecord showOwnerDialog(OwnerRecord existing) {
        Dialog<OwnerRecord> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить правообладателя" : "Редактировать правообладателя");
        dialog.setHeaderText("Введите данные о правообладателе");
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Создание формы
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Дата регистрации права");
        
        TextField documentField = new TextField();
        documentField.setPromptText("№ свидетельства или выписки");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Иванов Иван Иванович");
        
        ComboBox<String> rightsCombo = new ComboBox<>();
        rightsCombo.getItems().addAll(
            "Собственность",
            "Общая долевая собственность",
            "Общая совместная собственность",
            "Пожизненное наследуемое владение",
            "Постоянное (бессрочное) пользование",
            "Аренда"
        );
        rightsCombo.setMaxWidth(Double.MAX_VALUE);
        
        TextField shareField = new TextField();
        shareField.setPromptText("1/2 или 0.5 или 1");
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Дополнительная информация");
        notesArea.setPrefRowCount(3);
        
        // Заполнение существующими данными
        if (existing != null) {
            datePicker.setValue(existing.getRecordDate());
            documentField.setText(existing.getDocumentNumber());
            nameField.setText(existing.getOwnerName());
            rightsCombo.setValue(existing.getRightsType());
            shareField.setText(existing.getShare());
            notesArea.setText(existing.getNotes());
        } else {
            datePicker.setValue(LocalDate.now());
            rightsCombo.getSelectionModel().selectFirst();
        }
        
        // Добавление полей в форму
        grid.add(new Label("Дата регистрации:"), 0, 0);
        grid.add(datePicker, 1, 0);
        
        grid.add(new Label("№ документа:"), 0, 1);
        grid.add(documentField, 1, 1);
        
        grid.add(new Label("ФИО / Организация:"), 0, 2);
        grid.add(nameField, 1, 2);
        
        grid.add(new Label("Вид права:"), 0, 3);
        grid.add(rightsCombo, 1, 3);
        
        grid.add(new Label("Доля:"), 0, 4);
        grid.add(shareField, 1, 4);
        
        grid.add(new Label("Примечание:"), 0, 5);
        grid.add(notesArea, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Конвертация результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().trim().isEmpty()) {
                    showError("ФИО или наименование организации обязательно");
                    return null;
                }
                
                return new OwnerRecord(
                    datePicker.getValue(),
                    documentField.getText(),
                    nameField.getText(),
                    rightsCombo.getValue(),
                    shareField.getText(),
                    notesArea.getText()
                );
            }
            return null;
        });
        
        Optional<OwnerRecord> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
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
    
    // Внутренний класс для данных о правообладателе
    public static class OwnerRecord {
        private final ObjectProperty<LocalDate> recordDate;
        private final StringProperty documentNumber;
        private final StringProperty ownerName;
        private final StringProperty rightsType;
        private final StringProperty share;
        private final StringProperty notes;
        
        public OwnerRecord(LocalDate recordDate, String documentNumber, 
                         String ownerName, String rightsType, 
                         String share, String notes) {
            this.recordDate = new SimpleObjectProperty<>(recordDate);
            this.documentNumber = new SimpleStringProperty(documentNumber);
            this.ownerName = new SimpleStringProperty(ownerName);
            this.rightsType = new SimpleStringProperty(rightsType);
            this.share = new SimpleStringProperty(share);
            this.notes = new SimpleStringProperty(notes);
        }
        
        // Properties
        public ObjectProperty<LocalDate> recordDateProperty() { return recordDate; }
        public StringProperty documentNumberProperty() { return documentNumber; }
        public StringProperty ownerNameProperty() { return ownerName; }
        public StringProperty rightsTypeProperty() { return rightsType; }
        public StringProperty shareProperty() { return share; }
        public StringProperty notesProperty() { return notes; }
        
        // Getters
        public LocalDate getRecordDate() { return recordDate.get(); }
        public String getDocumentNumber() { return documentNumber.get(); }
        public String getOwnerName() { return ownerName.get(); }
        public String getRightsType() { return rightsType.get(); }
        public String getShare() { return share.get(); }
        public String getNotes() { return notes.get(); }
    }
}
