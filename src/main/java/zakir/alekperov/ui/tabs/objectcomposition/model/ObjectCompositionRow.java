package zakir.alekperov.ui.tabs.objectcomposition.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ObjectCompositionRow {

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty purpose = new SimpleStringProperty("");
    private final StringProperty area = new SimpleStringProperty("");
    private final StringProperty floors = new SimpleStringProperty("");
    private final StringProperty note = new SimpleStringProperty("");

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty purposeProperty() {
        return purpose;
    }

    public StringProperty areaProperty() {
        return area;
    }

    public StringProperty floorsProperty() {
        return floors;
    }

    public StringProperty noteProperty() {
        return note;
    }
}
