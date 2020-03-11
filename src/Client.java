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
            System.out.println("Hello. Please connect.");
            System.out.println("Enter your name : ");
            String connexion = inClient.readLine().trim();
            out.write(ChatamuProtocol.PREFIX_LOGIN + connexion + "\n");
            out.flush();
            System.out.println("Waiting for server ...");
            while(true) {
                String reponse = inServer.readLine().trim();
                if(reponse.equals(ChatamuProtocol.Error.ERROR_LOGIN)) throw new IOException("Error while connecting");
                else if (reponse.equals(ChatamuProtocol.LOGIN_SUCCESS)) break;
            }
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
        System.out.println("You're now connected and you can start tchating !");
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
        System.out.println("Connecting to localhost " + adress + ", port  " + ChatamuProtocol.DEFAULT_PORT );
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
                    if(inServer.readLine().trim().equals(ChatamuProtocol.Error.ERROR_MESSAGE))  throw new IOException("Error while sending message.");
                } catch (IOException e) {
                    System.out.println("Bye !");
                    break;
                }
            }
        }
    }
}
