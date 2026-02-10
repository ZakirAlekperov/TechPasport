package zakir.alekperov.ui.tabs.objectcomposition;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import zakir.alekperov.ui.tabs.objectcomposition.model.ObjectCompositionRow;

public class ObjectCompositionTabController {

    @FXML
    private TableView<ObjectCompositionRow> tableView;

    @FXML
    private TableColumn<ObjectCompositionRow, String> nameColumn;

    @FXML
    private TableColumn<ObjectCompositionRow, String> purposeColumn;

    @FXML
    private TableColumn<ObjectCompositionRow, String> areaColumn;

    @FXML
    private TableColumn<ObjectCompositionRow, String> floorsColumn;

    @FXML
    private TableColumn<ObjectCompositionRow, String> noteColumn;

    private final ObservableList<ObjectCompositionRow> rows =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        purposeColumn.setCellValueFactory(data -> data.getValue().purposeProperty());
        areaColumn.setCellValueFactory(data -> data.getValue().areaProperty());
        floorsColumn.setCellValueFactory(data -> data.getValue().floorsProperty());
        noteColumn.setCellValueFactory(data -> data.getValue().noteProperty());

        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        purposeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        areaColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        floorsColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        noteColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        tableView.setItems(rows);
    }

    @FXML
    private void onAddRow() {
        rows.add(new ObjectCompositionRow());
    }

    @FXML
    private void onRemoveRow() {
        var selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            rows.remove(selected);
        }
    }
}
