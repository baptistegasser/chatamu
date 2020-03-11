import protocol.ChatamuProtocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {
    private Selector selector;
    private ServerSocketChannel servSocket;
    private InetSocketAddress socketAddress;

    public Server(String address, int port) throws IOException {
        selector = Selector.open();

        servSocket = ServerSocketChannel.open();
        servSocket.configureBlocking(false);
        servSocket.register(selector, SelectionKey.OP_ACCEPT);

        socketAddress = new InetSocketAddress(address, port);
    }

    public void start() throws IOException {
        // If problem bind in the constructor
        servSocket.bind(this.socketAddress);

        while (true) {
            selector.select();

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    SocketChannel client = servSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);

                } else if (key.isReadable()) {
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
            keyIterator.remove();
        }
    }

    public void close() throws IOException {
        if (servSocket.isOpen()) {
            servSocket.close();
        }
        if (selector.isOpen()) {
            selector.close();
        }
    }

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
