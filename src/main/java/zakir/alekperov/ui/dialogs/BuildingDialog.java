package zakir.alekperov.ui.dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import zakir.alekperov.model.Building;

import java.time.Year;

/**
 * Диалог для добавления или редактирования строения
 */
public class BuildingDialog extends Dialog<Building> {

    private final TextField letterField;
    private final ComboBox<String> nameField;
    private final TextField yearField;
    private final ComboBox<String> materialField;
    private final TextField areaField;
    private final TextField heightField;
    private final TextField volumeField;
    private final TextField valueField;

    private final Building editingBuilding;

    public BuildingDialog(Building building) {
        this.editingBuilding = building;

        setTitle(building == null ? "Добавить строение" : "Редактировать строение");
        setHeaderText(building == null ? "Заполните информацию о строении" : "Редактирование: " + building.getName());

        // Создаем поля ввода
        letterField = new TextField();
        letterField.setPromptText("А");
        letterField.setPrefWidth(80);

        nameField = new ComboBox<>(FXCollections.observableArrayList(
                "Жилой дом",
                "Жилая пристройка",
                "Баня",
                "Гараж",
                "Сарай",
                "Беседка",
                "Навес",
                "Хозблок",
                "Погреб",
                "Теплица",
                "Забор",
                "Ворота",
                "Колодец",
                "Септик",
                "Другое"));
        nameField.setEditable(true);
        nameField.setPromptText("Выберите или введите");
        nameField.setPrefWidth(250);

        yearField = new TextField();
        yearField.setPromptText(String.valueOf(Year.now().getValue()));
        yearField.setPrefWidth(100);

        materialField = new ComboBox<>(FXCollections.observableArrayList(
                "Кирпич",
                "Блоки (пеноблок, газоблок)",
                "Бревно",
                "Брус",
                "Каркасный",
                "Монолит",
                "Панель",
                "Камень",
                "Металл",
                "Дерево",
                "Смешанный",
                "Другое"));
        materialField.setEditable(true);
        materialField.setPromptText("Выберите или введите");
        materialField.setPrefWidth(250);

        areaField = new TextField();
        areaField.setPromptText("120.0");
        areaField.setPrefWidth(150);

        heightField = new TextField();
        heightField.setPromptText("6.0");
        heightField.setPrefWidth(150);

        volumeField = new TextField();
        volumeField.setPromptText("Оставьте пустым для автоматического расчета");
        volumeField.setPrefWidth(150);

        valueField = new TextField();
        valueField.setPromptText("5000000.00");
        valueField.setPrefWidth(200);

        // Валидация только цифр
        setupNumericValidation();

        // Заполняем данные при редактировании
        if (building != null) {
            fillFields(building);
        }

        // Создаем layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        int row = 0;

        // Литера
        grid.add(new Label("Литера (№ на плане):*"), 0, row);
        HBox letterBox = new HBox(5);
        letterBox.getChildren().addAll(
                letterField,
                new Label("(например: А, Б, В или 1, 2, 3)"));
        grid.add(letterBox, 1, row++);

        // Наименование
        grid.add(new Label("Наименование:*"), 0, row);
        grid.add(nameField, 1, row++);

        // Год ввода
        grid.add(new Label("Год ввода в эксплуатацию:*"), 0, row);
        grid.add(yearField, 1, row++);

        // Материал стен
        grid.add(new Label("Материал стен:*"), 0, row);
        grid.add(materialField, 1, row++);

        // Площадь застройки
        grid.add(new Label("Площадь застройки (кв.м):*"), 0, row);
        HBox areaBox = new HBox(5);
        areaBox.getChildren().addAll(areaField, new Label("кв.м"));
        grid.add(areaBox, 1, row++);

        // Высота
        grid.add(new Label("Высота (м):*"), 0, row);
        HBox heightBox = new HBox(5);
        heightBox.getChildren().addAll(heightField, new Label("м"));
        grid.add(heightBox, 1, row++);

        // Объем
        grid.add(new Label("Строительный объем (куб.м):"), 0, row);
        HBox volumeInputBox = new HBox(5);
        volumeInputBox.getChildren().addAll(volumeField, new Label("куб.м (пусто = площадь × высота)"));
        grid.add(volumeInputBox, 1, row++);

        // Инвентаризационная стоимость
        grid.add(new Label("Инвентаризационная стоимость (руб.):"), 0, row);
        HBox valueBox = new HBox(5);
        valueBox.getChildren().addAll(valueField, new Label("руб."));
        grid.add(valueBox, 1, row++);

        // Примечание
        Label noteLabel = new Label("* - обязательные поля");
        noteLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");
        grid.add(noteLabel, 0, row, 2, 1);

        getDialogPane().setContent(grid);

        // Кнопки
        ButtonType saveButton = new ButtonType(
                building == null ? "Добавить" : "Сохранить",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Обработка результата
        setResultConverter(buttonType -> {
            if (buttonType == saveButton) {
                return null; // Не возвращаем результат, обработаем через фильтр
            }
            return null;
        });

        // Блокируем закрытие диалога при невалидных данных
        final Button saveBtn = (Button) getDialogPane().lookupButton(saveButton);
        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateInput()) {
                event.consume(); // Блокируем закрытие
            }
        });

        // Устанавливаем результат только при успешной валидации
        saveBtn.setOnAction(e -> {
            if (validateInput()) {
                setResult(createBuilding());
            }
        });
    }

    private void setupNumericValidation() {
        // Только цифры для года
        yearField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                yearField.setText(oldVal);
            }
        });

        // Числа с точкой для площади
        areaField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                areaField.setText(oldVal);
            }
        });

        // Числа с точкой для высоты
        heightField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                heightField.setText(oldVal);
            }
        });

        // Числа с точкой для объема
        volumeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                volumeField.setText(oldVal);
            }
        });

        // Числа с точкой для стоимости
        valueField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                valueField.setText(oldVal);
            }
        });
    }

    private void fillFields(Building building) {
        letterField.setText(building.getLetter());
        nameField.setValue(building.getName());
        yearField.setText(String.valueOf(building.getYearBuilt()));
        materialField.setValue(building.getWallMaterial());
        areaField.setText(String.format("%.1f", building.getBuildingArea()));
        heightField.setText(String.format("%.1f", building.getHeight()));
        volumeField.setText(String.format("%.1f", building.getVolume()));
        valueField.setText(String.format("%.2f", building.getInventoryValue()));
    }

    private boolean validateInput() {
        // Литера
        if (letterField.getText().isBlank()) {
            showError("Укажите литеру (номер на плане)");
            return false;
        }

        // Наименование
        if (nameField.getValue() == null || nameField.getValue().isBlank()) {
            showError("Укажите наименование строения");
            return false;
        }

        // Год
        if (yearField.getText().isBlank()) {
            showError("Укажите год ввода в эксплуатацию");
            return false;
        }

        try {
            int year = Integer.parseInt(yearField.getText());
            int currentYear = Year.now().getValue();
            if (year < 1800 || year > currentYear + 1) {
                showError("Год должен быть между 1800 и " + (currentYear + 1));
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Год должен быть числом");
            return false;
        }

        // Материал
        if (materialField.getValue() == null || materialField.getValue().isBlank()) {
            showError("Укажите материал стен");
            return false;
        }

        // Площадь
        if (areaField.getText().isBlank()) {
            showError("Укажите площадь застройки");
            return false;
        }

        try {
            double area = Double.parseDouble(areaField.getText());
            if (area <= 0 || area > 10000) {
                showError("Площадь должна быть больше 0 и меньше 10000 кв.м");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Площадь должна быть числом");
            return false;
        }

        // Высота
        if (heightField.getText().isBlank()) {
            showError("Укажите высоту");
            return false;
        }

        try {
            double height = Double.parseDouble(heightField.getText());
            if (height <= 0 || height > 100) {
                showError("Высота должна быть больше 0 и меньше 100 м");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Высота должна быть числом");
            return false;
        }

        // Объем - теперь НЕ обязательное поле
        if (!volumeField.getText().isBlank()) {
            try {
                double volume = Double.parseDouble(volumeField.getText());
                if (volume <= 0) {
                    showError("Объем должен быть больше 0");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Объем должен быть числом");
                return false;
            }
        }

        // Стоимость (необязательное поле)
        if (!valueField.getText().isBlank()) {
            try {
                double value = Double.parseDouble(valueField.getText());
                if (value < 0) {
                    showError("Стоимость не может быть отрицательной");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Стоимость должна быть числом");
                return false;
            }
        }

        return true;
    }

    private Building createBuilding() {
        String letter = letterField.getText().trim();
        String name = nameField.getValue();
        int year = Integer.parseInt(yearField.getText());
        String material = materialField.getValue();
        double area = Double.parseDouble(areaField.getText());
        double height = Double.parseDouble(heightField.getText());
        
        // Если объем не указан - рассчитываем автоматически
        double volume;
        if (volumeField.getText().isBlank()) {
            volume = area * height;
            volume = Math.round(volume * 10.0) / 10.0; // Округляем до 1 знака
        } else {
            volume = Double.parseDouble(volumeField.getText());
        }
        
        double value = valueField.getText().isBlank() ? 0.0 : Double.parseDouble(valueField.getText());

        return new Building(letter, name, year, material, area, height, volume, value);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
