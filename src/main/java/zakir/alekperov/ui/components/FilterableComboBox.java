package zakir.alekperov.ui.components;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.function.Function;

/**
 * ComboBox с фильтрацией и поддержкой API подсказок
 */
public class FilterableComboBox<T> extends ComboBox<T> {
    
    private Function<String, List<T>> suggestionProvider;
    private boolean moveCaretToPos = false;
    private int caretPos;
    
    public FilterableComboBox() {
        super();
        setEditable(true);
        
        // Слушатель изменений в редактируемом поле
        getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            // Если значение было выбрано из списка, не делаем запрос
            if (getValue() != null && newValue.equals(getValue().toString())) {
                return;
            }
            
            if (newValue == null || newValue.isEmpty()) {
                hide();
                getItems().clear();
                return;
            }
            
            // Минимум 2 символа для поиска
            if (newValue.length() < 2) {
                hide();
                getItems().clear();
                return;
            }
            
            // Если не фокус на элементе, не показываем
            if (!isFocused() && !getEditor().isFocused()) {
                return;
            }
            
            // Получаем подсказки через провайдер
            if (suggestionProvider != null) {
                fetchAndShowSuggestions(newValue);
            }
        });
        
        // При выборе из списка - устанавливаем значение
        setOnAction(event -> {
            T selected = getSelectionModel().getSelectedItem();
            if (selected != null) {
                setValue(selected);
                getEditor().setText(selected.toString());
                hide();
            }
        });
        
        // Обработка клавиш
        getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Enter - выбираем первый элемент если список открыт
                if (isShowing() && !getItems().isEmpty()) {
                    T firstItem = getItems().get(0);
                    setValue(firstItem);
                    getEditor().setText(firstItem.toString());
                    hide();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hide();
            }
        });
        
        // При клике на элемент списка
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                setValue(newVal);
                getEditor().setText(newVal.toString());
                
                // Перемещаем каретку в конец
                Platform.runLater(() -> {
                    getEditor().positionCaret(getEditor().getText().length());
                });
            }
        });
    }
    
    /**
     * Устанавливает провайдер подсказок из API
     */
    public void setSuggestionProvider(Function<String, List<T>> provider) {
        this.suggestionProvider = provider;
    }
    
    /**
     * Получает подсказки из API в фоновом потоке
     */
    private void fetchAndShowSuggestions(String query) {
        new Thread(() -> {
            try {
                List<T> suggestions = suggestionProvider.apply(query);
                
                // Обновляем UI в JavaFX потоке
                Platform.runLater(() -> {
                    if (suggestions != null && !suggestions.isEmpty()) {
                        getItems().clear();
                        getItems().addAll(suggestions);
                        
                        // Показываем список
                        if (!isShowing()) {
                            show();
                        }
                    } else {
                        hide();
                    }
                });
                
            } catch (Exception e) {
                System.err.println("Ошибка получения подсказок: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Получить выбранное значение как строку
     */
    public String getSelectedValue() {
        T value = getValue();
        return value != null ? value.toString() : getEditor().getText();
    }
}
