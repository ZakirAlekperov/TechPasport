package zakir.alekperov.ui.tabs.base;

import javafx.fxml.FXML;

/**
 * Базовый контроллер для всех вкладок технического паспорта.
 * Содержит общую логику инициализации и валидации.
 */
public abstract class BaseTabController {
    
    @FXML
    protected void initialize() {
        setupBindings();
        setupValidation();
        loadInitialData();
    }
    
    /**
     * Настройка привязок данных к UI элементам.
     * Переопределяется в наследниках.
     */
    protected void setupBindings() {
        // Реализация в наследниках
    }
    
    /**
     * Настройка валидации полей ввода.
     * Переопределяется в наследниках.
     */
    protected void setupValidation() {
        // Реализация в наследниках
    }
    
    /**
     * Загрузка начальных данных для вкладки.
     * Переопределяется в наследниках.
     */
    protected void loadInitialData() {
        // Реализация в наследниках
    }
    
    /**
     * Валидация данных перед сохранением.
     * @return true если данные корректны
     */
    public abstract boolean validateData();
    
    /**
     * Сохранение данных вкладки.
     */
    public abstract void saveData();
    
    /**
     * Очистка всех полей вкладки.
     */
    public abstract void clearData();
}
