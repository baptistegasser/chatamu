package client.gui.controllers;

public class MessageDisplayController implements Controller {
    public Void displayMessageFrom(String pseudo, String message) {
        System.out.println(pseudo + "> " + message);
        return null;
    }

    public void showUserJoined(String pseudo) {

    }

    public void showUserLeaved(String pseudo) {

    }
}
