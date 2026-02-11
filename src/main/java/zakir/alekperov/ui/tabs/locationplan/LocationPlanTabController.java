package zakir.alekperov.ui.tabs.locationplan;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import zakir.alekperov.application.locationplan.*;
import zakir.alekperov.domain.shared.ValidationException;
import zakir.alekperov.ui.dialogs.AddBuildingDialogController;
import zakir.alekperov.ui.tabs.base.BaseTabController;
import zakir.alekperov.ui.visualization.BuildingVisualizer;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер вкладки "Ситуационный план" с адаптивным Canvas.
 */
public class LocationPlanTabController extends BaseTabController {
    
    private SaveLocationPlanUseCase saveLocationPlanUseCase;
    private LoadLocationPlanUseCase loadLocationPlanUseCase;
    private AddBuildingCoordinatesUseCase addBuildingCoordinatesUseCase;
    private DeleteBuildingUseCase deleteBuildingUseCase;
    
    @FXML private ComboBox<String> scaleComboBox;
    @FXML private DatePicker creationDatePicker;
    @FXML private TextField authorField;
    @FXML private TextArea notesArea;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button addCoordinatesButton;
    @FXML private ListView<BuildingItem> buildingsListView;
    
    @FXML private Canvas buildingCanvas;
    @FXML private StackPane canvasContainer;
    @FXML private Label canvasPlaceholder;
    
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomResetButton;
    
    @FXML private Label selectedBuildingLabel;
    
    private String currentPassportId;
    private List<LocationPlanDTO.BuildingCoordinatesDTO> currentBuildings = new ArrayList<>();
    private BuildingVisualizer visualizer;
    
    private double lastMouseX;
    private double lastMouseY;
    private boolean isPanning = false;
    
    public LocationPlanTabController() {}
    
    public LocationPlanTabController(SaveLocationPlanUseCase saveLocationPlanUseCase,
                                    LoadLocationPlanUseCase loadLocationPlanUseCase,
                                    AddBuildingCoordinatesUseCase addBuildingCoordinatesUseCase,
                                    DeleteBuildingUseCase deleteBuildingUseCase) {
        setDependencies(saveLocationPlanUseCase, loadLocationPlanUseCase, addBuildingCoordinatesUseCase, deleteBuildingUseCase);
    }
    
    public void setDependencies(SaveLocationPlanUseCase saveLocationPlanUseCase,
                               LoadLocationPlanUseCase loadLocationPlanUseCase,
                               AddBuildingCoordinatesUseCase addBuildingCoordinatesUseCase,
                               DeleteBuildingUseCase deleteBuildingUseCase) {
        if (saveLocationPlanUseCase == null || loadLocationPlanUseCase == null || 
            addBuildingCoordinatesUseCase == null || deleteBuildingUseCase == null) {
            throw new IllegalArgumentException("Зависимости не могут быть null");
        }
        
        this.saveLocationPlanUseCase = saveLocationPlanUseCase;
        this.loadLocationPlanUseCase = loadLocationPlanUseCase;
        this.addBuildingCoordinatesUseCase = addBuildingCoordinatesUseCase;
        this.deleteBuildingUseCase = deleteBuildingUseCase;
    }
    
    @Override
    protected void setupBindings() {
        if (scaleComboBox != null) {
            scaleComboBox.getItems().addAll("100", "200", "500", "1000", "2000", "5000");
            scaleComboBox.setValue("500");
        }
        
        if (buildingsListView != null) {
            buildingsListView.setCellFactory(param -> new BuildingListCell());
        }
        
        if (buildingCanvas != null && canvasContainer != null) {
            visualizer = new BuildingVisualizer(buildingCanvas);
            setupCanvasResize();
            setupCanvasInteraction();
        }
    }
    
    /**
     * Настроить автоматическое изменение размера Canvas БЕЗ циклических зависимостей.
     */
    private void setupCanvasResize() {
        // ИСПРАВЛЕНИЕ: Используем binding вместо listeners
        // Canvas просто привязывается к размеру контейнера, но не влияет на него
        buildingCanvas.widthProperty().bind(canvasContainer.widthProperty());
        buildingCanvas.heightProperty().bind(canvasContainer.heightProperty());
        
        // Перерисовываем только когда контейнер меняется
        canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (Math.abs(newVal.doubleValue() - oldVal.doubleValue()) > 1) {
                updateVisualization();
            }
        });
        
        canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (Math.abs(newVal.doubleValue() - oldVal.doubleValue()) > 1) {
                updateVisualization();
            }
        });
    }
    
    /**
     * Настроить интерактивность Canvas: zoom, pan, selection, point editing.
     */
    private void setupCanvasInteraction() {
        // Zoom колесом мыши
        buildingCanvas.setOnScroll((ScrollEvent event) -> {
            if (visualizer != null) {
                visualizer.getTransform().zoomByScroll(event.getX(), event.getY(), event.getDeltaY());
                updateVisualization();
                event.consume();
            }
        });
        
        // Hover - подсветка при наведении (здания или точки)
        buildingCanvas.setOnMouseMoved(event -> {
            if (visualizer == null || isPanning) return;
            
            // Сначала проверить, наведена ли точка
            BuildingVisualizer.PointHandle point = visualizer.findPointAt(event.getX(), event.getY(), currentBuildings);
            visualizer.setHoveredPoint(point);
            
            if (point != null) {
                buildingCanvas.setCursor(javafx.scene.Cursor.CROSSHAIR);
            } else {
                String hoveredLitera = visualizer.findBuildingAt(event.getX(), event.getY(), currentBuildings);
                visualizer.setHoveredBuilding(hoveredLitera);
                buildingCanvas.setCursor(hoveredLitera != null ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);
            }
            
            updateVisualization();
        });
        
        // Клик - выделение здания
        buildingCanvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !event.isControlDown() && visualizer != null) {
                String clickedLitera = visualizer.findBuildingAt(event.getX(), event.getY(), currentBuildings);
                
                if (clickedLitera != null) {
                    if (clickedLitera.equals(visualizer.getSelectedBuilding())) {
                        visualizer.clearSelection();
                        updateSelectionInfo(null);
                    } else {
                        visualizer.setSelectedBuilding(clickedLitera);
                        updateSelectionInfo(clickedLitera);
                    }
                } else {
                    visualizer.clearSelection();
                    updateSelectionInfo(null);
                }
                
                updateVisualization();
                event.consume();
            }
        });
        
        // Mouse pressed - начало pan или перетаскивания точки
        buildingCanvas.setOnMousePressed(event -> {
            if (visualizer == null) return;
            
            // Pan средней кнопкой или Ctrl+левая
            if (event.getButton() == MouseButton.MIDDLE || 
                (event.getButton() == MouseButton.PRIMARY && event.isControlDown())) {
                isPanning = true;
                lastMouseX = event.getX();
                lastMouseY = event.getY();
                buildingCanvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
                event.consume();
                return;
            }
            
            // Перетаскивание точки левой кнопкой (Alt+ЛКМ)
            if (event.getButton() == MouseButton.PRIMARY && event.isAltDown()) {
                BuildingVisualizer.PointHandle point = visualizer.findPointAt(event.getX(), event.getY(), currentBuildings);
                if (point != null) {
                    visualizer.startDraggingPoint(point);
                    buildingCanvas.setCursor(javafx.scene.Cursor.MOVE);
                    event.consume();
                }
            }
        });
        
        // Mouse dragged - перемещение Canvas или точки
        buildingCanvas.setOnMouseDragged(event -> {
            if (visualizer == null) return;
            
            // Pan
            if (isPanning) {
                double dx = event.getX() - lastMouseX;
                double dy = event.getY() - lastMouseY;
                visualizer.getTransform().pan(dx, dy);
                lastMouseX = event.getX();
                lastMouseY = event.getY();
                updateVisualization();
                event.consume();
                return;
            }
            
            // Перетаскивание точки
            if (visualizer.isDraggingPoint()) {
                visualizer.updateDraggingPoint(event.getX(), event.getY());
                updateVisualization();
                event.consume();
            }
        });
        
        // Mouse released - завершение pan или сохранение точки
        buildingCanvas.setOnMouseReleased(event -> {
            if (visualizer == null) return;
            
            if (isPanning) {
                isPanning = false;
                buildingCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
                event.consume();
                return;
            }
            
            // Сохранить новые координаты точки
            if (visualizer.isDraggingPoint()) {
                BuildingVisualizer.PointHandle point = visualizer.stopDraggingPoint();
                if (point != null) {
                    savePointCoordinates(point);
                }
                buildingCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
                event.consume();
            }
        });
        
        // Mouse exited - очистить hover
        buildingCanvas.setOnMouseExited(event -> {
            if (visualizer != null) {
                visualizer.setHoveredBuilding(null);
                visualizer.setHoveredPoint(null);
                buildingCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
                updateVisualization();
            }
        });
    }
    
    /**
     * Сохранить новые координаты точки в БД.
     */
    private void savePointCoordinates(BuildingVisualizer.PointHandle point) {
        try {
            LocationPlanDTO.BuildingCoordinatesDTO building = currentBuildings.stream()
                .filter(b -> b.litera().equals(point.buildingLitera))
                .findFirst()
                .orElse(null);
            
            if (building == null) {
                showError("Ошибка", "Здание не найдено");
                return;
            }
            
            List<AddBuildingCoordinatesCommand.CoordinatePointData> pointDatas = new ArrayList<>();
            for (int i = 0; i < building.points().size(); i++) {
                if (i == point.pointIndex) {
                    pointDatas.add(new AddBuildingCoordinatesCommand.CoordinatePointData(
                        String.format("%.2f", point.worldX),
                        String.format("%.2f", point.worldY)
                    ));
                } else {
                    LocationPlanDTO.CoordinatePointDTO p = building.points().get(i);
                    pointDatas.add(new AddBuildingCoordinatesCommand.CoordinatePointData(p.x(), p.y()));
                }
            }
            
            DeleteBuildingCommand deleteCommand = new DeleteBuildingCommand(currentPassportId, building.litera());
            deleteBuildingUseCase.execute(deleteCommand);
            
            AddBuildingCoordinatesCommand addCommand = new AddBuildingCoordinatesCommand(
                currentPassportId,
                building.litera(),
                building.description(),
                pointDatas
            );
            addBuildingCoordinatesUseCase.execute(addCommand);
            
            loadLocationPlanData();
            System.out.println("✓ Координаты точки обновлены");
        } catch (Exception e) {
            showError("Ошибка сохранения", e.getMessage());
            e.printStackTrace();
            loadLocationPlanData();
        }
    }
    
    private void updateSelectionInfo(String litera) {
        if (selectedBuildingLabel == null) return;
        
        if (litera == null) {
            selectedBuildingLabel.setText("Кликните по зданию для выбора. Alt+ЛКМ на точке - редактирование.");
            selectedBuildingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        } else {
            LocationPlanDTO.BuildingCoordinatesDTO building = currentBuildings.stream()
                .filter(b -> b.litera().equals(litera))
                .findFirst()
                .orElse(null);
            
            if (building != null) {
                String info = String.format("✅ Выбрано: Литера %s - %s (%d точек). Alt+ЛКМ на точке - редактирование.", 
                    building.litera(), building.description(), building.points().size());
                selectedBuildingLabel.setText(info);
                selectedBuildingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            }
        }
    }
    
    @FXML
    private void handleZoomIn() {
        if (visualizer != null && buildingCanvas != null) {
            double centerX = buildingCanvas.getWidth() / 2;
            double centerY = buildingCanvas.getHeight() / 2;
            visualizer.getTransform().zoomIn(centerX, centerY);
            updateVisualization();
        }
    }
    
    @FXML
    private void handleZoomOut() {
        if (visualizer != null && buildingCanvas != null) {
            double centerX = buildingCanvas.getWidth() / 2;
            double centerY = buildingCanvas.getHeight() / 2;
            visualizer.getTransform().zoomOut(centerX, centerY);
            updateVisualization();
        }
    }
    
    @FXML
    private void handleZoomReset() {
        if (visualizer != null) {
            visualizer.getTransform().reset();
            updateVisualization();
        }
    }
    
    @Override
    protected void loadInitialData() {
        if (creationDatePicker != null) {
            creationDatePicker.setValue(LocalDate.now());
        }
        
        if (currentPassportId != null && saveLocationPlanUseCase != null) {
            loadLocationPlanData();
        }
    }
    
    public void setPassportId(String passportId) {
        if (passportId == null || passportId.isBlank()) {
            throw new IllegalArgumentException("ID паспорта не может быть пустым");
        }
        this.currentPassportId = passportId;
        
        if (loadLocationPlanUseCase != null) {
            loadLocationPlanData();
        }
    }
    
    private void loadLocationPlanData() {
        try {
            LoadLocationPlanQuery query = new LoadLocationPlanQuery(currentPassportId);
            Optional<LocationPlanDTO> planOptional = loadLocationPlanUseCase.execute(query);
            
            if (planOptional.isPresent()) {
                LocationPlanDTO plan = planOptional.get();
                
                if (scaleComboBox != null) {
                    scaleComboBox.setValue(String.valueOf(plan.scaleDenominator()));
                }
                if (authorField != null) {
                    authorField.setText(plan.executorName());
                }
                if (creationDatePicker != null) {
                    creationDatePicker.setValue(plan.planDate());
                }
                if (notesArea != null) {
                    notesArea.setText(plan.notes());
                }
                
                currentBuildings = plan.buildings();
                
                if (buildingsListView != null) {
                    buildingsListView.getItems().clear();
                    for (var building : currentBuildings) {
                        buildingsListView.getItems().add(new BuildingItem(building));
                    }
                }
                
                updateVisualization();
                System.out.println("✓ Данные ситуационного плана загружены");
            } else {
                System.out.println("ℹ️ Ситуационный план не найден");
            }
        } catch (Exception e) {
            showError("Ошибка загрузки", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateVisualization() {
        if (visualizer == null) return;
        
        if (canvasPlaceholder != null) {
            canvasPlaceholder.setVisible(currentBuildings == null || currentBuildings.isEmpty());
        }
        
        visualizer.draw(currentBuildings);
    }
    
    @Override
    public boolean validateData() {
        if (currentPassportId == null || currentPassportId.isBlank()) {
            showWarning("ID паспорта не установлен");
            return false;
        }
        
        if (scaleComboBox == null || scaleComboBox.getValue() == null || scaleComboBox.getValue().isBlank()) {
            showWarning("Укажите масштаб плана");
            if (scaleComboBox != null) scaleComboBox.requestFocus();
            return false;
        }
        
        if (creationDatePicker == null || creationDatePicker.getValue() == null) {
            showWarning("Укажите дату создания плана");
            if (creationDatePicker != null) creationDatePicker.requestFocus();
            return false;
        }
        
        return true;
    }
    
    @Override
    public void saveData() {
        if (saveLocationPlanUseCase == null || !validateData()) return;
        
        try {
            SaveLocationPlanCommand command = new SaveLocationPlanCommand(
                currentPassportId,
                scaleComboBox.getValue(),
                authorField != null ? authorField.getText() : "",
                creationDatePicker.getValue(),
                notesArea != null ? notesArea.getText() : "",
                null
            );
            
            saveLocationPlanUseCase.execute(command);
            showInfo("Ситуационный план сохранен");
        } catch (Exception e) {
            showError("Ошибка", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void clearData() {
        if (scaleComboBox != null) scaleComboBox.setValue("500");
        if (creationDatePicker != null) creationDatePicker.setValue(LocalDate.now());
        if (authorField != null) authorField.clear();
        if (notesArea != null) notesArea.clear();
        if (buildingsListView != null) buildingsListView.getItems().clear();
        currentBuildings.clear();
        if (visualizer != null) visualizer.clearSelection();
        updateSelectionInfo(null);
        updateVisualization();
    }
    
    @FXML
    private void handleAddCoordinates() {
        if (currentPassportId == null || currentPassportId.isBlank()) {
            showWarning("Сначала необходимо создать и сохранить паспорт");
            return;
        }
        openBuildingDialog(null);
    }
    
    private void openBuildingDialog(LocationPlanDTO.BuildingCoordinatesDTO existingBuilding) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/dialogs/AddBuildingDialog.fxml"));
            Scene dialogScene = new Scene(loader.load());
            AddBuildingDialogController controller = loader.getController();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(existingBuilding == null ? "Добавление здания" : "Редактирование здания");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(dialogScene);
            dialogStage.setResizable(false);
            
            controller.setDialogStage(dialogStage);
            
            if (existingBuilding != null) {
                List<AddBuildingDialogController.Point> points = new ArrayList<>();
                for (var point : existingBuilding.points()) {
                    points.add(new AddBuildingDialogController.Point(
                        Double.parseDouble(point.x()),
                        Double.parseDouble(point.y())
                    ));
                }
                controller.setExistingBuilding(existingBuilding.litera(), existingBuilding.description(), points);
            }
            
            dialogStage.showAndWait();
            
            if (controller.isSavedSuccessfully()) {
                AddBuildingDialogController.BuildingData buildingData = controller.getBuildingData();
                if (buildingData != null) {
                    if (controller.isEditMode()) {
                        updateBuildingInDatabase(buildingData);
                    } else {
                        saveBuildingToDatabase(buildingData);
                    }
                }
            }
        } catch (IOException e) {
            showError("Ошибка загрузки диалога", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveBuildingToDatabase(AddBuildingDialogController.BuildingData buildingData) {
        try {
            List<AddBuildingCoordinatesCommand.CoordinatePointData> pointDatas = new ArrayList<>();
            for (AddBuildingDialogController.Point point : buildingData.getPoints()) {
                pointDatas.add(new AddBuildingCoordinatesCommand.CoordinatePointData(
                    String.valueOf(point.getX()), String.valueOf(point.getY())
                ));
            }
            
            AddBuildingCoordinatesCommand command = new AddBuildingCoordinatesCommand(
                currentPassportId, buildingData.getLitera(), buildingData.getDescription(), pointDatas
            );
            
            addBuildingCoordinatesUseCase.execute(command);
            loadLocationPlanData();
            showInfo("Здание успешно добавлено!");
        } catch (Exception e) {
            showError("Ошибка", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateBuildingInDatabase(AddBuildingDialogController.BuildingData buildingData) {
        try {
            DeleteBuildingCommand deleteCommand = new DeleteBuildingCommand(currentPassportId, buildingData.getLitera());
            deleteBuildingUseCase.execute(deleteCommand);
            
            List<AddBuildingCoordinatesCommand.CoordinatePointData> pointDatas = new ArrayList<>();
            for (AddBuildingDialogController.Point point : buildingData.getPoints()) {
                pointDatas.add(new AddBuildingCoordinatesCommand.CoordinatePointData(
                    String.valueOf(point.getX()), String.valueOf(point.getY())
                ));
            }
            
            AddBuildingCoordinatesCommand addCommand = new AddBuildingCoordinatesCommand(
                currentPassportId, buildingData.getLitera(), buildingData.getDescription(), pointDatas
            );
            
            addBuildingCoordinatesUseCase.execute(addCommand);
            loadLocationPlanData();
            showInfo("Здание успешно обновлено!");
        } catch (Exception e) {
            showError("Ошибка", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleDeleteBuilding(BuildingItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удаление здания");
        alert.setContentText("Вы уверены, что хотите удалить здание \"" + item.getBuilding().litera() + "\"?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    DeleteBuildingCommand command = new DeleteBuildingCommand(currentPassportId, item.getBuilding().litera());
                    deleteBuildingUseCase.execute(command);
                    loadLocationPlanData();
                    showInfo("Здание успешно удалено!");
                } catch (Exception e) {
                    showError("Ошибка", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void handleEditBuilding(BuildingItem item) {
        openBuildingDialog(item.getBuilding());
    }
    
    private void handleViewBuilding(BuildingItem item) {
        StringBuilder info = new StringBuilder();
        info.append("Литера: ").append(item.getBuilding().litera()).append("\n");
        info.append("Описание: ").append(item.getBuilding().description()).append("\n\n");
        info.append("Координаты:\n");
        
        int i = 1;
        for (var point : item.getBuilding().points()) {
            info.append(String.format("• Точка %d: X=%s, Y=%s\n", i++, point.x(), point.y()));
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация о здании");
        alert.setHeaderText("Здание литера " + item.getBuilding().litera());
        alert.setContentText(info.toString());
        alert.getDialogPane().setPrefWidth(400);
        alert.showAndWait();
    }
    
    @FXML
    private void handleSave() { saveData(); }
    
    @FXML
    private void handleClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Очистка данных");
        alert.setContentText("Вы уверены, что хотите очистить все данные?");
        alert.showAndWait().ifPresent(response -> { if (response == ButtonType.OK) clearData(); });
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
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
    
    private static class BuildingItem {
        private final LocationPlanDTO.BuildingCoordinatesDTO building;
        public BuildingItem(LocationPlanDTO.BuildingCoordinatesDTO building) { this.building = building; }
        public LocationPlanDTO.BuildingCoordinatesDTO getBuilding() { return building; }
    }
    
    /**
     * ИСПРАВЛЕНИЕ 2: Кнопки всегда видны, текст с ellipsis.
     */
    private class BuildingListCell extends ListCell<BuildingItem> {
        private final HBox content;
        private final Label textLabel;
        private final Button viewButton, editButton, deleteButton;
        
        public BuildingListCell() {
            content = new HBox(8);
            content.setAlignment(Pos.CENTER_LEFT);
            
            // Текст с ограничением ширины и ellipsis
            textLabel = new Label();
            textLabel.setStyle("-fx-text-overrun: ellipsis;");
            textLabel.setMaxWidth(150); // Фиксированная максимальная ширина
            HBox.setHgrow(textLabel, Priority.ALWAYS);
            
            // Прокладка
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Кнопки с фиксированными размерами
            viewButton = new Button("👁️");
            viewButton.setTooltip(new Tooltip("Просмотреть"));
            viewButton.setStyle("-fx-font-size: 14px; -fx-padding: 4 8;");
            viewButton.setMinWidth(35);
            viewButton.setMaxWidth(35);
            
            editButton = new Button("✏️");
            editButton.setTooltip(new Tooltip("Редактировать"));
            editButton.setStyle("-fx-font-size: 14px; -fx-padding: 4 8;");
            editButton.setMinWidth(35);
            editButton.setMaxWidth(35);
            
            deleteButton = new Button("🗑️");
            deleteButton.setTooltip(new Tooltip("Удалить"));
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 4 8;");
            deleteButton.setMinWidth(35);
            deleteButton.setMaxWidth(35);
            
            content.getChildren().addAll(textLabel, spacer, viewButton, editButton, deleteButton);
        }
        
        @Override
        protected void updateItem(BuildingItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                var building = item.getBuilding();
                textLabel.setText(String.format("🏗️ %s: %s (%d)", 
                    building.litera(), building.description(), building.points().size()));
                viewButton.setOnAction(e -> handleViewBuilding(item));
                editButton.setOnAction(e -> handleEditBuilding(item));
                deleteButton.setOnAction(e -> handleDeleteBuilding(item));
                setGraphic(content);
            }
        }
    }
}
