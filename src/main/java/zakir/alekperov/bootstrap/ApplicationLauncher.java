package zakir.alekperov.bootstrap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ApplicationLauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/main/MainWindow.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 800);
        
        scene.getStylesheets().add(
            getClass().getResource("/ui/styles/app.css").toExternalForm()
        );

        stage.setTitle("TechPasport");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
