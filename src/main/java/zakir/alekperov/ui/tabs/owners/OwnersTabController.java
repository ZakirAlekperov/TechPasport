package zakir.alekperov.ui.tabs.owners;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import zakir.alekperov.model.Owner;
import zakir.alekperov.ui.dialogs.OwnerDialog;
import zakir.alekperov.ui.tabs.base.BaseTabController;

import java.util.Optional;

public class OwnersTabController extends BaseTabController {
    
    @FXML private TableView<Owner> ownersTable;
    @FXML private TableColumn<Owner, String> fullNameColumn;
    @FXML private TableColumn<Owner, String> ownerTypeColumn;
    @FXML private TableColumn<Owner, String> documentTypeColumn;
    @FXML private TableColumn<Owner, String> documentNumberColumn;
    @FXML private TableColumn<Owner, String> documentDateColumn;
    @FXML private TableColumn<Owner, String> registrationNumberColumn;
    @FXML private TableColumn<Owner, String> registrationDateColumn;
    @FXML private TableColumn<Owner, Double> shareColumn;
    @FXML private TableColumn<Owner, Void> actionsColumn;
    
    @FXML private Label totalOwnersLabel;
    @FXML private Label totalShareLabel;
    
    private ObservableList<Owner> owners;
    
    @Override
    protected void setupBindings() {
        owners = FXCollections.observableArrayList();
        ownersTable.setItems(owners);
        
        setupTableColumns();
        setupTotalsListeners();
    }
    
    private void setupTableColumns() {
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        ownerTypeColumn.setCellValueFactory(new PropertyValueFactory<>("ownerType"));
        documentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("documentType"));
        documentNumberColumn.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        documentDateColumn.setCellValueFactory(new PropertyValueFactory<>("documentDate"));
        registrationNumberColumn.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        registrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        
        // Форматированная доля
        shareColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Owner owner = getTableView().getItems().get(getIndex());
                    setText(owner.getFormattedShare());
                }
            }
        });
        shareColumn.setCellValueFactory(new PropertyValueFactory<>("shareSize"));
        
        // Колонка действий
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("✏️");
            private final Button deleteButton = new Button("🗑️");
            private final HBox buttons = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setOnAction(event -> {
                    Owner owner = getTableView().getItems().get(getIndex());
                    handleEditOwner(owner);
                });
                
                deleteButton.setOnAction(event -> {
                    Owner owner = getTableView().getItems().get(getIndex());
                    handleDeleteOwner(owner);
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
        owners.addListener((javafx.collections.ListChangeListener<Owner>) change -> {
            updateTotals();
        });
    }
    
    private void updateTotals() {
        int count = owners.size();
        double totalShare = owners.stream()
            .mapToDouble(Owner::getShareSize)
            .sum();
        
        totalOwnersLabel.setText(String.valueOf(count));
        totalShareLabel.setText(String.format("%.4f", totalShare));
        
        // Предупреждение если сумма долей != 1
        if (Math.abs(totalShare - 1.0) > 0.0001 && count > 0) {
            totalShareLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
        } else {
            totalShareLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
        }
    }
    
 @FXML
private void handleAddOwner() {
    OwnerDialog dialog = new OwnerDialog(null, ownersTable.getScene().getWindow());
    Optional<Owner> result = dialog.showAndWait();
    
    result.ifPresent(owner -> {
        owners.add(owner);
        ownersTable.refresh();
    });
}

private void handleEditOwner(Owner owner) {
    OwnerDialog dialog = new OwnerDialog(owner, ownersTable.getScene().getWindow());
    Optional<Owner> result = dialog.showAndWait();
    
    result.ifPresent(updatedOwner -> {
        int index = owners.indexOf(owner);
        owners.set(index, updatedOwner);
        ownersTable.refresh();
    });
}

    
    private void handleDeleteOwner(Owner owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить правообладателя?");
        alert.setContentText("Вы уверены, что хотите удалить:\n" + owner.getFullName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            owners.remove(owner);
            ownersTable.refresh();
        }
    }
    
    @FXML
    private void handleClear() {
        if (owners.isEmpty()) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Очистить все данные?");
        alert.setContentText("Вы уверены, что хотите удалить всех правообладателей?");
        
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
        if (owners.isEmpty()) {
            showWarning("Добавьте хотя бы одного правообладателя");
            return false;
        }
        
        // Проверка суммы долей
        double totalShare = owners.stream()
            .mapToDouble(Owner::getShareSize)
            .sum();
        
        if (Math.abs(totalShare - 1.0) > 0.0001) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Внимание");
            alert.setHeaderText("Сумма долей не равна 1");
            alert.setContentText(String.format("Текущая сумма долей: %.4f\nДолжна быть: 1.0\n\nПродолжить сохранение?", totalShare));
            
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }
        
        return true;
    }
    
    @Override
    public void saveData() {
        StringBuilder report = new StringBuilder();
        report.append("=== РАЗДЕЛ 3. ПРАВООБЛАДАТЕЛИ ===\n\n");
        
        for (int i = 0; i < owners.size(); i++) {
            report.append(i + 1).append(". ").append(owners.get(i).toString()).append("\n");
        }
        
        report.append("\n--- ИТОГО ---\n");
        report.append("Количество правообладателей: ").append(owners.size()).append("\n");
        report.append("Сумма долей: ").append(totalShareLabel.getText()).append("\n");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сохранено");
        alert.setHeaderText("Раздел 3. Правообладатели сохранен");
        alert.setContentText(report.toString());
        alert.getDialogPane().setPrefWidth(800);
        alert.showAndWait();
        
        System.out.println(report);
    }
    
    @Override
    public void clearData() {
        owners.clear();
        ownersTable.refresh();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
