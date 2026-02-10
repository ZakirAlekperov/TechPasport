package zakir.alekperov.ui.tabs.inspection;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

/**
 * Контроллер вкладки "Отметки об обследованиях".
 */
public class InspectionTabController extends BaseTabController {
    
    @FXML
    private TableView<InspectionRecord> inspectionsTable;
    
    @FXML
    private TableColumn<InspectionRecord, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<InspectionRecord, String> typeColumn;
    
    @FXML
    private TableColumn<InspectionRecord, String> organizationColumn;
    
    @FXML
    private TableColumn<InspectionRecord, String> documentColumn;
    
    @FXML
    private TableColumn<InspectionRecord, String> defectsColumn;
    
    @FXML
    private TableColumn<InspectionRecord, String> recommendationsColumn;
    
    @FXML
    private TableColumn<InspectionRecord, String> statusColumn;
    
    @FXML
    private ComboBox<String> typeFilterCombo;
    
    @FXML
    private Label totalInspectionsLabel;
    
    @FXML
    private Label lastInspectionLabel;
    
    @FXML
    private Label lastRepairLabel;
    
    @FXML
    private Label attentionRequiredLabel;
    
    @FXML
    private TextArea generalNotesArea;
    
    private final ObservableList<InspectionRecord> inspectionsList = 
            FXCollections.observableArrayList();
    
    private final ObservableList<InspectionRecord> filteredInspectionsList = 
            FXCollections.observableArrayList();
    
    @Override
    protected void setupBindings() {
        // Настройка колонок таблицы
        dateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().dateProperty());
        
        typeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().typeProperty());
        
        organizationColumn.setCellValueFactory(cellData -> 
            cellData.getValue().organizationProperty());
        
        documentColumn.setCellValueFactory(cellData -> 
            cellData.getValue().documentProperty());
        
        defectsColumn.setCellValueFactory(cellData -> 
            cellData.getValue().defectsProperty());
        
        recommendationsColumn.setCellValueFactory(cellData -> 
            cellData.getValue().recommendationsProperty());
        
        statusColumn.setCellValueFactory(cellData -> 
            cellData.getValue().statusProperty());
        
        // Привязка отфильтрованного списка к таблице
        inspectionsTable.setItems(filteredInspectionsList);
        
        // Обновление при изменении списка
        inspectionsList.addListener((javafx.collections.ListChangeListener.Change<? extends InspectionRecord> c) -> {
            updateFilter();
            updateStatistics();
        });
        
        // Настройка фильтра
        typeFilterCombo.getItems().addAll(
            "Все типы",
            "Техническое обследование",
            "Капитальный ремонт",
            "Текущий ремонт",
            "Аварийные работы",
            "Реконструкция"
        );
        typeFilterCombo.getSelectionModel().selectFirst();
        typeFilterCombo.setOnAction(e -> updateFilter());
    }
    
    @Override
    protected void loadInitialData() {
        updateStatistics();
    }
    
    @Override
    public boolean validateData() {
        // Обследования не обязательны
        return true;
    }
    
    @Override
    public void saveData() {
        System.out.println("Сохранение отметок об обследованиях...");
        System.out.println("Всего записей: " + inspectionsList.size());
        showInfo("Данные об обследованиях сохранены успешно");
    }
    
    @Override
    public void clearData() {
        inspectionsList.clear();
        filteredInspectionsList.clear();
        generalNotesArea.clear();
        updateStatistics();
    }
    
    // Обработчики событий
    
    @FXML
    private void handleAddInspection() {
        InspectionRecord newInspection = showInspectionDialog(null);
        if (newInspection != null) {
            inspectionsList.add(newInspection);
        }
    }
    
    @FXML
    private void handleEditInspection() {
        InspectionRecord selected = inspectionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите запись для редактирования");
            return;
        }
        
        InspectionRecord edited = showInspectionDialog(selected);
        if (edited != null) {
            int index = inspectionsList.indexOf(selected);
            inspectionsList.set(index, edited);
        }
    }
    
    @FXML
    private void handleDeleteInspection() {
        InspectionRecord selected = inspectionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите запись для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление записи");
        alert.setContentText("Вы уверены, что хотите удалить запись от " + 
                           selected.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                inspectionsList.remove(selected);
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
    
    private void updateFilter() {
        filteredInspectionsList.clear();
        
        String selectedType = typeFilterCombo.getValue();
        if (selectedType == null || selectedType.equals("Все типы")) {
            filteredInspectionsList.addAll(inspectionsList);
        } else {
            filteredInspectionsList.addAll(
                inspectionsList.stream()
                    .filter(record -> record.getType().equals(selectedType))
                    .collect(java.util.stream.Collectors.toList())
            );
        }
        
        // Сортировка по дате (новые сверху)
        filteredInspectionsList.sort(Comparator.comparing(InspectionRecord::getDate).reversed());
    }
    
    private void updateStatistics() {
        totalInspectionsLabel.setText(String.valueOf(inspectionsList.size()));
        
        // Последнее обследование
        Optional<InspectionRecord> lastInspection = inspectionsList.stream()
            .filter(r -> r.getType().contains("обследование"))
            .max(Comparator.comparing(InspectionRecord::getDate));
        
        if (lastInspection.isPresent()) {
            lastInspectionLabel.setText(
                lastInspection.get().getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            );
        } else {
            lastInspectionLabel.setText("Не проводилось");
        }
        
        // Последний ремонт
        Optional<InspectionRecord> lastRepair = inspectionsList.stream()
            .filter(r -> r.getType().contains("ремонт"))
            .max(Comparator.comparing(InspectionRecord::getDate));
        
        if (lastRepair.isPresent()) {
            lastRepairLabel.setText(
                lastRepair.get().getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            );
        } else {
            lastRepairLabel.setText("Не проводился");
        }
        
        // Записи требующие внимания
        long attentionCount = inspectionsList.stream()
            .filter(r -> r.getStatus().equals("Требуется внимание") || 
                        r.getStatus().equals("Критично"))
            .count();
        
        attentionRequiredLabel.setText(attentionCount + " записей");
        if (attentionCount > 0) {
            attentionRequiredLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            attentionRequiredLabel.setStyle("-fx-text-fill: green;");
        }
    }
    
    private InspectionRecord showInspectionDialog(InspectionRecord existing) {
        Dialog<InspectionRecord> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить запись" : "Редактировать запись");
        dialog.setHeaderText("Введите данные об обследовании или ремонте");
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Создание формы
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Дата проведения");
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
            "Техническое обследование",
            "Капитальный ремонт",
            "Текущий ремонт",
            "Аварийные работы",
            "Реконструкция"
        );
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        
        TextField organizationField = new TextField();
        organizationField.setPromptText("Название организации");
        
        TextField documentField = new TextField();
        documentField.setPromptText("Акт № или др. документ");
        
        TextArea defectsArea = new TextArea();
        defectsArea.setPromptText("Описание выявленных дефектов");
        defectsArea.setPrefRowCount(3);
        
        TextArea recommendationsArea = new TextArea();
        recommendationsArea.setPromptText("Рекомендации по устранению");
        recommendationsArea.setPrefRowCount(3);
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
            "Завершено",
            "В работе",
            "Требуется внимание",
            "Критично",
            "Отложено"
        );
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        
        // Заполнение существующими данными
        if (existing != null) {
            datePicker.setValue(existing.getDate());
            typeCombo.setValue(existing.getType());
            organizationField.setText(existing.getOrganization());
            documentField.setText(existing.getDocument());
            defectsArea.setText(existing.getDefects());
            recommendationsArea.setText(existing.getRecommendations());
            statusCombo.setValue(existing.getStatus());
        } else {
            datePicker.setValue(LocalDate.now());
            statusCombo.getSelectionModel().selectFirst();
        }
        
        // Добавление полей в форму
        grid.add(new Label("Дата:"), 0, 0);
        grid.add(datePicker, 1, 0);
        
        grid.add(new Label("Тип работ:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        
        grid.add(new Label("Организация:"), 0, 2);
        grid.add(organizationField, 1, 2);
        
        grid.add(new Label("Документ:"), 0, 3);
        grid.add(documentField, 1, 3);
        
        grid.add(new Label("Выявленные дефекты:"), 0, 4);
        grid.add(defectsArea, 1, 4);
        
        grid.add(new Label("Рекомендации:"), 0, 5);
        grid.add(recommendationsArea, 1, 5);
        
        grid.add(new Label("Статус:"), 0, 6);
        grid.add(statusCombo, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        // Конвертация результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (datePicker.getValue() == null || typeCombo.getValue() == null) {
                    showError("Заполните обязательные поля: Дата и Тип работ");
                    return null;
                }
                
                return new InspectionRecord(
                    datePicker.getValue(),
                    typeCombo.getValue(),
                    organizationField.getText(),
                    documentField.getText(),
                    defectsArea.getText(),
                    recommendationsArea.getText(),
                    statusCombo.getValue()
                );
            }
            return null;
        });
        
        Optional<InspectionRecord> result = dialog.showAndWait();
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
    
    // Внутренний класс для записи об обследовании
    public static class InspectionRecord {
        private final ObjectProperty<LocalDate> date;
        private final StringProperty type;
        private final StringProperty organization;
        private final StringProperty document;
        private final StringProperty defects;
        private final StringProperty recommendations;
        private final StringProperty status;
        
        public InspectionRecord(LocalDate date, String type, String organization,
                              String document, String defects, String recommendations,
                              String status) {
            this.date = new SimpleObjectProperty<>(date);
            this.type = new SimpleStringProperty(type);
            this.organization = new SimpleStringProperty(organization);
            this.document = new SimpleStringProperty(document);
            this.defects = new SimpleStringProperty(defects);
            this.recommendations = new SimpleStringProperty(recommendations);
            this.status = new SimpleStringProperty(status);
        }
        
        // Properties
        public ObjectProperty<LocalDate> dateProperty() { return date; }
        public StringProperty typeProperty() { return type; }
        public StringProperty organizationProperty() { return organization; }
        public StringProperty documentProperty() { return document; }
        public StringProperty defectsProperty() { return defects; }
        public StringProperty recommendationsProperty() { return recommendations; }
        public StringProperty statusProperty() { return status; }
        
        // Getters
        public LocalDate getDate() { return date.get(); }
        public String getType() { return type.get(); }
        public String getOrganization() { return organization.get(); }
        public String getDocument() { return document.get(); }
        public String getDefects() { return defects.get(); }
        public String getRecommendations() { return recommendations.get(); }
        public String getStatus() { return status.get(); }
    }
}
