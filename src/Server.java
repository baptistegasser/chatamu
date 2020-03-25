import protocol.ChatamuProtocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The server implementing the chatamu protocol
 */
public class Server {
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private InetSocketAddress socketAddress;
    private ClientHandler clientHandler = new ClientHandler();

    /**
     * Initialize the server.
     * @param address The address to bind on.
     * @param port The port to bind on.
     * @throws IOException Creation of sockets may fail.
     */
    public Server(String address, int port) throws IOException {
        // Create a selector
        selector = Selector.open();

        // Configure the server socket
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        // Store the address and port to bind to the socket later
        socketAddress = new InetSocketAddress(address, port);
    }

    /**
     * Launch the infinite server loop.
     * @throws IOException Operations on channel and socket may fail.
     * TODO remove global throws, one client might fail the whole server !!!
     */
    public void start() throws IOException {
        // Bind the socket to the address now.
        serverSocket.bind(this.socketAddress);

        // Infinite server loop
        while (true) {
            selector.select();

            // Iterate on the selected keys
            for (SelectionKey key : selector.selectedKeys()) {
                // Appropriate handle calls
                if (key.isAcceptable()) {
                    try {
                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } catch (IOException ioe) {
                        System.err.println("Failed to accept a client.");
                        ioe.printStackTrace();
                    }
                } else if (key.isReadable()) {
                    clientHandler.handleRead(key);
                }
            }
            // Remove the iterated keys
            selector.selectedKeys().clear();
        }
    }

    /**
     * Clean the server.
     */
    public void close() throws IOException {
        if (serverSocket.isOpen()) {
            serverSocket.close();
        }
        if (selector.isOpen()) {
            selector.close();
        }
    }

    /**
     * Launch a chatamu server instance
     */
    public static void main(String[] args) {
        try {
            // Set wanted logging level
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.ALL);

            Server server = new Server("localhost", ChatamuProtocol.DEFAULT_PORT);
            System.out.println("Le serveur est lancé sur le port " + ChatamuProtocol.DEFAULT_PORT);
            server.start();
            server.close();
        } catch (IOException ioe) {
            System.err.println("Server failure due to the following IOException:");
            ioe.printStackTrace();
        }
    }
}
