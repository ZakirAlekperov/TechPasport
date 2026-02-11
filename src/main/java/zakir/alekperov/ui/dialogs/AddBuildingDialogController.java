package zakir.alekperov.ui.dialogs;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер диалога добавления/редактирования здания.
 * Позволяет ввести литеру, описание и координаты точек.
 */
public class AddBuildingDialogController {
    
    @FXML private TextField literaField;
    @FXML private TextField descriptionField;
    @FXML private TableView<CoordinatePoint> coordinatesTable;
    @FXML private TableColumn<CoordinatePoint, Integer> pointNumberColumn;
    @FXML private TableColumn<CoordinatePoint, Double> xCoordColumn;
    @FXML private TableColumn<CoordinatePoint, Double> yCoordColumn;
    @FXML private TableColumn<CoordinatePoint, Void> actionColumn;
    @FXML private Label pointCountLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button addPointButton;
    
    private ObservableList<CoordinatePoint> points = FXCollections.observableArrayList();
    private boolean savedSuccessfully = false;
    private Stage dialogStage;
    private boolean isEditMode = false;
    
    /**
     * Результат диалога - данные нового здания.
     */
    public static class BuildingData {
        private final String litera;
        private final String description;
        private final List<Point> points;
        
        public BuildingData(String litera, String description, List<Point> points) {
            this.litera = litera;
            this.description = description;
            this.points = points;
        }
        
        public String getLitera() { return litera; }
        public String getDescription() { return description; }
        public List<Point> getPoints() { return points; }
    }
    
    /**
     * Точка с координатами.
     */
    public static class Point {
        private final double x;
        private final double y;
        
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public double getX() { return x; }
        public double getY() { return y; }
    }
    
    /**
     * Модель точки для TableView.
     */
    public static class CoordinatePoint {
        private final SimpleIntegerProperty pointNumber;
        private final SimpleDoubleProperty x;
        private final SimpleDoubleProperty y;
        
        public CoordinatePoint(int pointNumber, double x, double y) {
            this.pointNumber = new SimpleIntegerProperty(pointNumber);
            this.x = new SimpleDoubleProperty(x);
            this.y = new SimpleDoubleProperty(y);
        }
        
        public int getPointNumber() { return pointNumber.get(); }
        public void setPointNumber(int value) { pointNumber.set(value); }
        public SimpleIntegerProperty pointNumberProperty() { return pointNumber; }
        
        public double getX() { return x.get(); }
        public void setX(double value) { x.set(value); }
        public SimpleDoubleProperty xProperty() { return x; }
        
        public double getY() { return y.get(); }
        public void setY(double value) { y.set(value); }
        public SimpleDoubleProperty yProperty() { return y; }
    }
    
    @FXML
    private void initialize() {
        setupTable();
        updatePointCount();
        
        // Добавить 3 пустые точки по умолчанию (только для нового здания)
        // Если редактирование, точки будут загружены через setExistingBuilding()
        if (!isEditMode) {
            addPoint();
            addPoint();
            addPoint();
        }
    }
    
    private void setupTable() {
        // Настройка колонок
        pointNumberColumn.setCellValueFactory(cellData -> 
            cellData.getValue().pointNumberProperty().asObject());
        
        xCoordColumn.setCellValueFactory(cellData -> 
            cellData.getValue().xProperty().asObject());
        xCoordColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        xCoordColumn.setOnEditCommit(event -> {
            CoordinatePoint point = event.getRowValue();
            point.setX(event.getNewValue());
        });
        
        yCoordColumn.setCellValueFactory(cellData -> 
            cellData.getValue().yProperty().asObject());
        yCoordColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        yCoordColumn.setOnEditCommit(event -> {
            CoordinatePoint point = event.getRowValue();
            point.setY(event.getNewValue());
        });
        
        // Колонка с кнопкой удаления
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑️");
            
            {
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteButton.setOnAction(event -> {
                    CoordinatePoint point = getTableView().getItems().get(getIndex());
                    removePoint(point);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        
        coordinatesTable.setItems(points);
    }
    
    /**
     * Установить существующие данные здания для редактирования.
     */
    public void setExistingBuilding(String litera, String description, List<Point> existingPoints) {
        isEditMode = true;
        
        if (literaField != null) {
            literaField.setText(litera);
            literaField.setDisable(true); // Литеру нельзя изменить при редактировании
        }
        
        if (descriptionField != null) {
            descriptionField.setText(description);
        }
        
        // Очистить текущие точки и загрузить существующие
        points.clear();
        int pointNum = 1;
        for (Point p : existingPoints) {
            points.add(new CoordinatePoint(pointNum++, p.getX(), p.getY()));
        }
        
        updatePointCount();
    }
    
    @FXML
    private void handleAddPoint() {
        addPoint();
    }
    
    private void addPoint() {
        int nextNumber = points.size() + 1;
        points.add(new CoordinatePoint(nextNumber, 0.0, 0.0));
        updatePointCount();
    }
    
    private void removePoint(CoordinatePoint point) {
        if (points.size() <= 3) {
            showWarning("Нельзя удалить", "Минимум 3 точки для контура здания");
            return;
        }
        
        points.remove(point);
        // Перенумеровать точки
        for (int i = 0; i < points.size(); i++) {
            points.get(i).setPointNumber(i + 1);
        }
        updatePointCount();
    }
    
    private void updatePointCount() {
        if (pointCountLabel != null) {
            pointCountLabel.setText(String.valueOf(points.size()));
        }
    }
    
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        savedSuccessfully = true;
        closeDialog();
    }
    
    @FXML
    private void handleCancel() {
        savedSuccessfully = false;
        closeDialog();
    }
    
    private boolean validateInput() {
        // Проверка литеры
        if (literaField.getText() == null || literaField.getText().trim().isEmpty()) {
            showWarning("Ошибка валидации", "Укажите литеру здания");
            literaField.requestFocus();
            return false;
        }
        
        // Проверка описания
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            showWarning("Ошибка валидации", "Укажите описание здания");
            descriptionField.requestFocus();
            return false;
        }
        
        // Проверка количества точек
        if (points.size() < 3) {
            showWarning("Ошибка валидации", "Минимум 3 точки для контура здания");
            return false;
        }
        
        // Проверка координат
        for (CoordinatePoint point : points) {
            if (point.getX() == 0.0 && point.getY() == 0.0) {
                showWarning("Ошибка валидации", 
                    "Все точки должны иметь координаты.\nТочка №" + point.getPointNumber() + " не заполнена");
                return false;
            }
        }
        
        return true;
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isSavedSuccessfully() {
        return savedSuccessfully;
    }
    
    public boolean isEditMode() {
        return isEditMode;
    }
    
    /**
     * Получить данные здания.
     */
    public BuildingData getBuildingData() {
        if (!savedSuccessfully) {
            return null;
        }
        
        String litera = literaField.getText().trim();
        String description = descriptionField.getText().trim();
        
        List<Point> pointList = new ArrayList<>();
        for (CoordinatePoint cp : points) {
            pointList.add(new Point(cp.getX(), cp.getY()));
        }
        
        return new BuildingData(litera, description, pointList);
    }
}
