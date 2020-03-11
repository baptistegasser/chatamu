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

    public void launch()  {
        try {
            System.out.println("Hello. Please connect.");
            System.out.println("Enter your name : ");
            byte[] clientBuffer = new byte[256];
            System.in.read(clientBuffer);
            String reponse = new String(clientBuffer);
            out.write(ChatamuProtocol.PREFIX_LOGIN + reponse + "\n");
            out.flush();
            System.out.println("Waiting for server ...");
            while(true) {
                byte[] serverBuffer = new byte[256];
                socket.getInputStream().read(serverBuffer);
                reponse = new String(serverBuffer).trim();
                if(reponse.equals(ChatamuProtocol.Error.ERROR_LOGIN)) throw new IOException("Error while connecting");
                else if (reponse.equals(ChatamuProtocol.LOGIN_SUCCESS)) break;
            }
            new Thread(new HandlerReceived()).start();
            communication();
            closeAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeAll() throws IOException {
        socket.close();
        out.close();
    }

    public void communication()  {
        System.out.println("You're now connected and you can start tchating !");
        String message;
        while (true) {
            try {
                byte[] clientBuffer = new byte[256];
                System.in.read(clientBuffer);
                message = new String(clientBuffer).trim();
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

        public void run () {
            String messageRecv;
            while(true) {
                try {
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
