import protocol.ChatamuProtocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * The server implementing the chatamu protocol
 */
public class Server {
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private InetSocketAddress socketAddress;

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

                // New connection
                if (key.isAcceptable()) {
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);

                }
                // Packet received
                else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();

                    ByteBuffer recvBuf = ByteBuffer.allocate(256);
                    client.read(recvBuf);
                    String msg = new String(recvBuf.array()).trim();

                    if (msg.startsWith(ChatamuProtocol.PREFIX_LOGIN)) {
                        System.out.println(msg.replace(ChatamuProtocol.PREFIX_LOGIN, "") + " connected !");
                        client.write(ByteBuffer.wrap(ChatamuProtocol.LOGIN_SUCCESS.getBytes()));
                    } else if (msg.startsWith(ChatamuProtocol.PREFIX_MESSAGE)) {
                        System.out.println("name> " + msg.replace(ChatamuProtocol.PREFIX_MESSAGE, ""));
                    } else if (msg.equals(ChatamuProtocol.LOGOUT_MESSAGE)) {
                        client.close();
                    } else {
                        System.err.println("Invalid message for protocol chatamu");
                        // Send error message, if fail close the client
                        try {
                            client.write(ByteBuffer.wrap(ChatamuProtocol.Error.ERROR_MESSAGE.getBytes()));
                        } catch (IOException ioe) {
                            client.close();
                        }
                    }
                }
            }

            // Remove the iterated keys
            keyIterator.remove();
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
            Server server = new Server("localhost", ChatamuProtocol.DEFAULT_PORT);
            server.start();
            server.close();
        } catch (IOException ioe) {
            System.err.println("Server failure due to the following IOException:");
            ioe.printStackTrace();
        }
    }
}
