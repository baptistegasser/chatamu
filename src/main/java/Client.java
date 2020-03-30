import protocol.ChatamuProtocol;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean loggedIn;

    public Client(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
        this.setLoggedIn(false);
    }

    public synchronized void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public synchronized boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Send a valid chatamu login message
     * @param pseudo The pseudo to use in the chat
     */
    private boolean login(String pseudo) throws IOException {
        final String msg = ChatamuProtocol.prefixContent(ChatamuProtocol.PREFIX_LOGIN, pseudo.trim());
        outputStream.write(msg.getBytes());
        outputStream.flush();

        byte[] buf = new byte[ChatamuProtocol.BUFFER_SIZE];
        int size = inputStream.read(buf);

        final String resp = new String(buf).trim();
        return resp.equals(ChatamuProtocol.LOGIN_SUCCESS);
    }

    /**
     * Send a valid chatamu logout message
     */
    private void logout() throws IOException {
        outputStream.write(ChatamuProtocol.LOGOUT_MESSAGE.getBytes());
    }

    /**
     * Send a valid chatamu message
     * @param content The content of the message
     */
    private void sendMessage(String content) throws IOException {
        final String msg = ChatamuProtocol.prefixContent(ChatamuProtocol.PREFIX_MESSAGE, content.trim());
        outputStream.write(msg.getBytes());
        outputStream.flush();
    }

    public void launch() throws IOException {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("Hello. Please connect.");
        System.out.print("Enter your name : ");

        // Read the user pseudo and logging
        boolean success = login(scanner.nextLine().trim());
        if (success) {
            System.out.println("You're now connected and you can start tchating !");
            this.setLoggedIn(true);
        } else {
            throw new IOException("Failed to login !");
        }

        // Start the server response handler.
        new Thread(new HandlerReceived()).start();

        // Loop while user have message to send
        String message;
        while (true) {
            message = scanner.nextLine().trim();
            if (message.toLowerCase().equals("quit")) {
                break;
            } else {
                sendMessage(message);
            }
        }

        // Logout from server
        logout();
        this.setLoggedIn(false);
    }

    /**
     * Close the open socket, streams...
     */
    public void closeAll() {
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            // Ignore the exception when closing
        }
    }

    /**
     * Handler charged of reading the message sent by the server
     */
    class HandlerReceived implements Runnable {
        public void run () {
            try {
                handle();
            } catch (Exception e) {
                System.out.println("Handler failed, see stack trace below:");
                e.printStackTrace();
            }
        }

        private void handle() throws IOException {
            String response;
            while (true) {
                // Read a message from the server, if nothing was read, sleep 100ms
                byte[] buf= new byte[ChatamuProtocol.BUFFER_SIZE];

                // Read from inputStream and handle case where the socket might close
                int size = 0;
                try {
                    size = inputStream.read(buf);
                } catch (SocketException e) {
                    if (!isLoggedIn()) {
                        break;
                    } else {
                        System.err.println("Socket was unexpectedly closed !");
                    }
                }

                // If we read something only
                if (size > 0) {
                    response = new String(buf).trim();

                    // Verify the response is not an error
                    if (response.equals(ChatamuProtocol.Error.ERROR_MESSAGE)) {
                        System.out.println("An invalid message was sent !");
                    } else {
                        final String[] split = response.split(" ", 2);
                        final String prefix = split[0];
                        final String content = split.length > 1 ? split[1] : "";

                        switch (prefix) {
                            case ChatamuProtocol.PREFIX_USER_CONNECTED:
                                System.out.printf("User '%s' joined !%n", content);
                                break;
                            case ChatamuProtocol.PREFIX_USER_DISCONNECTED:
                                System.out.printf("Oh no, user '%s' left !%n", content);
                                break;
                            default:
                                System.out.println(response);
                        }
                    }
                }
            }
        }
    }

    /**
     * Create a new client and start it
     * @param args Optional arguments
     */
    public static void main(String[] args) {
        // Default value for the client connection
        int port = ChatamuProtocol.DEFAULT_PORT;
        final String adress = "localhost";

        // Parse arguments if any
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
        Client client = null;
        try {
            client = new Client(adress, port);
            client.launch();
        } catch (Exception e) {
            System.err.println("Unexpected client failure, see stack trace below:");
            e.printStackTrace();
        } finally {
            // Clean all
            if (client != null) client.closeAll();
        }
    }

    /**
     * Show how to use this program
     */
    private static void showUsage() {
        System.out.println("Correct usage of this client:");
        System.out.println("java Client [--port n]");
        System.out.printf("--port: n specify the target port, default to %d%n", ChatamuProtocol.DEFAULT_PORT);
    }
}
