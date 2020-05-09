package client.gui.controllers;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MessageDisplayController implements Controller {
    @FXML
    private VBox messageContainer;

    private String lastUser = "";

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-padding: 0 10 0 10");
        ChangeListener<Number> listener = (observableValue, number, t1) -> {
            final double w = t1.doubleValue();
            label.setMinWidth(w);
            label.setPrefWidth(w);
            label.setMaxWidth(w);
        };
        listener.changed(null, null, messageContainer.getWidth());
        messageContainer.widthProperty().addListener(listener);
        return label;
    }

    private void addMessage(Label label) {
        messageContainer.getChildren().add(label);
    }

    public void displayMessage(String message) {
        Label label = this.createLabel(message + " |");
        label.setAlignment(Pos.CENTER_RIGHT);
        if (!lastUser.equals("")) { label.setStyle("-fx-padding: 10 10 0 10");}
        lastUser = "";
        this.addMessage(label);
    }

    public void displayMessageFrom(String pseudo, String message) {
        Label label;
        final String _message = "  | " + message;
        if (lastUser.equals(pseudo)) {
            label = this.createLabel(_message);
        } else {
            lastUser = pseudo;
            label = this.createLabel("# " + pseudo + "\n" + _message);
            label.setStyle("-fx-padding: 10 10 0 10");
        }
        this.addMessage(label);
    }

    public void showUserJoined(String pseudo) {
        Label label = this.createLabel("User '"+pseudo+"' joined !");
        label.setTextFill(Color.GREEN);
        this.addMessage(label);
    }

    public void showUserLeaved(String pseudo) {
        Label label = this.createLabel("User '"+pseudo+"' leaved !");
        label.setTextFill(Color.RED);
        this.addMessage(label);
    }
}
