import java.io.*;
import java.net.Socket;

import protocol.*;

public class Client {

    final int port;
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader inServer;
    private BufferedReader inClient;

    public Client(String adress) throws IOException {
        port = ChatamuProtocol.DEFAULT_PORT;
        socket = new Socket(adress, port);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        inServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        inClient = new BufferedReader(new InputStreamReader(System.in));
    }

    public void launch()  {
        try {
            System.out.println("Bonjour, merci de vous connecter.");
            System.out.println("Quel est votre pseudo ?");
            String connexion = inClient.readLine().trim();
            out.write(ChatamuProtocol.PREFIX_LOGIN + connexion + "\n");
            out.flush();
            if (inServer.readLine().trim().equals(ChatamuProtocol.Error.ERROR_LOGIN)) throw new IOException("Erreur lors de la connexion");
            new Thread(new HandlerReceived(inServer)).start();
            communication();
            closeAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeAll() throws IOException {
        socket.close();
        inServer.close();
        inClient.close();
        out.close();
    }

    public void communication()  {
        System.out.println("Vous êtes connectés, vous pouvez maintenant communiquer.");
        String message;
        while (true) {
            try {
                message = inClient.readLine().trim();
                out.write(ChatamuProtocol.PREFIX_MESSAGE + message + "\n");
                out.flush();
                if(message.equals("quit") || message.equals("QUIT")) break;
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String adress = "localhost";
        System.out.println("Connexion au serveur d'adresse " + adress + " et de port " + ChatamuProtocol.DEFAULT_PORT );
            try {
                Client client = new Client(adress);
                client.launch();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    class HandlerReceived implements Runnable {
        private BufferedReader reader;

        HandlerReceived(BufferedReader reader) {
           this.reader = reader;
        }

        public void run () {
            String messageRecv;
            while(true) {
                try {
                    messageRecv = reader.readLine();
                    System.out.println(messageRecv);
                    if(inServer.readLine().trim().equals(ChatamuProtocol.Error.ERROR_MESSAGE))  throw new IOException("Erreur lors de l'envoi du message");
                } catch (IOException e) {
                    System.out.println("Au revoir");
                    break;
                }
            }
        }
    }
}
