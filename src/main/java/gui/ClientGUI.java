package gui;

import gui.controllers.Home;
import gui.controllers.MessageDisplay;
import gui.controllers.MessageInput;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class ClientGUI extends Application {
    private Stage primaryStage;
    private Stage inputStage = new Stage();
    private Stage displayStage = new Stage();

    private Home homeController;
    private final String homeFXML           = "home.fxml";
    private final String messageInputFXML   = "messageInput.fxml";
    private final String messageDisplayFXML = "messageDisplay.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(new FileInputStream(loadFileFromRessources(homeFXML)));
        this.homeController = loader.getController();
        this.homeController.setOnConnectButtonAction(this::displayChatWindows);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("chatamu");
        this.showHome();
    }

    private void showHome() {
        if (this.inputStage.isShowing()) this.inputStage.close();
        if (this.displayStage.isShowing()) this.displayStage.close();

        this.displayStage(primaryStage, "Home");
    }

    private void displayStage(Stage stage, String title) {
        stage.setTitle("ChatAMU GUI | " + title);
        stage.sizeToScene();
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    private void displayChatWindows(){
        final String address = homeController.getServerAddress();

        MessageInput inputController = (MessageInput) this.configureChatSubWindowsStage(inputStage, messageInputFXML);
        MessageDisplay displayController = (MessageDisplay) this.configureChatSubWindowsStage(displayStage, messageDisplayFXML);

        inputController.setOnMessageSend(displayController::displayMessage);

        primaryStage.hide();
        this.displayStage(inputStage, "Message input");
        this.displayStage(displayStage, "Chat display");
    }

    private Object configureChatSubWindowsStage(Stage stage, String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(new FileInputStream(loadFileFromRessources(fxmlName)));

            if (stage.isShowing()) stage.close();
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(windowEvent -> showHome());

            return loader.getController();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private File loadFileFromRessources(String fileName) throws IOException {
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IOException("invalid fxml file name !");
        } else {
            return new File(resource.getFile());
        }
    }

    public static void main(String[] args) {
        ClientGUI.launch(args);
    }
}
