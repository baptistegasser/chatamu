package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.function.Function;

public class MessageInput {
    @FXML
    private TextField messageField;
    private Function<String, Void> callback;


    public void setOnMessageSend(Function<String, Void> callback) {
        this.callback = callback;
    }

    /**
     * Clear the message input field
     */
    private void clearMessageField() {
        this.messageField.clear();
    }

    /**
     * Send the message typed in chat
     */
    public void sendMessage() {
        final String message = messageField.getText();
        this.clearMessageField();
        this.callback.apply(message);
    }

    /**
     * Handle key press to send message if "enter" is pressed
     */
    public void handleKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            this.sendMessage();
        }
    }
}
