import java.io.*;
import java.net.Socket;

import protocol.*;

public class Client {

    final int port;
    private Socket socket;
    private BufferedWriter out;

    public Client(String adress) throws IOException {
        port = ChatamuProtocol.DEFAULT_PORT;
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
                if(message.equals("quit") || message.equals("QUIT")) {
                    out.write(ChatamuProtocol.LOGOUT_MESSAGE);
                    out.flush();
                    break;
                }
                out.write(ChatamuProtocol.PREFIX_MESSAGE + message);
                out.flush();
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
        String adress = "localhost";
        System.out.println("Connecting to localhost " + adress + ", port  " + ChatamuProtocol.DEFAULT_PORT );
            try {
                Client client = new Client(adress);
                client.launch();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
