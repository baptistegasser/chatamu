import protocol.ChatamuProtocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The server implementing the chatamu protocol
 */
public class Server {
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private InetSocketAddress socketAddress;
    private HashMap<SocketAddress, String> connectedUsers = new HashMap<>();
    private ArrayList<SocketChannel> clientsConnected = new ArrayList<>();

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
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                // Appropriate handle calls
                if (key.isAcceptable()) {
                    handleAcceptable(key);
                } else if (key.isReadable()) {
                    handleReadable(key);
                }
            }
            // Remove the iterated keys
            keyIterator.remove();
        }
    }

    /**
     * Handle a SelectionKey if it's acceptable
     * @param key
     */
    private void handleAcceptable(SelectionKey key) {
        try {
            SocketChannel client = serverSocket.accept();
            clientsConnected.add(client);

            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } catch (IOException ioe) {
            System.err.println("Failed to accept a client.");
            ioe.printStackTrace();
        }
    }

    private void handleReadable(SelectionKey key) {
        try {
            SocketChannel client = (SocketChannel) key.channel();
            SocketAddress client_addr = client.getRemoteAddress();

            // Read the from the socket channel
            ByteBuffer recvBuf = ByteBuffer.allocate(256);
            client.read(recvBuf);
            String msg = new String(recvBuf.array()).trim();

            if (msg.startsWith(ChatamuProtocol.PREFIX_LOGIN)) {
                final String pseudo = msg.replace(ChatamuProtocol.PREFIX_LOGIN, "");
                if (!isUserConnected(client_addr)) {
                    registerUser(client_addr, pseudo);
                    client.write(ByteBuffer.wrap(ChatamuProtocol.LOGIN_SUCCESS.getBytes()));
                    System.out.println(pseudo + " connected !");
                } else {
                    client.write(ByteBuffer.wrap(ChatamuProtocol.Error.ERROR_LOGIN.getBytes()));
                }
            }
            else if (msg.startsWith(ChatamuProtocol.PREFIX_MESSAGE))
            {
                if (isUserConnected(client_addr)) {
                    final String content = msg.replace(ChatamuProtocol.PREFIX_MESSAGE, "");
                    final String pseudo = getUserPseudo(client_addr);
                    String msgToSend = pseudo + "> " + content;
                    for(SocketChannel sa : clientsConnected) {
                        if(sa.getRemoteAddress() == client.getRemoteAddress()) continue;
                        sa.write(ByteBuffer.wrap(msgToSend.getBytes()));
                    }
                } else {
                    client.write(ByteBuffer.wrap(ChatamuProtocol.Error.ERROR_LOGIN.getBytes()));
                }
            }
            else if (msg.equals(ChatamuProtocol.LOGOUT_MESSAGE))
            {
                if (isUserConnected(client_addr)) {
                    unregisterUser(client_addr);
                    client.close();
                } else {
                    client.write(ByteBuffer.wrap(ChatamuProtocol.Error.ERROR_LOGIN.getBytes()));
                }
            }
            else {
                if (!isUserConnected(client_addr)) return;

                System.err.println("Invalid message for protocol chatamu");
                System.err.println("Got: '" + msg + "'");
                // Send error message, if fail close the client
                try {
                    client.write(ByteBuffer.wrap(ChatamuProtocol.Error.ERROR_MESSAGE.getBytes()));
                } catch (IOException ioe) {
                    client.close();
                }
            }
        } catch (IOException ioe) {
            System.err.println("Failed to handle read operation of a client.");
            ioe.printStackTrace();
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

    private String registerUser(SocketAddress addr, String pseudo) {
        return connectedUsers.put(addr, pseudo);
    }

    private String unregisterUser(SocketAddress addr) {
        return connectedUsers.remove(addr);
    }

    private boolean isUserConnected(SocketAddress addr) {
        return connectedUsers.containsKey(addr);
    }

    private String getUserPseudo(SocketAddress addr) {
        return connectedUsers.get(addr);
    }

    /**
     * Launch a chatamu server instance
     */
    public static void main(String[] args) {
        try {
            Server server = new Server("localhost", ChatamuProtocol.DEFAULT_PORT);
            server.start();
            server.close();
        } catch (IOException ioe) {
            System.err.println("Server failure due to the following IOException:");
            ioe.printStackTrace();
        }
    }
}
