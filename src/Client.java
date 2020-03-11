import java.io.*;
import java.net.Socket;
import protocol.*;

public class Client {

    final int port;
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader inServer;
    private BufferedReader inClient;

    public Client(int port, String adress) throws IOException {
        this.port = port;
        socket = new Socket(adress, port);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        inServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        inClient = new BufferedReader(new InputStreamReader(System.in));
    }

    public void launch()  {
        try {
            System.out.println("Bonjour, merci de vous connecter. \n");
            System.out.println("Quel est votre pseudo ? \n");
            String connexion = inClient.readLine().trim();
            out.write(ChatamuProtocol.PREFIX_LOGIN + connexion);
            out.flush();
            if (inServer.readLine().trim().equals(ChatamuProtocol.Error.ERROR_LOGIN)) throw new IOException("Erreur lors de la connexion");
            new Thread(new Handler(inServer)).start();
            communication();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void communication()  {
        System.out.println("Vous êtes connectés, vous pouvez maintenant communiquer.");
        String message;
        while (true) {
            try {
                message = inClient.readLine().trim();
                out.write(ChatamuProtocol.PREFIX_MESSAGE + message);
                out.flush();
                if(message.equals("quit") || message.equals("QUIT")) break;
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
                Client client = new Client(Integer.parseInt(args[0]),adress);
                client.launch();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage: java Client port");
        }
    }

    class Handler implements Runnable {

        BufferedReader reader;
        Handler(BufferedReader reader) throws IOException {
           this.reader = reader;
        }

        public void run () {
            String messageRecu;
            while(true) {
                try {
                    messageRecu = reader.readLine();
                    System.out.println(messageRecu);
                    if(inServer.readLine().trim().equals(ChatamuProtocol.Error.ERROR_MESSAGE))  throw new IOException("Erreur lors de l'envoi du message");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
