package protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChatamuSocketChannel {
    private final SocketChannel channel;

    private ChatamuSocketChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public static ChatamuSocketChannel wrap(SocketChannel channel) {
        return new ChatamuSocketChannel(channel);
    }

    public int sendLoginError() throws IOException {
        return writeMessage(ChatamuProtocol.Error.ERROR_LOGIN);
    }

    public int sendMessageError() throws IOException {
        return writeMessage(ChatamuProtocol.Error.ERROR_MESSAGE);
    }

    public int writeMessage(String content) throws IOException {
        return write(ByteBuffer.wrap(content.getBytes()));
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
