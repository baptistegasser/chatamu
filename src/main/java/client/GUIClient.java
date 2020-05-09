package client;

import client.event.Event;
import client.gui.controllers.Controller;
import client.gui.controllers.LoginController;
import client.gui.controllers.MessageDisplayController;
import client.gui.controllers.MessageInputController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class GUIClient extends Application implements IClient {
    private Core core;

    private Stage primaryStage;
    private final Stage loginStage = new Stage();
    private final Stage inputStage = new Stage();
    private final Stage displayStage = new Stage();
    private LoginController loginController;
    private MessageInputController inputController;
    private MessageDisplayController displayController;

    public GUIClient() {
        IClient.registerClientListeners(this);
        this.loginController = (LoginController) this.loadStage(loginStage, "login.fxml");
        this.inputController = (MessageInputController) this.loadStage(inputStage, "messageInput.fxml");
        this.displayController = (MessageDisplayController) this.loadStage(displayStage, "messageDisplay.fxml");
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        loginStage.setOnCloseRequest(e -> this.quit());
        inputStage.setOnCloseRequest(e -> this.quit());
        displayStage.setOnCloseRequest(e -> this.quit());

        loginController.setLoginCallback(this::login);
        this.displayStage(loginStage, "Login");
    }

    private Void login(String[] args) {
        int port = 12345;
        try {
            port = Integer.parseInt(args[1]);
        } catch (Exception ignored) {}

        this.core = new Core(args[0], port);
        if (!this.core.start()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Failed to connect to the server");
            alert.setHeaderText(core.getErrorMessage());
            alert.showAndWait();
        } else {
            this.core.login(args[2]);
        }
        return null;
    }

    private void displayStage(Stage stage, String title) {
        stage.setTitle("ChatAMU GUI | " + title);
        stage.sizeToScene();
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
        this.bringToFront(stage);
    }

    private void quit() {
        this.core.logout();
        this.core.close();
        loginStage.close();
        inputStage.close();
        displayStage.close();
        primaryStage.close();
        Platform.exit();
    }

    @Override
    public void onError(Event event) {

    }

    @Override
    public void onLoginFail(Event event) {

    }

    @Override
    public void onLoginSuccess(Event event) {
        Platform.runLater(() -> {
            this.loginStage.close();
            this.inputController.setOnMessageSend(s -> {
                this.core.sendMessage(s);
                this.displayController.displayMessage(s);
                return null;
            });
            this.displayStage(displayStage, "Chat display");
            this.displayStage(inputStage, "Message input");
        });
    }

    @Override
    public void onMessage(Event event) {
        Platform.runLater(() -> this.displayController.displayMessageFrom(event.pseudo, event.message));
    }

    @Override
    public void onUserJoined(Event event) {
        Platform.runLater(() -> this.displayController.showUserJoined(event.pseudo));
    }

    @Override
    public void onUserLeaved(Event event) {
        Platform.runLater(() -> this.displayController.showUserLeaved(event.pseudo));
    }

    /**
     * Bring a window to the front of other windows
     * @param stage The stage of the window
     */
    private void bringToFront(Stage stage) {
        final boolean alwaysOnTop = stage.isAlwaysOnTop();
        stage.setAlwaysOnTop(true);
        stage.setAlwaysOnTop(alwaysOnTop);
    }

    /**
     * Load a fxml definition into a stage and return a Controller
     * @param stage The target stage to contain the scene
     * @param fxmlName The fxml file name, stored in "resources" folder
     * @return The Controller of this stage.
     */
    private Controller loadStage(Stage stage, String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.load(new FileInputStream(loadFileFromResources(fxmlName)));

            if (stage.isShowing()) stage.close();
            stage.setScene(new Scene(loader.getRoot()));

            return loader.getController();
        } catch (IOException ioe) {
            System.out.println("Failed to load stage | Stack Trace START");
            ioe.printStackTrace();
            System.out.println("Stack Trace END");
        }
        return null;
    }

    /**
     * Load a file from the resources folder
     * @param fileName The file's name
     * @return The file
     * @throws IOException Getting a file can lead to i/o errors
     */
    private File loadFileFromResources(String fileName) throws IOException {
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IOException("invalid fxml file name !");
        } else {
            return new File(resource.getFile());
        }
    }
}
