package client;

import client.event.Event;

import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class CLIClient extends AbstractClient {
    private String pseudo;
    private final Scanner scanner;
    private boolean isLogged;
    private final Semaphore loggedSemaphore;

    public CLIClient(String address, int port) {
        this(address, port, null);
    }

    public CLIClient(String address, int port, String pseudo) {
        super(address, port);
        this.pseudo = pseudo;
        this.scanner = new Scanner(System.in);
        this.isLogged = false;
        this.loggedSemaphore = new Semaphore(1);
        this.loggedSemaphore.tryAcquire();
    }

    @Override
    protected void launch() {
        this.tryToLogin();
        this.loggedSemaphore.tryAcquire();
        this.loggedSemaphore.release();
        String message = "";
        while (!message.equals("quit")) {
            message = scanner.nextLine().trim();
            this.core.sendMessage(message);
        }
        scanner.close();
        System.out.println("Login out");
        this.core.logout();
        this.isLogged = false;
        System.out.println("bye !");
    }

    private void tryToLogin() {
        if (isLogged) {
            System.err.println("Already logged !");
            return;
        }else if (this.pseudo == null) {
            System.out.print("Enter your pseudo : ");
            this.pseudo = scanner.nextLine().trim();
        }
        System.out.println("Attempting to login with pseudo '"+this.pseudo+"'");
        this.core.login(pseudo);
    }

    @Override
    protected void onError(Event event) {
        System.out.printf("Error: %s%n", event.message);
    }

    @Override
    protected void onLoginFail(Event event) {
        System.out.println("Login attempt as '"+this.pseudo+"' failed, please try again");
        this.pseudo = null;
        this.tryToLogin();
    }

    @Override
    protected void onLoginSuccess(Event event) {
        System.out.println("Logged in succesfully !\nYou can start sending message.");
        this.isLogged = true;
        this.loggedSemaphore.release();
    }

    @Override
    protected void onMessage(Event event) {
        System.out.printf("%s > %s.%n", event.pseudo, event.message);
    }

    @Override
    protected void onUserJoined(Event event) {
        System.out.printf("%s joined.%n", event.pseudo);
    }

    @Override
    protected void onUserLeaved(Event event) {
        System.out.printf("%s left.%n", event.pseudo);
    }

    public static void main(String[] args) {
        AbstractClient client = new CLIClient("127.0.0.1", 12345);
        client.start();
    }
}
