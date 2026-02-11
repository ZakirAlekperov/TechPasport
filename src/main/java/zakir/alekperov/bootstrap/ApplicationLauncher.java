package zakir.alekperov.bootstrap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import zakir.alekperov.ui.MainWindowController;

/**
 * Точка входа в приложение.
 * Инициализирует DI контейнер и загружает главное окно.
 */
public class ApplicationLauncher extends Application {
    
    private DependencyContainer dependencyContainer;

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║   TechPasport - Запуск приложения                 ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        
        // Инициализация DI контейнера
        dependencyContainer = new DependencyContainer();
        dependencyContainer.initialize();
        
        // Инициализация БД
        try {
            dependencyContainer.getDatabaseInitializer().initialize();
            System.out.println("✓ База данных инициализирована");
        } catch (Exception e) {
            System.err.println("✗ Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        // Создание главного контроллера с внедрением зависимостей
        MainWindowController mainController = new MainWindowController(dependencyContainer);
        
        // Загрузка FXML с предустановленным контроллером
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/ui/main/MainWindow.fxml")
        );
        
        // Установить контроллер вручную (без вызова конструктора FXML)
        loader.setController(mainController);
        
        Scene scene = new Scene(loader.load(), 1200, 800);
        
        scene.getStylesheets().add(
            getClass().getResource("/ui/styles/app.css").toExternalForm()
        );

        stage.setTitle("TechPasport - Технический паспорт объекта ИЖС");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            System.out.println("\n╔═══════════════════════════════════════════════════╗");
            System.out.println("║   Завершение работы приложения                    ║");
            System.out.println("╚═══════════════════════════════════════════════════╝");
            if (dependencyContainer != null) {
                dependencyContainer.shutdown();
            }
        });
        
        stage.show();
        
        System.out.println("\n✓ Приложение успешно запущено");
        System.out.println("═══════════════════════════════════════════════════\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
