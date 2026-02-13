# Инструкции по интеграции нового функционала

Дата: 13.02.2026

## Что уже загружено

✅ **Domain слой**:
- `PlanMode.java` - enum для выбора режима
- `PlanImage.java` - Value Object для изображений
- `PlanScale.java` - Value Object для масштаба
- `CoordinatePoint.java` - Value Object для координат
- `BuildingLitera.java` - Value Object для литеры
- `BuildingCoordinates.java` - Entity с координатами
- `LocationPlan.java` - Aggregate Root

✅ **Application слой**:
- `UploadPlanImageCommand.java`
- `UploadPlanImageUseCase.java`
- `UploadPlanImageService.java`

## Что нужно сделать вручную

### 1. Обновить DependencyContainer

**Файл**: `src/main/java/zakir/alekperov/bootstrap/DependencyContainer.java`

Добавьте в класс:

```java
// Добавить поле
private final UploadPlanImageUseCase uploadPlanImageUseCase;

// В конструкторе (после инициализации других use cases)
this.uploadPlanImageUseCase = new UploadPlanImageService(locationPlanRepository);

// Добавить геттер
public UploadPlanImageUseCase getUploadPlanImageUseCase() {
    return uploadPlanImageUseCase;
}
```

### 2. Обновить схему БД

**Файл**: `src/main/java/zakir/alekperov/infrastructure/database/migration/DatabaseMigration.java`

Добавьте новые колонки в таблицу `location_plan`:

```sql
ALTER TABLE location_plan ADD COLUMN plan_mode TEXT NOT NULL DEFAULT 'MANUAL_DRAWING';
ALTER TABLE location_plan ADD COLUMN uploaded_image_data BLOB;
ALTER TABLE location_plan ADD COLUMN uploaded_image_filename TEXT;
```

**Важно**: Обновите номер версии миграции!

### 3. Обновить LocationPlanRepositoryImpl

**Файл**: `src/main/java/zakir/alekperov/infrastructure/persistence/locationplan/LocationPlanRepositoryImpl.java`

Если этот файл еще не создан, создайте его согласно архитектурному документу.

Основные изменения:

```java
// При сохранении в savePlanData()
statement.setString(4, locationPlan.getMode().name());
if (locationPlan.getUploadedImage().isPresent()) {
    PlanImage image = locationPlan.getUploadedImage().get();
    statement.setBytes(5, image.getImageData());
    statement.setString(6, image.getFileName());
} else {
    statement.setNull(5, java.sql.Types.BLOB);
    statement.setNull(6, java.sql.Types.VARCHAR);
}

// При загрузке в loadPlanData()
String modeStr = resultSet.getString("plan_mode");
PlanMode mode = PlanMode.valueOf(modeStr);

byte[] imageData = resultSet.getBytes("uploaded_image_data");
String imageFileName = resultSet.getString("uploaded_image_filename");

PlanImage uploadedImage = null;
if (imageData != null && imageFileName != null) {
    uploadedImage = new PlanImage(imageData, imageFileName);
}
```

### 4. Обновить LocationPlanTabController

**Файл**: `src/main/java/zakir/alekperov/ui/tabs/locationplan/LocationPlanTabController.java`

#### 4.1. Добавить FXML элементы

```java
@FXML private ToggleGroup planModeToggleGroup;
@FXML private RadioButton manualDrawingRadio;
@FXML private RadioButton uploadedImageRadio;
@FXML private Button uploadImageButton;
@FXML private ImageView uploadedImageView;
@FXML private Label uploadedImageInfoLabel;
@FXML private VBox uploadedImageContainer;

private UploadPlanImageUseCase uploadPlanImageUseCase;
```

#### 4.2. Добавить в setDependencies()

```java
public void setDependencies(
        SaveLocationPlanUseCase saveLocationPlanUseCase,
        LoadLocationPlanUseCase loadLocationPlanUseCase,
        AddBuildingCoordinatesUseCase addBuildingCoordinatesUseCase,
        DeleteBuildingUseCase deleteBuildingUseCase,
        UploadPlanImageUseCase uploadPlanImageUseCase) {  // НОВЫЙ параметр
    
    // ... существующая валидация ...
    
    this.uploadPlanImageUseCase = uploadPlanImageUseCase;
}
```

#### 4.3. Добавить в setupBindings()

```java
if (planModeToggleGroup != null) {
    manualDrawingRadio.setToggleGroup(planModeToggleGroup);
    uploadedImageRadio.setToggleGroup(planModeToggleGroup);
    manualDrawingRadio.setSelected(true);
    
    planModeToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
        handlePlanModeChange();
    });
}
```

#### 4.4. Добавить обработчики

```java
@FXML
private void handlePlanModeChange() {
    boolean isManualDrawing = manualDrawingRadio.isSelected();
    
    // Показать/скрыть элементы для ручного рисования
    canvasContainer.setVisible(isManualDrawing);
    canvasContainer.setManaged(isManualDrawing);
    buildingsListView.setVisible(isManualDrawing);
    buildingsListView.setManaged(isManualDrawing);
    addCoordinatesButton.setVisible(isManualDrawing);
    scaleComboBox.setDisable(!isManualDrawing);
    authorField.setDisable(!isManualDrawing);
    
    // Показать/скрыть элементы для загруженного изображения
    uploadedImageContainer.setVisible(!isManualDrawing);
    uploadedImageContainer.setManaged(!isManualDrawing);
    
    System.out.println("🔄 Режим изменен: " + (isManualDrawing ? "Ручное рисование" : "Загруженное изображение"));
}

@FXML
private void handleUploadImage() {
    if (currentPassportId == null || currentPassportId.isBlank()) {
        showWarning("Сначала необходимо создать и сохранить паспорт");
        return;
    }
    
    javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
    fileChooser.setTitle("Выберите изображение ситуационного плана");
    fileChooser.getExtensionFilters().addAll(
        new javafx.stage.FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg"),
        new javafx.stage.FileChooser.ExtensionFilter("PNG", "*.png"),
        new javafx.stage.FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg")
    );
    
    java.io.File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
    
    if (selectedFile != null) {
        try {
            byte[] imageData = java.nio.file.Files.readAllBytes(selectedFile.toPath());
            
            UploadPlanImageCommand command = new UploadPlanImageCommand(
                currentPassportId,
                imageData,
                selectedFile.getName(),
                java.time.LocalDate.now(),
                notesArea != null ? notesArea.getText() : ""
            );
            
            uploadPlanImageUseCase.execute(command);
            
            // Отобразить загруженное изображение
            javafx.scene.image.Image image = new javafx.scene.image.Image(
                new java.io.ByteArrayInputStream(imageData)
            );
            uploadedImageView.setImage(image);
            
            double sizeMB = imageData.length / (1024.0 * 1024.0);
            uploadedImageInfoLabel.setText(
                String.format("✅ Загружено: %s (%.2f МБ)", selectedFile.getName(), sizeMB)
            );
            
            showInfo("Изображение успешно загружено!");
            
        } catch (ValidationException e) {
            showError("Ошибка валидации", e.getMessage());
        } catch (Exception e) {
            showError("Ошибка загрузки", "Не удалось загрузить изображение: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

### 5. Обновить FXML

**Файл**: `src/main/resources/ui/tabs/locationplan/LocationPlanTab.fxml`

Добавьте в начало вкладки (после основного VBox):

```xml
<!-- Выбор режима работы -->
<HBox spacing="20" style="-fx-padding: 10; -fx-background-color: #f5f5f5;">
    <Label text="Режим работы:" style="-fx-font-weight: bold;"/>
    <RadioButton fx:id="manualDrawingRadio" text="🖊️ Рисовать план вручную" selected="true">
        <toggleGroup>
            <ToggleGroup fx:id="planModeToggleGroup"/>
        </toggleGroup>
    </RadioButton>
    <RadioButton fx:id="uploadedImageRadio" text="📁 Загрузить готовое изображение" toggleGroup="$planModeToggleGroup"/>
</HBox>

<!-- Секция для загруженного изображения (скрыта по умолчанию) -->
<VBox fx:id="uploadedImageContainer" visible="false" managed="false" spacing="10" style="-fx-padding: 10;">
    <Button fx:id="uploadImageButton" text="📤 Загрузить изображение плана" onAction="#handleUploadImage" 
            style="-fx-font-size: 14px; -fx-padding: 10 20;"/>
    <Label fx:id="uploadedImageInfoLabel" text="Изображение не загружено" 
           style="-fx-text-fill: #666;"/>
    <ScrollPane fitToWidth="true" fitToHeight="true" VBox.vgrow="ALWAYS">
        <ImageView fx:id="uploadedImageView" preserveRatio="true"/>
    </ScrollPane>
</VBox>
```

### 6. Обновить MainWindowController

**Файл**: `src/main/java/zakir/alekperov/ui/main/MainWindowController.java` (или аналогичный)

Где создается `LocationPlanTabController`, добавьте новый use case:

```java
locationPlanController.setDependencies(
    dependencyContainer.getSaveLocationPlanUseCase(),
    dependencyContainer.getLoadLocationPlanUseCase(),
    dependencyContainer.getAddBuildingCoordinatesUseCase(),
    dependencyContainer.getDeleteBuildingUseCase(),
    dependencyContainer.getUploadPlanImageUseCase()  // НОВОЕ!
);
```

## Проверка работы

1. Запустите приложение:
   ```bash
   mvn javafx:run
   ```

2. Перейдите на вкладку "Ситуационный план"

3. Проверьте:
   - ✅ Переключение между режимами (RadioButton)
   - ✅ Ручное рисование (как раньше)
   - ✅ Загрузка изображения (PNG, JPG)
   - ✅ Валидация (размер < 10МБ, поддерживаемые форматы)
   - ✅ Отображение загруженного изображения

## Архитектурные преимущества

✅ **Строгое разделение слоев**: Domain → Application → Infrastructure → UI
✅ **Вся валидация в domain**: PlanImage, LocationPlan
✅ **Инварианты**: Нельзя смешивать режимы
✅ **Неизменяемые Value Objects**: Безопасность данных
✅ **Явные зависимости**: Ручной DI

## Вопросы?

Если возникли проблемы с интеграцией:
1. Проверьте логи приложения
2. Убедитесь, что все imports корректны
3. Проверьте, что миграция БД выполнена
4. Убедитесь, что FXML элементы имеют корректные fx:id
