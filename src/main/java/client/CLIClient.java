package client;

import client.event.Event;
import protocol.ChatamuProtocol;

import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class CLIClient implements IClient {
    private final Core core;
    private String pseudo;
    private final Scanner scanner;
    private boolean isLogged;
    private final Semaphore loggedSemaphore;

    public CLIClient(String address, int port) {
        this(address, port, null);
    }

    public CLIClient(String address, int port, String pseudo) {
        this.core = new Core(address, port);
        this.pseudo = pseudo;
        this.scanner = new Scanner(System.in);
        this.isLogged = false;
        this.loggedSemaphore = new Semaphore(1);
        this.loggedSemaphore.tryAcquire();

        IClient.registerClientListeners(this);
    }

    protected void start() {
        core.start();
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
    public void onError(Event event) {
        System.out.printf("Error: %s%n", event.message);
    }

    @Override
    public void onLoginFail(Event event) {
        System.out.println("Login attempt as '"+this.pseudo+"' failed, please try again");
        this.pseudo = null;
        this.tryToLogin();
    }

    @Override
    public void onLoginSuccess(Event event) {
        System.out.println("Logged in succesfully !\nYou can start sending message.");
        this.isLogged = true;
        this.loggedSemaphore.release();
    }

    @Override
    public void onMessage(Event event) {
        System.out.printf("%s > %s%n", event.pseudo, event.message);
    }

    @Override
    public void onUserJoined(Event event) {
        System.out.printf("%s joined.%n", event.pseudo);
    }

    @Override
    public void onUserLeaved(Event event) {
        System.out.printf("%s left.%n", event.pseudo);
    }

    /**
     * Create a new CLI client and start it
     * @param args Optional arguments
     */
    public static void main(String[] args) {
        // Default value for the client connection
        int port = ChatamuProtocol.DEFAULT_PORT;
        final String address = "localhost";

        // Parse arguments if any
        // If you dont specify a port with the option, the default port will be 12345
        if (args.length >= 2) {
            if (args[0].equals("--port")) {
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    showUsage();
                    System.exit(-1);
                }
            }
        }

        // Start the client and handle error
        CLIClient client = null;
        try {
            client = new CLIClient(address, port);
            client.start();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected client failure, see stack trace below:");
            e.printStackTrace();
        }
    }

    /**
     * Show how to use this program
     */
    private static void showUsage() {
        System.out.println("Correct usage of this client:");
        System.out.println("java CLIClient [--port n]");
        System.out.printf("--port: n specify the target port, default to %d%n", ChatamuProtocol.DEFAULT_PORT);
    }
}
