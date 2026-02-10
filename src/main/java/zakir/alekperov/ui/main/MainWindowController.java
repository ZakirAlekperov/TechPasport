package zakir.alekperov.ui.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

/**
 * Главный контроллер окна приложения.
 * Управляет загрузкой и отображением всех вкладок технического паспорта.
 */
public class MainWindowController {

    @FXML
    private TabPane mainTabPane;

    @FXML
    private void initialize() {
        loadAllTabs();
    }

    /**
     * Загружает все вкладки технического паспорта.
     */
    private void loadAllTabs() {
        // 1. Общие сведения
        mainTabPane.getTabs().add(
                loadTab(
                        "1. Общие сведения",
                        "/ui/tabs/common-info/CommonInfoTab.fxml"
                )
        );
        
        // 2. Состав объекта
        mainTabPane.getTabs().add(
                loadTab(
                        "2. Состав объекта",
                        "/ui/tabs/object-composition/ObjectCompositionTab.fxml"
                )
        );
        
        // 3. Сведения о правообладателях
        mainTabPane.getTabs().add(
                loadTab(
                        "3. Правообладатели",
                        "/ui/tabs/owners/OwnersTab.fxml"
                )
        );
        
        // 4. Ситуационный план
        mainTabPane.getTabs().add(
                loadTab(
                        "4. Ситуационный план",
                        "/ui/tabs/location-plan/LocationPlanTab.fxml"
                )
        );
        
        // 5. Благоустройство
        mainTabPane.getTabs().add(
                loadTab(
                        "5. Благоустройство",
                        "/ui/tabs/improvement/ImprovementTab.fxml"
                )
        );
        
        // 6. Поэтажный план
        mainTabPane.getTabs().add(
                loadTab(
                        "6. Поэтажный план",
                        "/ui/tabs/floor-plan/FloorPlanTab.fxml"
                )
        );
        
        // 7. Экспликация
        mainTabPane.getTabs().add(
                loadTab(
                        "7. Экспликация",
                        "/ui/tabs/explication/ExplicationTab.fxml"
                )
        );
        
        // 8. Отметки об обследованиях
        mainTabPane.getTabs().add(
                loadTab(
                        "8. Обследования",
                        "/ui/tabs/inspection/InspectionTab.fxml"
                )
        );
        
        // Выбираем первую вкладку по умолчанию
        mainTabPane.getSelectionModel().selectFirst();
    }

    /**
     * Загружает вкладку из FXML файла.
     * 
     * @param title название вкладки
     * @param fxmlPath путь к FXML файлу
     * @return загруженная вкладка
     * @throws IllegalStateException если не удалось загрузить FXML
     */
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

    /**
     * Загружает вкладку-заглушку (для разработки).
     * Используется временно для вкладок, которые еще не реализованы.
     * 
     * @param title название вкладки
     * @param description описание содержимого
     * @return загруженная вкладка-заглушка
     * @throws IllegalStateException если не удалось загрузить заглушку
     */
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
