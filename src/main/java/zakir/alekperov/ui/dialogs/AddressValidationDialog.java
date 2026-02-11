package zakir.alekperov.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import zakir.alekperov.service.address.AddressSuggestion;

import java.util.List;
import java.util.Optional;

/**
 * Диалог для выбора правильного варианта адреса
 */
public class AddressValidationDialog extends Dialog<AddressSuggestion> {
    
    public AddressValidationDialog(String enteredAddress, List<AddressSuggestion> suggestions) {
        setTitle("Проверка адреса");
        setHeaderText("Введенный адрес не найден точно.\nВыберите правильный вариант или исправьте ввод:");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Показываем введенный адрес
        Label enteredLabel = new Label("Вы ввели: " + enteredAddress);
        enteredLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        content.getChildren().add(enteredLabel);
        
        // Список вариантов
        if (!suggestions.isEmpty()) {
            Label suggestionLabel = new Label("Возможно, вы имели в виду:");
            suggestionLabel.setStyle("-fx-font-weight: bold;");
            content.getChildren().add(suggestionLabel);
            
            ListView<AddressSuggestion> listView = new ListView<>();
            listView.getItems().addAll(suggestions);
            listView.setPrefHeight(200);
            
            // Отображение в виде строки
            listView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(AddressSuggestion item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getUnrestricted());
                    }
                }
            });
            
            content.getChildren().add(listView);
            
            // Кнопки
            ButtonType selectButton = new ButtonType("Выбрать", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Исправить вручную", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            getDialogPane().getButtonTypes().addAll(selectButton, cancelButton);
            
            // Результат
            setResultConverter(buttonType -> {
                if (buttonType == selectButton) {
                    return listView.getSelectionModel().getSelectedItem();
                }
                return null;
            });
            
            // Автоматически выбираем первый вариант
            listView.getSelectionModel().select(0);
            
        } else {
            Label noSuggestions = new Label("К сожалению, подходящих адресов не найдено.\nПроверьте правильность написания.");
            noSuggestions.setStyle("-fx-text-fill: #d32f2f;");
            content.getChildren().add(noSuggestions);
            
            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().add(okButton);
        }
        
        getDialogPane().setContent(content);
    }
}
