package zakir.alekperov.ui.main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class MainWindowController {

        @FXML
        private TabPane mainTabPane;

        @FXML
        private void initialize() {

                mainTabPane.getTabs().add(
                                loadTab(
                                                "1. Общие сведения",
                                                "Основная информация об объекте недвижимости",
                                                "/ui/tabs/common-info/CommonInfoTab.fxml"));

                mainTabPane.getTabs().add(
                                loadTab(
                                                "2. Состав объекта",
                                                "Сведения о составе и характеристиках объекта",
                                                "/ui/tabs/object-composition/ObjectCompositionTab.fxml"));

                mainTabPane.getTabs().add(
                                loadTab(
                                                "3. Сведения о правообладателях",
                                                "Информация о собственниках и правах",
                                                "/ui/tabs/owners/OwnersTab.fxml"));

                mainTabPane.getTabs().add(
                                loadTab(
                                                "4. Ситуационный план",
                                                "Расположение объекта на местности",
                                                "/ui/tabs/location-plan/LocationPlanTab.fxml"));

                mainTabPane.getTabs().add(
                                loadTab(
                                                "5. Благоустройство",
                                                "Элементы благоустройства территории",
                                                "/ui/tabs/improvement/ImprovementTab.fxml"));

                mainTabPane.getTabs().add(
                                loadTab(
                                                "6. Поэтажный план",
                                                "Планы этажей здания",
                                                "/ui/tabs/floor-plan/FloorPlanTab.fxml"));

                mainTabPane.getTabs().add(
                                loadTab(
                                                "7. Экспликация",
                                                "Назначение и площади помещений",
                                                "/ui/tabs/explication/ExplicationTab.fxml"));

                mainTabPane.getTabs().add(
                                loadTab(
                                                "8. Отметки об обследованиях",
                                                "История обследований и замечаний",
                                                "/ui/tabs/inspections/InspectionsTab.fxml"));
        }

        private Tab loadTab(String title, String description, String fxmlPath) {
                try {
                        FXMLLoader loader = new FXMLLoader(
                                        getClass().getResource(fxmlPath));

                        Parent content = loader.load();

                        Tab tab = new Tab(title);
                        tab.setClosable(false);
                        tab.setContent(content);

                        return tab;

                } catch (IOException e) {
                        throw new IllegalStateException(
                                        "Cannot load tab FXML: " + fxmlPath,
                                        e);
                }
        }

}
