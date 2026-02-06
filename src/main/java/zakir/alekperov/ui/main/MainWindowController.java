package zakir.alekperov.ui.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private TabPane mainTabPane;

    @FXML
    private void initialize() {
        // Пока создаём заглушки вкладок
        mainTabPane.getTabs().add(
                loadTab(
                        "1. Общие сведения",
                        "/ui/tabs/common-info/CommonInfoTab.fxml"
                )
        );
        mainTabPane.getTabs().add(loadStubTab(
                "2. Состав объекта",
                "Пока заглушка"
        ));
        mainTabPane.getTabs().add(loadStubTab(
                "3. Сведения о правообладателях",
                "Пока заглушка"
        ));
        mainTabPane.getTabs().add(loadStubTab(
                "4. Ситуационный план",
                "Пока заглушка"
        ));
        mainTabPane.getTabs().add(loadStubTab(
                "5. Благоустройство",
                "Пока заглушка"
        ));
        mainTabPane.getTabs().add(loadStubTab(
                "6. Поэтажный план",
                "Пока заглушка"
        ));
        mainTabPane.getTabs().add(loadStubTab(
                "7. Экспликация",
                "Пока заглушка"
        ));
        mainTabPane.getTabs().add(loadStubTab(
                "8. Отметки об обследованиях",
                "Пока заглушка"
        ));
    }

    private Tab createTab(String title) {
        Tab tab = new Tab(title);
        tab.setClosable(false);
        return tab;
    }

    private Tab loadTab(String title, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );

            Tab tab = new Tab(title);
            tab.setClosable(false);
            tab.setContent(loader.load());
            return tab;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot load tab FXML: " + fxmlPath,
                    e
            );
        }
    }

    private Tab loadStubTab(String title, String description) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/tabs/stub/StubTab.fxml")
            );

            Tab tab = new Tab(title);
            tab.setClosable(false);

            tab.setContent(loader.load());

            var controller =
                    (zakir.alekperov.ui.tabs.stub.StubTabController)
                            loader.getController();

            controller.setup(title, description);

            return tab;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot load stub tab",
                    e
            );
        }
    }
}
