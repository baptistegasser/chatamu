package client.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.function.Function;

public class LoginController implements Controller {
    @FXML
    TextField serverAddressField, serverPortField, pseudoField;

    private Function<String[], Void> loginCallback;

    public void setLoginCallback(Function<String[], Void> loginCallback) {
        this.loginCallback = loginCallback;
    }

    @FXML
    public void login() {
        loginCallback.apply(new String[]{serverAddressField.getText(), serverPortField.getText(), pseudoField.getText()});
    }
}
