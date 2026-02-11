package zakir.alekperov.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import zakir.alekperov.bootstrap.DependencyContainer;
import zakir.alekperov.ui.tabs.base.BaseTabController;
import zakir.alekperov.ui.tabs.commoninfo.CommonInfoTabController;
import zakir.alekperov.ui.tabs.locationplan.LocationPlanTabController;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер главного окна приложения.
 * Управляет вкладками и интегрирует их с DependencyContainer.
 */
public class MainWindowController {
    
    private final DependencyContainer dependencyContainer;
    private final Map<Tab, BaseTabController> tabControllers = new HashMap<>();
    private String currentPassportId;
    
    @FXML
    private TabPane tabPane;
    
    /**
     * Конструктор с внедрением зависимостей.
     */
    public MainWindowController(DependencyContainer dependencyContainer) {
        if (dependencyContainer == null) {
            throw new IllegalArgumentException("DependencyContainer не может быть null");
        }
        this.dependencyContainer = dependencyContainer;
    }
    
    @FXML
    private void initialize() {
        System.out.println("✓ MainWindowController инициализирован");
        
        // Установить тестовый паспорт для демонстрации
        loadTestPassport();
        
        // 🌍 НАСТРОЙКА АВТОМАТИЧЕСКОГО ОПРЕДЕЛЕНИЯ СИСТЕМЫ КООРДИНАТ
        setupRegionSync();
    }
    
    /**
     * 🌍 НОВЫЙ МЕТОД: Настроить синхронизацию региона между вкладками.
     * Автоматически передает регион из "Общие сведения" на "Ситуационный план".
     */
    private void setupRegionSync() {
        try {
            CommonInfoTabController commonInfoController = dependencyContainer.getCommonInfoTabController();
            LocationPlanTabController locationPlanController = dependencyContainer.getLocationPlanTabController();
            
            if (commonInfoController == null || locationPlanController == null) {
                System.out.println("⚠️ Контроллеры еще не инициализированы, синхронизация региона будет настроена позже");
                return;
            }
            
            // Установить listener на изменение региона
            commonInfoController.setRegionChangeListener(region -> {
                System.out.println("✅ [MainWindow] Передаю регион на ситуационный план: " + region);
                locationPlanController.setRegion(region);
            });
            
            // Инициализировать, если регион уже заполнен
            String currentRegion = commonInfoController.getCurrentRegion();
            if (currentRegion != null && !currentRegion.isBlank()) {
                System.out.println("🔍 [MainWindow] Текущий регион: " + currentRegion);
                locationPlanController.setRegion(currentRegion);
            }
            
            System.out.println("✓ Синхронизация региона настроена");
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка настройки синхронизации региона: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Загрузить тестовый паспорт для демонстрации.
     */
    private void loadTestPassport() {
        // Используем тестовый ID, созданный TestDataCreator
        currentPassportId = "test-passport-001";
        System.out.println("✓ Загружен тестовый паспорт: " + currentPassportId);
        
        // Уведомить все контроллеры о новом паспорте
        notifyControllersAboutPassportChange();
    }
    
    /**
     * Установить текущий ID паспорта.
     */
    public void setCurrentPassportId(String passportId) {
        if (passportId == null || passportId.isBlank()) {
            throw new IllegalArgumentException("ID паспорта не может быть пустым");
        }
        this.currentPassportId = passportId;
        notifyControllersAboutPassportChange();
    }
    
    /**
     * Уведомить все контроллеры вкладок о смене паспорта.
     */
    private void notifyControllersAboutPassportChange() {
        if (currentPassportId == null) {
            return;
        }
        
        // Явно установить passportId для LocationPlanTabController из DI
        try {
            LocationPlanTabController locationPlanController = dependencyContainer.getLocationPlanTabController();
            if (locationPlanController != null) {
                locationPlanController.setPassportId(currentPassportId);
                System.out.println("  → Паспорт установлен для: LocationPlanTabController");
            }
        } catch (Exception e) {
            System.err.println("Ошибка установки паспорта для LocationPlanTabController: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Для остальных контроллеров через рефлексию
        for (BaseTabController controller : tabControllers.values()) {
            try {
                var method = controller.getClass().getMethod("setPassportId", String.class);
                method.invoke(controller, currentPassportId);
                System.out.println("  → Паспорт установлен для: " + controller.getClass().getSimpleName());
            } catch (NoSuchMethodException e) {
                // Контроллер не поддерживает setPassportId - это нормально
            } catch (Exception e) {
                System.err.println("Ошибка установки паспорта для контроллера: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Получить контроллер DI.
     */
    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
    
    // === Обработчики меню ===
    
    @FXML
    private void handleNewPassport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Новый паспорт");
        alert.setHeaderText("Создание нового технического паспорта");
        alert.setContentText("Функция в разработке.\n\nВы можете использовать текущий тестовый паспорт:\nID: " + currentPassportId);
        alert.showAndWait();
    }
    
    @FXML
    private void handleOpenPassport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Открыть");
        alert.setHeaderText("Открытие существующего паспорта");
        alert.setContentText("Функция в разработке.\n\nТекущий паспорт: " + currentPassportId);
        alert.showAndWait();
    }
    
    @FXML
    private void handleSavePassport() {
        if (currentPassportId == null) {
            showWarning("Паспорт не выбран", "Сначала создайте или откройте паспорт");
            return;
        }
        
        // Сохранить данные из LocationPlanTabController
        try {
            LocationPlanTabController controller = dependencyContainer.getLocationPlanTabController();
            if (controller != null) {
                controller.saveData();
                showInfo("Сохранение завершено", "Данные ситуационного плана сохранены");
            }
        } catch (Exception e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
            showWarning("Ошибка", "Не удалось сохранить данные: " + e.getMessage());
        }
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
        // Закрыть соединение с БД перед выходом
        try {
            dependencyContainer.close();
            System.out.println("✓ Соединение с БД закрыто");
        } catch (Exception e) {
            System.err.println("Ошибка закрытия БД: " + e.getMessage());
        }
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
            "  • Ситуационный план с координатами зданий\n" +
            "  • Автоматический расчет параметров\n" +
            "  • Экспорт в PDF (в разработке)\n\n" +
            
            "🏗️ Архитектура:\n" +
            "  • Clean Architecture (Domain-Driven Design)\n" +
            "  • Строгое разделение слоёв\n" +
            "  • Ручное управление зависимостями\n\n" +
            
            "💾 База данных: SQLite\n" +
            "📂 Расположение: ~/.techpasport/\n\n" +
            
            "───────────────────────────────\n\n" +
            
            "👨‍💻 Автор: Закир Алекперов\n" +
            "© 2026 Все права защищены";
        
        alert.setContentText(content);
        alert.getDialogPane().setPrefWidth(550);
        alert.showAndWait();
    }
    
    // === Вспомогательные методы ===
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
