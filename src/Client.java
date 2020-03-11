import java.io.*;
import java.net.Socket;
import protocol.*;

public class Client {

/*    final int port;
    String adress = null;

    public Client(int port, String adress) {
        this.port = port;
        this.adress = adress;
    }*/

    public static void  launch( int port, String adress)  {
        try {
            Socket socket = new Socket(adress,port);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader inServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader inClient = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Bonjour, merci de vous connecter. \n");
            System.out.println("Quel est votre pseudo ?");
            String connexion = inClient.readLine().trim();
            out.write(ChatamuProtocol.PREFIX_LOGIN + connexion);
            out.flush();
            if(inServer.readLine().trim().equals(ChatamuProtocol.Error.ERROR_LOGIN))  throw new IOException("Erreur lors de la connexion");
            communication(inClient, inServer, out);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void communication(BufferedReader inClient, BufferedReader inServer, BufferedWriter out ) {
        System.out.println("Vous êtes connectés, vous pouvez parler.");
        String message;
        while (true) {
            try {
                message = inClient.readLine().trim();
                out.write(ChatamuProtocol.PREFIX_MESSAGE + message);
                out.flush();
                if(message.equals("quit") || message.equals("QUIT")) break;
                if(inServer.readLine().trim().equals(ChatamuProtocol.Error.ERROR_MESSAGE))  throw new IOException("Erreur lors de l'envoi du message");
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int length = args.length;
        String adress = "localhost";
        System.out.println("Connexion au serveur d'adresse " + adress + " et de port " + Integer.parseInt(args[0]) );

        if (length == 1) {
            try {
                Client.launch(Integer.parseInt(args[0]),adress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage: java Client port");
        }
    }
}
