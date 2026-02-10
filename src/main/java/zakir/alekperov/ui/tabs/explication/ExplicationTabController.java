package zakir.alekperov.ui.tabs.explication;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Контроллер вкладки "Экспликация".
 * Отображает сводную информацию о помещениях.
 */
public class ExplicationTabController extends BaseTabController {
    
    @FXML
    private TableView<ExplicationRecord> explicationTable;
    
    @FXML
    private TableColumn<ExplicationRecord, String> literaColumn;
    
    @FXML
    private TableColumn<ExplicationRecord, Integer> floorColumn;
    
    @FXML
    private TableColumn<ExplicationRecord, String> roomNumberColumn;
    
    @FXML
    private TableColumn<ExplicationRecord, String> purposeColumn;
    
    @FXML
    private TableColumn<ExplicationRecord, Double> areaColumn;
    
    @FXML
    private TableColumn<ExplicationRecord, String> categoryColumn;
    
    @FXML
    private ComboBox<String> groupByCombo;
    
    @FXML
    private ComboBox<String> purposeFilterCombo;
    
    @FXML
    private Label livingAreaLabel;
    
    @FXML
    private Label auxiliaryAreaLabel;
    
    @FXML
    private Label totalAreaLabel;
    
    @FXML
    private Label livingRoomsCountLabel;
    
    @FXML
    private Label auxiliaryRoomsCountLabel;
    
    @FXML
    private Label totalRoomsCountLabel;
    
    private final ObservableList<ExplicationRecord> recordsList = 
            FXCollections.observableArrayList();
    
    @Override
    protected void setupBindings() {
        // Настройка колонок таблицы
        literaColumn.setCellValueFactory(cellData -> 
            cellData.getValue().literaProperty());
        
        floorColumn.setCellValueFactory(cellData -> 
            cellData.getValue().floorProperty().asObject());
        
        roomNumberColumn.setCellValueFactory(cellData -> 
            cellData.getValue().roomNumberProperty());
        
        purposeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().purposeProperty());
        
        areaColumn.setCellValueFactory(cellData -> 
            cellData.getValue().areaProperty().asObject());
        
        categoryColumn.setCellValueFactory(cellData -> 
            cellData.getValue().categoryProperty());
        
        // Привязка списка к таблице
        explicationTable.setItems(recordsList);
        
        // Настройка комбобоксов
        groupByCombo.getItems().addAll("Без группировки", "По этажам", "По категориям");
        groupByCombo.getSelectionModel().selectFirst();
        
        purposeFilterCombo.getItems().add("Все типы");
        purposeFilterCombo.getSelectionModel().selectFirst();
        
        // Обновление статистики при изменении
        recordsList.addListener((javafx.collections.ListChangeListener.Change<? extends ExplicationRecord> c) -> {
            updateStatistics();
        });
    }
    
    @Override
    protected void loadInitialData() {
        loadSampleData();
        updateStatistics();
    }
    
    @Override
    public boolean validateData() {
        return true; // Экспликация - только для чтения
    }
    
    @Override
    public void saveData() {
        showInfo("Экспликация формируется автоматически из поэтажного плана");
    }
    
    @Override
    public void clearData() {
        recordsList.clear();
        updateStatistics();
    }
    
    // Обработчики событий
    
    @FXML
    private void handleRefresh() {
        // TODO: Загрузка данных из раздела "Поэтажный план"
        showInfo("Данные обновлены");
        loadSampleData();
    }
    
    @FXML
    private void handleExport() {
        showInfo("Функция экспорта в Excel будет реализована позже");
    }
    
    @FXML
    private void handlePrint() {
        showInfo("Функция печати будет реализована позже");
    }
    
    // Вспомогательные методы
    
    private void loadSampleData() {
        // Временные демо-данные
        recordsList.clear();
        recordsList.addAll(
            new ExplicationRecord("Б", 1, "1", "Жилая комната", 18.5, "Жилая"),
            new ExplicationRecord("Б", 1, "2", "Жилая комната", 16.2, "Жилая"),
            new ExplicationRecord("Б", 1, "3", "Кухня", 12.0, "Вспомогательная"),
            new ExplicationRecord("Б", 1, "4", "Прихожая", 8.3, "Вспомогательная"),
            new ExplicationRecord("Б", 1, "5", "Санузел", 4.5, "Вспомогательная"),
            new ExplicationRecord("Б", 2, "6", "Спальня", 20.0, "Жилая"),
            new ExplicationRecord("Б", 2, "7", "Спальня", 15.5, "Жилая"),
            new ExplicationRecord("Б", 2, "8", "Ванная", 6.8, "Вспомогательная")
        );
    }
    
    private void updateStatistics() {
        // Подсчет площадей
        double livingArea = recordsList.stream()
            .filter(r -> r.getCategory().equals("Жилая"))
            .mapToDouble(ExplicationRecord::getArea)
            .sum();
        
        double auxiliaryArea = recordsList.stream()
            .filter(r -> r.getCategory().equals("Вспомогательная"))
            .mapToDouble(ExplicationRecord::getArea)
            .sum();
        
        double totalArea = livingArea + auxiliaryArea;
        
        // Подсчет количества
        long livingCount = recordsList.stream()
            .filter(r -> r.getCategory().equals("Жилая"))
            .count();
        
        long auxiliaryCount = recordsList.stream()
            .filter(r -> r.getCategory().equals("Вспомогательная"))
            .count();
        
        long totalCount = recordsList.size();
        
        // Обновление меток
        livingAreaLabel.setText(String.format("%.2f кв.м", livingArea));
        auxiliaryAreaLabel.setText(String.format("%.2f кв.м", auxiliaryArea));
        totalAreaLabel.setText(String.format("%.2f кв.м", totalArea));
        
        livingRoomsCountLabel.setText(String.valueOf(livingCount));
        auxiliaryRoomsCountLabel.setText(String.valueOf(auxiliaryCount));
        totalRoomsCountLabel.setText(String.valueOf(totalCount));
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Внутренний класс для записи экспликации
    public static class ExplicationRecord {
        private final StringProperty litera;
        private final IntegerProperty floor;
        private final StringProperty roomNumber;
        private final StringProperty purpose;
        private final DoubleProperty area;
        private final StringProperty category;
        
        public ExplicationRecord(String litera, int floor, String roomNumber,
                               String purpose, double area, String category) {
            this.litera = new SimpleStringProperty(litera);
            this.floor = new SimpleIntegerProperty(floor);
            this.roomNumber = new SimpleStringProperty(roomNumber);
            this.purpose = new SimpleStringProperty(purpose);
            this.area = new SimpleDoubleProperty(area);
            this.category = new SimpleStringProperty(category);
        }
        
        // Properties
        public StringProperty literaProperty() { return litera; }
        public IntegerProperty floorProperty() { return floor; }
        public StringProperty roomNumberProperty() { return roomNumber; }
        public StringProperty purposeProperty() { return purpose; }
        public DoubleProperty areaProperty() { return area; }
        public StringProperty categoryProperty() { return category; }
        
        // Getters
        public String getLitera() { return litera.get(); }
        public int getFloor() { return floor.get(); }
        public String getRoomNumber() { return roomNumber.get(); }
        public String getPurpose() { return purpose.get(); }
        public double getArea() { return area.get(); }
        public String getCategory() { return category.get(); }
    }
}
