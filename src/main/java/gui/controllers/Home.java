package gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {
    @FXML
    private TextField serverAddressField;
    @FXML
    private Button connectButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setOnConnectButtonAction(() -> System.err.println("No handler set for the connect button."));
    }

    public void setOnConnectButtonAction(Runnable callback) {
        connectButton.setOnAction(actionEvent -> callback.run());
    }

    public String getServerAddress() {
        return this.serverAddressField.getText();
    }
}
