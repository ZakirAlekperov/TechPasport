package zakir.alekperov.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TabPane;

/**
 * Контроллер главного окна приложения
 */
public class MainWindowController {
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private void initialize() {
        // Инициализация при загрузке
    }
    
    @FXML
    private void handleNewPassport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Новый паспорт");
        alert.setHeaderText("Создание нового технического паспорта");
        alert.setContentText("Функция в разработке");
        alert.showAndWait();
    }
    
    @FXML
    private void handleOpenPassport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Открыть");
        alert.setHeaderText("Открытие существующего паспорта");
        alert.setContentText("Функция в разработке");
        alert.showAndWait();
    }
    
    @FXML
    private void handleSavePassport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сохранить");
        alert.setHeaderText("Сохранение технического паспорта");
        alert.setContentText("Функция в разработке");
        alert.showAndWait();
    }
    
    @FXML
    private void handleSaveAsPassport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сохранить как");
        alert.setHeaderText("Сохранение технического паспорта в новый файл");
        alert.setContentText("Функция в разработке");
        alert.showAndWait();
    }
    
    @FXML
    private void handleExportPDF() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Экспорт в PDF");
        alert.setHeaderText("Экспорт технического паспорта в PDF");
        alert.setContentText("Функция в разработке");
        alert.showAndWait();
    }
    
    @FXML
    private void handlePrint() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Печать");
        alert.setHeaderText("Печать технического паспорта");
        alert.setContentText("Функция в разработке");
        alert.showAndWait();
    }
    
    @FXML
    private void handleExit() {
        System.exit(0);
    }
    
    @FXML
private void handleAbout() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("О программе");
    alert.setHeaderText("📋 Система создания технических паспортов");
    
    String content = 
        "Версия: 1.0-SNAPSHOT\n" +
        "Дата: 11 февраля 2026 г.\n\n" +
        
        "Программа для автоматизированного создания\n" +
        "технических паспортов объектов индивидуального\n" +
        "жилищного строительства.\n\n" +
        
        "📜 Соответствует официальной форме\n" +
        "Приказ Минэкономразвития РФ от 17.08.2006 № 244\n\n" +
        
        "✨ Возможности:\n" +
        "  • Проверка адреса через базу ФИАС (DaData API)\n" +
        "  • Управление составом объекта\n" +
        "  • Автоматический расчет параметров\n" +
        "  • Экспорт в PDF (в разработке)\n\n" +
        
        "───────────────────────────────\n\n" +
        
        "👨‍💻 Автор: Закир Алекперов\n" +
        "© 2026 Все права защищены";
    
    alert.setContentText(content);
    alert.getDialogPane().setPrefWidth(500);
    alert.showAndWait();
}

}
