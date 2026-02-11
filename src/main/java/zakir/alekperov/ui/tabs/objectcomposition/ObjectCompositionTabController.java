package zakir.alekperov.ui.tabs.objectcomposition;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import zakir.alekperov.model.Building;
import zakir.alekperov.ui.dialogs.BuildingDialog;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.util.Optional;

public class ObjectCompositionTabController extends BaseTabController {
    
    @FXML private TableView<Building> buildingsTable;
    @FXML private TableColumn<Building, String> letterColumn;
    @FXML private TableColumn<Building, String> nameColumn;
    @FXML private TableColumn<Building, Integer> yearColumn;
    @FXML private TableColumn<Building, String> materialColumn;
    @FXML private TableColumn<Building, Double> areaColumn;
    @FXML private TableColumn<Building, Double> heightColumn;
    @FXML private TableColumn<Building, Double> volumeColumn;
    @FXML private TableColumn<Building, Double> valueColumn;
    @FXML private TableColumn<Building, Void> actionsColumn;
    
    @FXML private Label totalBuildingsLabel;
    @FXML private Label totalAreaLabel;
    @FXML private Label totalVolumeLabel;
    @FXML private Label totalValueLabel;
    
    private ObservableList<Building> buildings;
    
    @Override
    protected void setupBindings() {
        buildings = FXCollections.observableArrayList();
        buildingsTable.setItems(buildings);
        
        setupTableColumns();
        setupTotalsListeners();
    }
    
    private void setupTableColumns() {
        // Привязываем колонки к свойствам модели
        letterColumn.setCellValueFactory(new PropertyValueFactory<>("letter"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("yearBuilt"));
        materialColumn.setCellValueFactory(new PropertyValueFactory<>("wallMaterial"));
        areaColumn.setCellValueFactory(new PropertyValueFactory<>("buildingArea"));
        heightColumn.setCellValueFactory(new PropertyValueFactory<>("height"));
        volumeColumn.setCellValueFactory(new PropertyValueFactory<>("volume"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("inventoryValue"));
        
        // Форматирование чисел
        areaColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f", item));
            }
        });
        
        heightColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f", item));
            }
        });
        
        volumeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f", item));
            }
        });
        
        valueColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
        
        // Колонка с кнопками действий
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑️");
            private final HBox buttons = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setOnAction(event -> {
                    Building building = getTableView().getItems().get(getIndex());
                    handleEditBuilding(building);
                });
                
                deleteButton.setOnAction(event -> {
                    Building building = getTableView().getItems().get(getIndex());
                    handleDeleteBuilding(building);
                });
                
                editButton.setStyle("-fx-cursor: hand;");
                deleteButton.setStyle("-fx-cursor: hand;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }
    
    private void setupTotalsListeners() {
        // Автоматически обновляем итоги при изменении списка
        buildings.addListener((javafx.collections.ListChangeListener<Building>) change -> {
            updateTotals();
        });
    }
    
    private void updateTotals() {
        int count = buildings.size();
        double totalArea = buildings.stream()
            .mapToDouble(Building::getBuildingArea)
            .sum();
        double totalVolume = buildings.stream()
            .mapToDouble(Building::getVolume)
            .sum();
        double totalValue = buildings.stream()
            .mapToDouble(Building::getInventoryValue)
            .sum();
        
        totalBuildingsLabel.setText(String.valueOf(count));
        totalAreaLabel.setText(String.format("%.1f кв.м", totalArea));
        totalVolumeLabel.setText(String.format("%.1f куб.м", totalVolume));
        totalValueLabel.setText(String.format("%.2f руб.", totalValue));
    }
    
    @FXML
    private void handleAddBuilding() {
        BuildingDialog dialog = new BuildingDialog(null);
        Optional<Building> result = dialog.showAndWait();
        
        result.ifPresent(building -> {
            buildings.add(building);
            buildingsTable.refresh();
        });
    }
    
    private void handleEditBuilding(Building building) {
        BuildingDialog dialog = new BuildingDialog(building);
        Optional<Building> result = dialog.showAndWait();
        
        result.ifPresent(updatedBuilding -> {
            int index = buildings.indexOf(building);
            buildings.set(index, updatedBuilding);
            buildingsTable.refresh();
        });
    }
    
    private void handleDeleteBuilding(Building building) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить строение?");
        alert.setContentText("Вы уверены, что хотите удалить:\n" + 
            building.getName() + " (литера " + building.getLetter() + ")?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            buildings.remove(building);
            buildingsTable.refresh();
        }
    }
    
    @FXML
    private void handleClear() {
        if (buildings.isEmpty()) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Очистить все данные?");
        alert.setContentText("Вы уверены, что хотите удалить все строения?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            clearData();
        }
    }
    
    @FXML
    private void handleSave() {
        if (!validateData()) {
            return;
        }
        saveData();
    }
    
    @Override
    public boolean validateData() {
        if (buildings.isEmpty()) {
            showWarning("Добавьте хотя бы одно строение");
            return false;
        }
        return true;
    }
    
    @Override
    public void saveData() {
        StringBuilder report = new StringBuilder();
        report.append("=== РАЗДЕЛ 2. СОСТАВ ОБЪЕКТА ===\n\n");
        
        for (Building building : buildings) {
            report.append(building.toString()).append("\n");
        }
        
        report.append("\n--- ИТОГО ---\n");
        report.append("Количество строений: ").append(buildings.size()).append("\n");
        report.append(totalAreaLabel.getText()).append("\n");
        report.append(totalVolumeLabel.getText()).append("\n");
        report.append(totalValueLabel.getText()).append("\n");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сохранено");
        alert.setHeaderText("Раздел 2. Состав объекта сохранен");
        alert.setContentText(report.toString());
        alert.getDialogPane().setPrefWidth(700);
        alert.showAndWait();
        
        System.out.println(report);
    }
    
    @Override
    public void clearData() {
        buildings.clear();
        buildingsTable.refresh();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
