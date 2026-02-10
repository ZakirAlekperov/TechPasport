package zakir.alekperov.ui.tabs.objectcomposition;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.util.Optional;

/**
 * Контроллер вкладки "Состав объекта".
 * Управляет таблицей зданий и сооружений на участке.
 */
public class ObjectCompositionTabController extends BaseTabController {
    
    @FXML
    private TableView<BuildingComponent> buildingsTable;
    
    @FXML
    private TableColumn<BuildingComponent, String> literaColumn;
    
    @FXML
    private TableColumn<BuildingComponent, String> nameColumn;
    
    @FXML
    private TableColumn<BuildingComponent, Integer> yearColumn;
    
    @FXML
    private TableColumn<BuildingComponent, String> wallMaterialColumn;
    
    @FXML
    private TableColumn<BuildingComponent, Double> areaColumn;
    
    @FXML
    private TableColumn<BuildingComponent, Double> heightColumn;
    
    @FXML
    private TableColumn<BuildingComponent, Double> volumeColumn;
    
    @FXML
    private TableColumn<BuildingComponent, Double> costColumn;
    
    @FXML
    private Label totalBuildingsLabel;
    
    @FXML
    private Label totalAreaLabel;
    
    @FXML
    private Label totalVolumeLabel;
    
    @FXML
    private Label totalCostLabel;
    
    private final ObservableList<BuildingComponent> buildingsList = 
            FXCollections.observableArrayList();
    
    @Override
    protected void setupBindings() {
        // Настройка колонок таблицы
        literaColumn.setCellValueFactory(cellData -> 
            cellData.getValue().literaProperty());
        
        nameColumn.setCellValueFactory(cellData -> 
            cellData.getValue().nameProperty());
        
        yearColumn.setCellValueFactory(cellData -> 
            cellData.getValue().yearProperty().asObject());
        
        wallMaterialColumn.setCellValueFactory(cellData -> 
            cellData.getValue().wallMaterialProperty());
        
        areaColumn.setCellValueFactory(cellData -> 
            cellData.getValue().areaProperty().asObject());
        
        heightColumn.setCellValueFactory(cellData -> 
            cellData.getValue().heightProperty().asObject());
        
        volumeColumn.setCellValueFactory(cellData -> 
            cellData.getValue().volumeProperty().asObject());
        
        costColumn.setCellValueFactory(cellData -> 
            cellData.getValue().costProperty().asObject());
        
        // Привязка списка к таблице
        buildingsTable.setItems(buildingsList);
        
        // Обновление итогов при изменении списка
        buildingsList.addListener((javafx.collections.ListChangeListener.Change<? extends BuildingComponent> c) -> {
            updateTotals();
        });
    }
    
    @Override
    protected void loadInitialData() {
        updateTotals();
    }
    
    @Override
    public boolean validateData() {
        if (buildingsList.isEmpty()) {
            showWarning("Список зданий пуст. Добавьте хотя бы одно здание.");
            return false;
        }
        return true;
    }
    
    @Override
    public void saveData() {
        if (validateData()) {
            System.out.println("Сохранение состава объекта...");
            System.out.println("Всего зданий: " + buildingsList.size());
            showInfo("Данные о составе объекта сохранены успешно");
        }
    }
    
    @Override
    public void clearData() {
        buildingsList.clear();
        updateTotals();
    }
    
    // Обработчики событий
    
    @FXML
    private void handleAddBuilding() {
        BuildingComponent newBuilding = showBuildingDialog(null);
        if (newBuilding != null) {
            buildingsList.add(newBuilding);
        }
    }
    
    @FXML
    private void handleEditBuilding() {
        BuildingComponent selected = buildingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите здание для редактирования");
            return;
        }
        
        BuildingComponent edited = showBuildingDialog(selected);
        if (edited != null) {
            int index = buildingsList.indexOf(selected);
            buildingsList.set(index, edited);
        }
    }
    
    @FXML
    private void handleDeleteBuilding() {
        BuildingComponent selected = buildingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите здание для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление здания");
        alert.setContentText("Вы уверены, что хотите удалить здание \"" + 
                           selected.getName() + "\"?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                buildingsList.remove(selected);
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
        alert.setContentText("Вы уверены, что хотите удалить все здания?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearData();
            }
        });
    }
    
    // Вспомогательные методы
    
    private void updateTotals() {
        int count = buildingsList.size();
        double totalArea = buildingsList.stream()
                .mapToDouble(BuildingComponent::getArea)
                .sum();
        double totalVolume = buildingsList.stream()
                .mapToDouble(BuildingComponent::getVolume)
                .sum();
        double totalCost = buildingsList.stream()
                .mapToDouble(BuildingComponent::getCost)
                .sum();
        
        totalBuildingsLabel.setText(String.valueOf(count));
        totalAreaLabel.setText(String.format("%.2f кв.м", totalArea));
        totalVolumeLabel.setText(String.format("%.2f куб.м", totalVolume));
        totalCostLabel.setText(String.format("%.2f руб.", totalCost));
    }
    
    private BuildingComponent showBuildingDialog(BuildingComponent existing) {
        Dialog<BuildingComponent> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить здание" : "Редактировать здание");
        dialog.setHeaderText("Введите данные о здании");
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Создание формы
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField literaField = new TextField();
        literaField.setPromptText("Б, Б1, и т.д.");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Индивидуальный жилой дом");
        
        TextField yearField = new TextField();
        yearField.setPromptText("2024");
        
        ComboBox<String> materialCombo = new ComboBox<>();
        materialCombo.getItems().addAll(
            "Блоки облицованные кирпичом",
            "Кирпичные",
            "ж/б блоки",
            "Деревянные",
            "Панельные",
            "Монолитные"
        );
        materialCombo.setMaxWidth(Double.MAX_VALUE);
        
        TextField areaField = new TextField();
        areaField.setPromptText("108.0");
        
        TextField heightField = new TextField();
        heightField.setPromptText("6.15");
        
        TextField volumeField = new TextField();
        volumeField.setPromptText("664");
        
        TextField costField = new TextField();
        costField.setPromptText("567312");
        
        // Заполнение существующими данными
        if (existing != null) {
            literaField.setText(existing.getLitera());
            nameField.setText(existing.getName());
            yearField.setText(String.valueOf(existing.getYear()));
            materialCombo.setValue(existing.getWallMaterial());
            areaField.setText(String.valueOf(existing.getArea()));
            heightField.setText(String.valueOf(existing.getHeight()));
            volumeField.setText(String.valueOf(existing.getVolume()));
            costField.setText(String.valueOf(existing.getCost()));
        }
        
        // Добавление полей в форму
        grid.add(new Label("Литера:"), 0, 0);
        grid.add(literaField, 1, 0);
        
        grid.add(new Label("Наименование:"), 0, 1);
        grid.add(nameField, 1, 1);
        
        grid.add(new Label("Год постройки:"), 0, 2);
        grid.add(yearField, 1, 2);
        
        grid.add(new Label("Материал стен:"), 0, 3);
        grid.add(materialCombo, 1, 3);
        
        grid.add(new Label("Площадь (кв.м):"), 0, 4);
        grid.add(areaField, 1, 4);
        
        grid.add(new Label("Высота (м):"), 0, 5);
        grid.add(heightField, 1, 5);
        
        grid.add(new Label("Объем (куб.м):"), 0, 6);
        grid.add(volumeField, 1, 6);
        
        grid.add(new Label("Стоимость (руб.):"), 0, 7);
        grid.add(costField, 1, 7);
        
        dialog.getDialogPane().setContent(grid);
        
        // Валидация и конвертация результата
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new BuildingComponent(
                        literaField.getText(),
                        nameField.getText(),
                        Integer.parseInt(yearField.getText()),
                        materialCombo.getValue(),
                        Double.parseDouble(areaField.getText()),
                        Double.parseDouble(heightField.getText()),
                        Double.parseDouble(volumeField.getText()),
                        Double.parseDouble(costField.getText())
                    );
                } catch (NumberFormatException e) {
                    showError("Ошибка ввода данных. Проверьте числовые поля.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<BuildingComponent> result = dialog.showAndWait();
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
    
    // Внутренний класс для данных здания
    public static class BuildingComponent {
        private final StringProperty litera;
        private final StringProperty name;
        private final IntegerProperty year;
        private final StringProperty wallMaterial;
        private final DoubleProperty area;
        private final DoubleProperty height;
        private final DoubleProperty volume;
        private final DoubleProperty cost;
        
        public BuildingComponent(String litera, String name, int year, 
                               String wallMaterial, double area, double height,
                               double volume, double cost) {
            this.litera = new SimpleStringProperty(litera);
            this.name = new SimpleStringProperty(name);
            this.year = new SimpleIntegerProperty(year);
            this.wallMaterial = new SimpleStringProperty(wallMaterial);
            this.area = new SimpleDoubleProperty(area);
            this.height = new SimpleDoubleProperty(height);
            this.volume = new SimpleDoubleProperty(volume);
            this.cost = new SimpleDoubleProperty(cost);
        }
        
        // Properties
        public StringProperty literaProperty() { return litera; }
        public StringProperty nameProperty() { return name; }
        public IntegerProperty yearProperty() { return year; }
        public StringProperty wallMaterialProperty() { return wallMaterial; }
        public DoubleProperty areaProperty() { return area; }
        public DoubleProperty heightProperty() { return height; }
        public DoubleProperty volumeProperty() { return volume; }
        public DoubleProperty costProperty() { return cost; }
        
        // Getters
        public String getLitera() { return litera.get(); }
        public String getName() { return name.get(); }
        public int getYear() { return year.get(); }
        public String getWallMaterial() { return wallMaterial.get(); }
        public double getArea() { return area.get(); }
        public double getHeight() { return height.get(); }
        public double getVolume() { return volume.get(); }
        public double getCost() { return cost.get(); }
    }
}
