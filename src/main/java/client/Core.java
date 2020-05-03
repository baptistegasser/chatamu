package client;

import client.event.EventDispatcher;
import client.event.EventHandler;
import protocol.ChatamuProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Core {
    private SocketHandler socketHandler;
    ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    public Core(String address, int port) {
        this.socketHandler = new SocketHandler(address, port);
    }

    public void start() {
        if (socketHandler.isStarted) {
            throw new IllegalStateException("Client handler is already started !");
        }
        threadExecutor.submit(socketHandler);
    }

    class SocketHandler implements Runnable {
        private final EventDispatcher dispatcher;

        private boolean isStarted;
        private int port;
        private String address;
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SocketHandler(String address, int port) {
            this.dispatcher = EventDispatcher.getInstance();

            this.port = port;
            this.address = address;
            this.isStarted = false;
        }

        @Override
        public void run() {
            try {
                this.isStarted = true;
                this.socket = new Socket(address, port);
                this.inputStream = socket.getInputStream();
                this.outputStream = socket.getOutputStream();

                while (!socket.isClosed()) {
                    // TODO
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Function to send a message to the server
         * @param message The message to send
         */
        private void write(String message) {
            try {
                final byte[] bytes = message.getBytes();
                outputStream.write(bytes);
                outputStream.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        /**
         * Read a message from the server
         * @return The message read or null on error/empty message
         */
        private String read() {
            try {
                byte[] buf = new byte[ChatamuProtocol.BUFFER_SIZE];
                int size = inputStream.read(buf);
                if (size >= 0) {
                    return null;
                } else {
                    return new String(buf);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
        }

        /**
         * Send a message instruction to the server
         * @param content The content of the message
         */
        private void sendMessage(String content) {
            this.write(ChatamuProtocol.prefixContent(ChatamuProtocol.PREFIX_MESSAGE, content).trim());
        }

        /**
         * Send a login instruction to the server
         * @param pseudo The pseudo to use
         */
        private void login(String pseudo) {
            this.write(ChatamuProtocol.prefixContent(ChatamuProtocol.PREFIX_LOGIN, pseudo.trim()));
        }

        /**
         * Send a logout instruction to the server
         */
        private void logout() {
            this.write(ChatamuProtocol.LOGOUT_MESSAGE);
        }
    }
}
