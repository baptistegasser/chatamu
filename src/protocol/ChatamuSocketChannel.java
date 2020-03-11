package protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChatamuSocketChannel {
    private final SocketChannel channel;

    public ChatamuSocketChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public static ChatamuSocketChannel wrap(SocketChannel channel) {
        return new ChatamuSocketChannel(channel);
    }

    public int login(String pseudo) throws IOException {
        final String msg = ChatamuProtocol.OP.LOGIN + " " + pseudo;
        return write(ByteBuffer.wrap(msg.getBytes()));
    }

    public int sendMessage(String message) throws IOException {
        final String msg = ChatamuProtocol.OP.MESSAGE + " " + message;
        return write(ByteBuffer.wrap(msg.getBytes()));
    }

    public int sendLoginError(String pseudo) throws IOException {
        final String msg = ChatamuProtocol.OP.ERROR_LOGIN + " aborting ChatAMU protocol.";
        return write(ByteBuffer.wrap(msg.getBytes()));
    }

    public int sendMessageError(String message) throws IOException {
        final String msg = ChatamuProtocol.OP.ERROR_MESSAGE + " ChatAMU.";
        return write(ByteBuffer.wrap(msg.getBytes()));
    }


    public int read(ByteBuffer byteBuffer) throws IOException {
        return channel.read(byteBuffer);
    }

    public int write(ByteBuffer byteBuffer) throws IOException {
        return channel.write(byteBuffer);
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    public void close() throws IOException {
        channel.close();
    }
}
