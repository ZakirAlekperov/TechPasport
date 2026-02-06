package zakir.alekperov.ui.tabs.stub;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StubTabController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label descriptionLabel;

    public void setup(String title, String description) {
        titleLabel.setText(title);
        descriptionLabel.setText(description);
    }
}
