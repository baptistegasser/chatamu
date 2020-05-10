import protocol.ChatamuProtocol;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Permet d'avoir un serveur central avec une architecture à base de file d'attente
 */
public class ClientHandler {

    // File d'attente
    private Queue<SocketChannel> connectedClientsSocket = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<SocketAddress, String> clientPseudoMap = new ConcurrentHashMap<>();

    private boolean isConnected(SocketChannel client) {
        return connectedClientsSocket.contains(client);
    }

    private String getClientPseudo(SocketAddress client_addr) {
        return clientPseudoMap.get(client_addr);
    }

    /**
     * Connect client
     * @param client Socket
     * @param pseudo
     */
    private void connectClient(SocketChannel client, String pseudo) throws IOException {
        connectedClientsSocket.add(client);
        clientPseudoMap.put(client.getRemoteAddress(), pseudo);
    }

    /**
     * Disconnect client using its socket
     * @param client Socket
     */
    private void disconnectClient(SocketChannel client) throws IOException {
        connectedClientsSocket.remove(client);
        clientPseudoMap.remove(client.getRemoteAddress());
    }

    public void handleRead (SelectionKey key) {
        new ReadHandler((SocketChannel) key.channel()).handle();
    }

    /**
     * Reading messages from client
     */
    private class ReadHandler {
        private final SocketChannel client;
        private final SocketAddress client_addr;
        private String client_instr;
        private String client_pseudo;

        public ReadHandler(SocketChannel socket) throws ServerException {
            try {
                // Store the client addr and socket
                this.client = socket;
                this.client_addr = client.getRemoteAddress();
            } catch (IOException ioe) {
                throw new ServerException(ServerException.Error.IO_OPERATION_FAILED, ioe);
            }
        }

        /**
         * Assert the user is connected or throw error
         * @param client The client's socket
         * @throws ServerException An exception if not connected
         */
        private boolean assertIsConnected(SocketChannel client) throws ServerException {
            if (isConnected(client)) return true;

            // Try to notify the client and then Throw a ServerException as assert failed
            try {
                client.write(ByteBuffer.wrap(ChatamuProtocol.Error.ERROR_LOGIN.getBytes()));
                return false;
            } catch (IOException ioe) {
                throw new ServerException(ServerException.Error.USER_NOT_CONNECTED, ioe);
            }
        }

        /**
         * Sending messages to all client but the current one
         * @param message The message
         */
        private void broadcastMessage(String message) {
            for (SocketChannel remoteClient : connectedClientsSocket) {
                // Don't send to current client
                if (remoteClient == client) continue;

                try {
                    remoteClient.write(ByteBuffer.wrap(message.getBytes()));
                } catch (IOException ioe) {
                    System.err.println("Failed sending broadcast message for client " + remoteClient);
                }
            }
        }

        /**
         * Handle message reception
         * Checking protocol for connexion and messages
         */
        public void handle() {
            try {
                // Read the from the socket channel
                ByteBuffer recvBuf = ByteBuffer.allocate(ChatamuProtocol.BUFFER_SIZE);
                client.read(recvBuf);
                client_instr = new String(recvBuf.array()).trim();


                final String[] split = client_instr.split(" ", 2);
                final String prefix = split[0];
                final String content = split.length > 1 ? split[1] : "";

                // First instruction to check: Client trying to loging
                if (prefix.equals(ChatamuProtocol.PREFIX_LOGIN)) {
                    this.client_pseudo = content;
                    handleLogin();
                } else {
                    // Other action should be sure that the user is connected
                    if(assertIsConnected(client))
                    {
                        // As we are connected, retrieve the pseudo
                        this.client_pseudo = getClientPseudo(client_addr);

                        if (prefix.equals(ChatamuProtocol.LOGOUT_MESSAGE)) {
                            handleLogout();
                        } else if (prefix.equals(ChatamuProtocol.PREFIX_MESSAGE)) {
                            handleMessage();
                        } else {
                            handleUnknownInstr();
                        }
                    }
                }
            } catch (IOException ioe) {
                throw new ServerException(ServerException.Error.IO_OPERATION_FAILED, ioe);
            }
        }

        /**
         * Handling a client logging in.
         */
        private void handleLogin() throws IOException {
            // Register the client as connected
            connectClient(client, this.client_pseudo);
            // Send a success to the client
            client.write(ByteBuffer.wrap(ChatamuProtocol.LOGIN_SUCCESS.getBytes()));

            // Broadcast to other client that an user connected
            final String msg = ChatamuProtocol.prefixContent(ChatamuProtocol.PREFIX_USER_CONNECTED, this.client_pseudo);
            broadcastMessage(msg);

            System.out.println(this.client_pseudo + " connected !"); // TODO better server output ?
        }

        /**
         * Handling a client logging out.
         */
        private void handleLogout() throws IOException {
            // Broadcast to other client that an user disconnected
            final String msg = ChatamuProtocol.prefixContent(ChatamuProtocol.PREFIX_USER_DISCONNECTED, this.client_pseudo);
            broadcastMessage(msg);
            // Unregister the client from connected lists
            disconnectClient(client);
            // Close the socket
            client.close();

            System.out.println(this.client_pseudo + " disconnected !"); // TODO better server output ?
        }

        /**
         * Handling a message reception.
         */
        private void handleMessage() {
            final String content = client_instr.substring(client_instr.indexOf(" ")+1);

            // Sending message to everyone
            broadcastMessage(ChatamuProtocol.PREFIX_MESSAGE_FROM + " "+ this.client_pseudo + " " + content);

            // Print the message received with the pseudo
            System.out.println(this.client_pseudo + "> " + content);
        }

        /**
         * Handling of unknown instructions sent by a client.
         */
        private void handleUnknownInstr() throws IOException {
            try {
                // Try to send an error message
                client.write(ByteBuffer.wrap(ChatamuProtocol.Error.ERROR_MESSAGE.getBytes()));
            } catch (IOException ioe) {
                // If it fail consider the client is dead and logout
                handleLogout();
            }
        }
    }
}
