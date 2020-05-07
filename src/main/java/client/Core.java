package client;

import client.event.Event;
import client.event.EventDispatcher;
import client.event.EventFactory;
import protocol.ChatamuProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Core {
    private final SocketHandler socketHandler;
    private final ExecutorService threadExecutor;
    private final Semaphore isStartingSemaphore;
    private String errorMessage = null;

    public Core(String address, int port) {
        this.socketHandler = new SocketHandler(address, port);
        this.threadExecutor = Executors.newSingleThreadExecutor();
        this.isStartingSemaphore = new Semaphore(1);
    }

    public boolean start() {
        if (socketHandler.isStarted) {
            throw new IllegalStateException("Client handler is already started !");
        }
        try {
            isStartingSemaphore.acquire();
            threadExecutor.submit(socketHandler);
            isStartingSemaphore.acquire();
            isStartingSemaphore.release();
            if (!socketHandler.isStarted) {
                if (socketHandler.connectionFailed) {
                    this.errorMessage = "Failed to connect to the server at " +  socketHandler.address + ":" + socketHandler.port;
                } else {
                    this.errorMessage = "Failed to start the handler";
                }
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void sendMessage(String content) {
        this.socketHandler.sendMessage(content);
    }

    public void login(String pseudo) {
        this.socketHandler.login(pseudo);
    }

    public void logout() {
        this.socketHandler.logout();
    }

    public void close() {
        socketHandler.close();
        threadExecutor.shutdown();
    }

    class SocketHandler implements Runnable {
        private final EventDispatcher dispatcher;

        private boolean isStarted;
        private boolean shouldClose;
        private boolean connectionFailed;

        private final int port;
        private final String address;
        private InputStream inputStream;
        private OutputStream outputStream;
        private Socket socket;

        public SocketHandler(String address, int port) {
            this.dispatcher = EventDispatcher.getInstance();

            this.port = port;
            this.address = address;
            this.isStarted = false;
            this.shouldClose = false;
            this.connectionFailed = false;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(address, port);
                this.inputStream = socket.getInputStream();
                this.outputStream = socket.getOutputStream();

                isStartingSemaphore.release();
                this.isStarted = true;

                while (!socket.isClosed()) {
                    final String serverMessage = this.read();
                    if (serverMessage == null) continue;

                    if (serverMessage.startsWith("ERROR")) {
                        handleError(serverMessage);
                    } else if (serverMessage.startsWith("USER")) {
                        handlerUserOperation(serverMessage);
                    } else if (serverMessage.startsWith(ChatamuProtocol.PREFIX_MESSAGE_FROM)) {
                        handleUserMessage(serverMessage);
                    } else if (serverMessage.equals(ChatamuProtocol.LOGIN_SUCCESS)) {
                        dispatcher.dispatchEvent(EventDispatcher.EventTypes.LOGIN_SUCCESS, null);
                    } else {
                        System.err.println("Received an unsupported server message !");
                        System.err.println("Server's message : "+serverMessage);
                    }
                }
            } catch (IOException ioe) {
                this.connectionFailed = ioe instanceof ConnectException;
                isStartingSemaphore.release();
                System.err.println(this.connectionFailed ? "Connection to server failed" : "I/O operation failed");
                ioe.printStackTrace();
            }
        }

        private void handleError(String serverMessage) {
            if (serverMessage.equals(ChatamuProtocol.Error.ERROR_LOGIN)) {
                dispatcher.dispatchEvent(EventDispatcher.EventTypes.LOGIN_FAIL, EventFactory.createErrorEvent("Login failed"));
            } else {
                System.err.println("Unknown error : " + serverMessage);
                dispatcher.dispatchEvent(EventDispatcher.EventTypes.ERROR, EventFactory.createErrorEvent(serverMessage));
            }
        }

        private void handlerUserOperation(String serverMessage) {
            final String[] split = serverMessage.split(" ", 2);
            if (split.length != 2 ) {
                System.err.println("Server sent an incomplete user operation");
                return;
            }
            Event e = new Event();
            e.pseudo = split[1];
            switch (split[0]) {
                case ChatamuProtocol.PREFIX_USER_CONNECTED:
                    dispatcher.dispatchEvent(EventDispatcher.EventTypes.USER_JOINED, e);
                    break;
                case ChatamuProtocol.PREFIX_USER_DISCONNECTED:
                    dispatcher.dispatchEvent(EventDispatcher.EventTypes.USER_LEAVED, e);
                    break;
                default:
                    System.err.println("Server sent an invalid user operation");
            }
        }

        private void handleUserMessage(String serverMessage) {
            final String[] split = serverMessage.split(" ", 3);
            if (split.length != 3 ) {
                System.err.println("Server sent an incomplete message");
                return;
            }
            final String pseudo = split[1];
            final String message = split[2];

            dispatcher.dispatchEvent(EventDispatcher.EventTypes.MESSAGE, EventFactory.createMessageEvent(pseudo,message));
        }

        /**
         * Read a message from the server
         * @return The message read or null on error/empty message
         */
        private String read() {
            try {
                byte[] buf = new byte[ChatamuProtocol.BUFFER_SIZE];
                int size = inputStream.read(buf);
                if (size <= 0) {
                    return null;
                } else {
                    return new String(buf).trim();
                }
            } catch (IOException ioe) {
                if (!socket.isClosed()) ioe.printStackTrace();
                return null;
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
                System.err.println("Core: Write operation failed.");
                ioe.printStackTrace();
            }
        }

        /**
         * Close the connection
         */
        private void close() {
            try {
                socket.getOutputStream().flush();
                socket.getInputStream().close();
                socket.close();
            } catch (IOException ioe) {
                System.err.println("Core: Closing connection failed.");
                ioe.printStackTrace();
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
