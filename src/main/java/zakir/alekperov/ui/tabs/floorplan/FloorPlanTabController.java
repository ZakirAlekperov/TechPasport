package zakir.alekperov.ui.tabs.floorplan;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер вкладки "Поэтажный план".
 */
public class FloorPlanTabController extends BaseTabController {
    
    @FXML
    private TableView<RoomRecord> roomsTable;
    
    @FXML
    private TableColumn<RoomRecord, String> literaColumn;
    
    @FXML
    private TableColumn<RoomRecord, Integer> floorColumn;
    
    @FXML
    private TableColumn<RoomRecord, String> roomNumberColumn;
    
    @FXML
    private TableColumn<RoomRecord, String> purposeColumn;
    
    @FXML
    private TableColumn<RoomRecord, Double> areaColumn;
    
    @FXML
    private TableColumn<RoomRecord, Double> heightColumn;
    
    @FXML
    private TableColumn<RoomRecord, String> wallsColumn;
    
    @FXML
    private TableColumn<RoomRecord, String> floorsColumn;
    
    @FXML
    private TableColumn<RoomRecord, String> notesColumn;
    
    @FXML
    private ComboBox<String> floorFilterCombo;
    
    @FXML
    private Label totalRoomsLabel;
    
    @FXML
    private Label totalAreaLabel;
    
    @FXML
    private Label livingAreaLabel;
    
    @FXML
    private Label auxiliaryAreaLabel;
    
    @FXML
    private Label averageHeightLabel;
    
    private final ObservableList<RoomRecord> roomsList = 
            FXCollections.observableArrayList();
    
    private final ObservableList<RoomRecord> filteredRoomsList = 
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
        
        heightColumn.setCellValueFactory(cellData -> 
            cellData.getValue().heightProperty().asObject());
        
        wallsColumn.setCellValueFactory(cellData -> 
            cellData.getValue().wallMaterialProperty());
        
        floorsColumn.setCellValueFactory(cellData -> 
            cellData.getValue().floorMaterialProperty());
        
        notesColumn.setCellValueFactory(cellData -> 
            cellData.getValue().notesProperty());
        
        // Привязка отфильтрованного списка к таблице
        roomsTable.setItems(filteredRoomsList);
        
        // Обновление при изменении основного списка
        roomsList.addListener((javafx.collections.ListChangeListener.Change<? extends RoomRecord> c) -> {
            updateFilter();
            updateStatistics();
            updateFloorFilter();
        });
        
        // Фильтрация по этажу
        floorFilterCombo.setOnAction(e -> updateFilter());
    }
    
    @Override
    protected void loadInitialData() {
        floorFilterCombo.getItems().add("Все этажи");
        floorFilterCombo.getSelectionModel().selectFirst();
        updateStatistics();
    }
    
    @Override
    public boolean validateData() {
        if (roomsList.isEmpty()) {
            showWarning("Список помещений пуст. Добавьте хотя бы одно помещение.");
            return false;
        }
        return true;
    }
    
    @Override
    public void saveData() {
        if (validateData()) {
            System.out.println("Сохранение поэтажного плана...");
            System.out.println("Всего помещений: " + roomsList.size());
            showInfo("Поэтажный план сохранен успешно");
        }
    }
    
    @Override
    public void clearData() {
        roomsList.clear();
        filteredRoomsList.clear();
        floorFilterCombo.getSelectionModel().selectFirst();
        updateStatistics();
    }
    
    // Обработчики событий
    
    @FXML
    private void handleAddRoom() {
        RoomRecord newRoom = showRoomDialog(null);
        if (newRoom != null) {
            roomsList.add(newRoom);
        }
    }
    
    @FXML
    private void handleEditRoom() {
        RoomRecord selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите помещение для редактирования");
            return;
        }
        
        RoomRecord edited = showRoomDialog(selected);
        if (edited != null) {
            int index = roomsList.indexOf(selected);
            roomsList.set(index, edited);
        }
    }
    
    @FXML
    private void handleDeleteRoom() {
        RoomRecord selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите помещение для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление помещения");
        alert.setContentText("Вы уверены, что хотите удалить помещение № " + 
                           selected.getRoomNumber() + "?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                roomsList.remove(selected);
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
        alert.setContentText("Вы уверены, что хотите удалить все помещения?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearData();
            }
        });
    }
    
    // Вспомогательные методы
    
    private void updateFilter() {
        filteredRoomsList.clear();
        
        String selectedFloor = floorFilterCombo.getValue();
        if (selectedFloor == null || selectedFloor.equals("Все этажи")) {
            filteredRoomsList.addAll(roomsList);
        } else {
            int floor = Integer.parseInt(selectedFloor);
            filteredRoomsList.addAll(
                roomsList.stream()
                    .filter(room -> room.getFloor() == floor)
                    .collect(Collectors.toList())
            );
        }
    }
    
    private void updateFloorFilter() {
        // Получаем уникальные этажи
        List<Integer> floors = roomsList.stream()
            .map(RoomRecord::getFloor)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        String currentSelection = floorFilterCombo.getValue();
        
        floorFilterCombo.getItems().clear();
        floorFilterCombo.getItems().add("Все этажи");
        floors.forEach(floor -> floorFilterCombo.getItems().add(String.valueOf(floor)));
        
        // Восстанавливаем выбор
        if (currentSelection != null && floorFilterCombo.getItems().contains(currentSelection)) {
            floorFilterCombo.setValue(currentSelection);
        } else {
            floorFilterCombo.getSelectionModel().selectFirst();
        }
    }
    
    private void updateStatistics() {
        int count = roomsList.size();
        
        double totalArea = roomsList.stream()
            .mapToDouble(RoomRecord::getArea)
            .sum();
        
        double livingArea = roomsList.stream()
            .filter(room -> isLivingRoom(room.getPurpose()))
            .mapToDouble(RoomRecord::getArea)
            .sum();
        
        double auxiliaryArea = totalArea - livingArea;
        
        double avgHeight = count > 0 
            ? roomsList.stream().mapToDouble(RoomRecord::getHeight).average().orElse(0.0)
            : 0.0;
        
        totalRoomsLabel.setText(String.valueOf(count));
        totalAreaLabel.setText(String.format("%.2f кв.м", totalArea));
        livingAreaLabel.setText(String.format("%.2f кв.м", livingArea));
        auxiliaryAreaLabel.setText(String.format("%.2f кв.м", auxiliaryArea));
        averageHeightLabel.setText(String.format("%.2f м", avgHeight));
    }
    
    private boolean isLivingRoom(String purpose) {
        if (purpose == null) return false;
        String lower = purpose.toLowerCase();
        return lower.contains("жилая") || lower.contains("комната") || 
               lower.contains("спальня") || lower.contains("гостиная");
    }
    
    private RoomRecord showRoomDialog(RoomRecord existing) {
        Dialog<RoomRecord> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить помещение" : "Редактировать помещение");
        dialog.setHeaderText("Введите данные о помещении");
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Создание формы
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField literaField = new TextField();
        literaField.setPromptText("Б, А1 и т.д.");
        
        TextField floorField = new TextField();
        floorField.setPromptText("1, 2, -1 и т.д.");
        
        TextField roomNumberField = new TextField();
        roomNumberField.setPromptText("1, 2, 3 и т.д.");
        
        ComboBox<String> purposeCombo = new ComboBox<>();
        purposeCombo.getItems().addAll(
            "Жилая комната",
            "Спальня",
            "Гостиная",
            "Кухня",
            "Прихожая",
            "Санузел",
            "Ванная",
            "Коридор",
            "Кладовая",
            "Гардеробная",
            "Балкон",
            "Лоджия",
            "Терраса",
            "Котельная",
            "Подвал",
            "Чердак"
        );
        purposeCombo.setEditable(true);
        purposeCombo.setMaxWidth(Double.MAX_VALUE);
        
        TextField areaField = new TextField();
        areaField.setPromptText("15.5");
        
        TextField heightField = new TextField();
        heightField.setPromptText("2.7");
        
        ComboBox<String> wallsCombo = new ComboBox<>();
        wallsCombo.getItems().addAll(
            "Кирпичные",
            "Бетонные",
            "Деревянные",
            "Гипсокартон",
            "Смешанные"
        );
        wallsCombo.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<String> floorsCombo = new ComboBox<>();
        floorsCombo.getItems().addAll(
            "Паркет",
            "Ламинат",
            "Линолеум",
            "Плитка",
            "Деревянные",
            "Бетонные"
        );
        floorsCombo.setMaxWidth(Double.MAX_VALUE);
        
        TextField notesField = new TextField();
        notesField.setPromptText("Дополнительная информация");
        
        // Заполнение существующими данными
        if (existing != null) {
            literaField.setText(existing.getLitera());
            floorField.setText(String.valueOf(existing.getFloor()));
            roomNumberField.setText(existing.getRoomNumber());
            purposeCombo.setValue(existing.getPurpose());
            areaField.setText(String.valueOf(existing.getArea()));
            heightField.setText(String.valueOf(existing.getHeight()));
            wallsCombo.setValue(existing.getWallMaterial());
            floorsCombo.setValue(existing.getFloorMaterial());
            notesField.setText(existing.getNotes());
        }
        
        // Добавление полей в форму
        grid.add(new Label("Литера:"), 0, 0);
        grid.add(literaField, 1, 0);
        
        grid.add(new Label("Этаж:"), 0, 1);
        grid.add(floorField, 1, 1);
        
        grid.add(new Label("№ помещения:"), 0, 2);
        grid.add(roomNumberField, 1, 2);
        
        grid.add(new Label("Назначение:"), 0, 3);
        grid.add(purposeCombo, 1, 3);
        
        grid.add(new Label("Площадь (кв.м):"), 0, 4);
        grid.add(areaField, 1, 4);
        
        grid.add(new Label("Высота (м):"), 0, 5);
        grid.add(heightField, 1, 5);
        
        grid.add(new Label("Материал стен:"), 0, 6);
        grid.add(wallsCombo, 1, 6);
        
        grid.add(new Label("Материал полов:"), 0, 7);
        grid.add(floorsCombo, 1, 7);
        
        grid.add(new Label("Примечание:"), 0, 8);
        grid.add(notesField, 1, 8);
        
        dialog.getDialogPane().setContent(grid);
        
        // Конвертация результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new RoomRecord(
                        literaField.getText(),
                        Integer.parseInt(floorField.getText()),
                        roomNumberField.getText(),
                        purposeCombo.getValue(),
                        Double.parseDouble(areaField.getText()),
                        Double.parseDouble(heightField.getText()),
                        wallsCombo.getValue(),
                        floorsCombo.getValue(),
                        notesField.getText()
                    );
                } catch (NumberFormatException e) {
                    showError("Ошибка ввода данных. Проверьте числовые поля.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<RoomRecord> result = dialog.showAndWait();
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
    
    // Внутренний класс для данных помещения
    public static class RoomRecord {
        private final StringProperty litera;
        private final IntegerProperty floor;
        private final StringProperty roomNumber;
        private final StringProperty purpose;
        private final DoubleProperty area;
        private final DoubleProperty height;
        private final StringProperty wallMaterial;
        private final StringProperty floorMaterial;
        private final StringProperty notes;
        
        public RoomRecord(String litera, int floor, String roomNumber,
                         String purpose, double area, double height,
                         String wallMaterial, String floorMaterial, String notes) {
            this.litera = new SimpleStringProperty(litera);
            this.floor = new SimpleIntegerProperty(floor);
            this.roomNumber = new SimpleStringProperty(roomNumber);
            this.purpose = new SimpleStringProperty(purpose);
            this.area = new SimpleDoubleProperty(area);
            this.height = new SimpleDoubleProperty(height);
            this.wallMaterial = new SimpleStringProperty(wallMaterial);
            this.floorMaterial = new SimpleStringProperty(floorMaterial);
            this.notes = new SimpleStringProperty(notes);
        }
        
        // Properties
        public StringProperty literaProperty() { return litera; }
        public IntegerProperty floorProperty() { return floor; }
        public StringProperty roomNumberProperty() { return roomNumber; }
        public StringProperty purposeProperty() { return purpose; }
        public DoubleProperty areaProperty() { return area; }
        public DoubleProperty heightProperty() { return height; }
        public StringProperty wallMaterialProperty() { return wallMaterial; }
        public StringProperty floorMaterialProperty() { return floorMaterial; }
        public StringProperty notesProperty() { return notes; }
        
        // Getters
        public String getLitera() { return litera.get(); }
        public int getFloor() { return floor.get(); }
        public String getRoomNumber() { return roomNumber.get(); }
        public String getPurpose() { return purpose.get(); }
        public double getArea() { return area.get(); }
        public double getHeight() { return height.get(); }
        public String getWallMaterial() { return wallMaterial.get(); }
        public String getFloorMaterial() { return floorMaterial.get(); }
        public String getNotes() { return notes.get(); }
    }
}
