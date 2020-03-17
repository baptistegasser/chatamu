import protocol.ChatamuProtocol;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedWriter out;

    public Client(String adress, int port) throws IOException {
        socket = new Socket(adress, port);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    private boolean login(String pseudo) throws IOException {
        final byte[] msg = (ChatamuProtocol.PREFIX_LOGIN + pseudo.trim()).getBytes();
        out.write(new String(msg));//TODO remove horror
        out.flush();

        byte[] buf = new byte[ChatamuProtocol.BUFFER_SIZE];
        socket.getInputStream().read(buf);

        final String resp = new String(buf).trim();
        return resp.equals(ChatamuProtocol.LOGIN_SUCCESS);
    }

    private void logout() throws IOException {
        out.write(ChatamuProtocol.LOGOUT_MESSAGE);//TODO remove horror
        out.flush();
    }

    private void sendMessage(String content) throws IOException {
        final byte[] msg = (ChatamuProtocol.PREFIX_MESSAGE + content.trim()).getBytes();
        out.write(new String(msg));//TODO remove horror
        out.flush();
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
        out.close();
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
            String messageRecv;
            while(true) {
                try {
                    // TODO Il faut gérer le cas où il y a un probleme ici mais que le client peux enncore envoyer des  messages
                    // TODO Voir s'il faut pas copier le socket dans le constructeur de HandlerReceived
                    // Lecture des messages du serveur
                    byte[] serverBuffer = new byte[256];
                    socket.getInputStream().read(serverBuffer);
                    messageRecv = new String(serverBuffer).trim();
                    System.out.println(messageRecv);
                    if(messageRecv.equals(ChatamuProtocol.Error.ERROR_MESSAGE))  throw new IOException("Error while sending message.");
                } catch (IOException e) {
                    System.out.println("Bye !");
                    break;
                }
            }
        }

    }
}
