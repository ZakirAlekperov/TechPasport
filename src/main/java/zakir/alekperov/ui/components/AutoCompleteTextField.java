package zakir.alekperov.ui.components;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.function.Function;

/**
 * TextField с автоподсказками
 */
public class AutoCompleteTextField<T> extends TextField {
    
    private final ContextMenu suggestionsPopup;
    private Function<String, List<T>> suggestionProvider;
    private int maxSuggestions = 10;
    
    public AutoCompleteTextField() {
        super();
        this.suggestionsPopup = new ContextMenu();
        
        // Слушатель изменений текста
        textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                suggestionsPopup.hide();
                return;
            }
            
            // Минимум 2 символа для поиска
            if (newValue.length() < 2) {
                suggestionsPopup.hide();
                return;
            }
            
            // Получаем подсказки
            if (suggestionProvider != null) {
                fetchSuggestions(newValue);
            }
        });
        
        // Закрываем popup при потере фокуса
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                suggestionsPopup.hide();
            }
        });
    }
    
    /**
     * Устанавливает провайдер подсказок
     */
    public void setSuggestionProvider(Function<String, List<T>> provider) {
        this.suggestionProvider = provider;
    }
    
    /**
     * Устанавливает максимальное количество подсказок
     */
    public void setMaxSuggestions(int max) {
        this.maxSuggestions = max;
    }
    
    /**
     * Получает подсказки в отдельном потоке
     */
    private void fetchSuggestions(String query) {
        // Выполняем в фоновом потоке, чтобы не блокировать UI
        new Thread(() -> {
            try {
                List<T> suggestions = suggestionProvider.apply(query);
                
                // Обновляем UI в JavaFX потоке
                Platform.runLater(() -> showSuggestions(suggestions));
                
            } catch (Exception e) {
                System.err.println("Ошибка получения подсказок: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Показывает подсказки в popup
     */
    private void showSuggestions(List<T> suggestions) {
        suggestionsPopup.getItems().clear();
        
        if (suggestions == null || suggestions.isEmpty()) {
            suggestionsPopup.hide();
            return;
        }
        
        // Ограничиваем количество
        int count = Math.min(suggestions.size(), maxSuggestions);
        
        for (int i = 0; i < count; i++) {
            T suggestion = suggestions.get(i);
            
            Label label = new Label(suggestion.toString());
            label.setStyle("-fx-padding: 5 10 5 10; -fx-cursor: hand;");
            
            CustomMenuItem item = new CustomMenuItem(label, true);
            item.setOnAction(event -> {
                setText(suggestion.toString());
                positionCaret(getText().length());
                suggestionsPopup.hide();
            });
            
            suggestionsPopup.getItems().add(item);
        }
        
        // Показываем popup
        if (!suggestionsPopup.isShowing()) {
            suggestionsPopup.show(this, Side.BOTTOM, 0, 0);
        }
    }
}
