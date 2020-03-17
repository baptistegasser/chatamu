import protocol.ChatamuProtocol;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public Client(String adress, int port) throws IOException {
        socket = new Socket(adress, port);
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    private boolean login(String pseudo) throws IOException {
        final byte[] msg = (ChatamuProtocol.PREFIX_LOGIN + pseudo.trim()).getBytes();
        outputStream.write(msg);
        outputStream.flush();

        byte[] buf = new byte[ChatamuProtocol.BUFFER_SIZE];
        inputStream.read(buf);

        final String resp = new String(buf).trim();
        return resp.equals(ChatamuProtocol.LOGIN_SUCCESS);
    }

    private void logout() throws IOException {
        outputStream.write(ChatamuProtocol.LOGOUT_MESSAGE.getBytes());
    }

    private void sendMessage(String content) throws IOException {
        final byte[] msg = (ChatamuProtocol.PREFIX_MESSAGE + content.trim()).getBytes();
        outputStream.write(msg);
        outputStream.flush();
    }

    public void launch()  {
        try {

            System.out.println("Hello. Please connect.");
            System.out.println("Enter your name : ");

            // Envoi du pseudo
            byte[] clientBuffer = new byte[256];
            System.in.read(clientBuffer);
            String pseudo = new String(clientBuffer).trim();

            boolean succes = login(pseudo);
            if(!succes) throw new IOException("Failed to login !");

            // Thread pour la lecture des messages
            new Thread(new HandlerReceived()).start();
            communication();
            closeAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void communication()  {
        System.out.println("You're now connected and you can start tchating !");
        String message;
        while (true) {
            try {
                // Envoi de messages
                byte[] clientBuffer = new byte[256];
                System.in.read(clientBuffer);
                message = new String(clientBuffer).trim();
                if(message.toLowerCase().equals("quit")) {
                    logout();
                    break;
                }
                sendMessage(message);
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void closeAll() throws IOException {
        socket.close();
        outputStream.close();
        inputStream.close();
    }

    public static void main(String[] args) {
        int port = ChatamuProtocol.DEFAULT_PORT;
        final String adress = "localhost";

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

        System.out.printf("Connecting to ChatAMU server at %s:%s%n", adress, port);
        try {
            Client client = new Client(adress, port);
            client.launch();
        } catch (Exception e) {
            System.err.println("Unexpected client failure, see stack trace below:");
            e.printStackTrace();
        }
    }

    private static void showUsage() {
        System.out.println("Correct usage of this client:");
        System.out.println("java Client [--port n]");
        System.out.printf("--port: n specify the target port, default to %d%n", ChatamuProtocol.DEFAULT_PORT);
    }

    class HandlerReceived implements Runnable {

        public void run () {
            String response;

            try {
                while (true) {
                    // Read a message from the server, if nothing was read, sleep 100ms
                    byte[] buf = new byte[256];
                    if (inputStream.read(buf) > 0) {
                        response = new String(buf).trim();

                        // Verify the response is not an error
                        if (response.equals(ChatamuProtocol.Error.ERROR_MESSAGE)) {
                            throw new IOException("Error while sending message.");
                        } else {
                            System.out.println(response);
                        }
                    } else {
                        Thread.sleep(100);
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Handler failed, see stack trace below:");
                e.printStackTrace();
            }
        }

    }
}
