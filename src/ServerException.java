public class ServerException extends RuntimeException {
    public static enum Error {
        USER_NOT_CONNECTED,
        IO_OPERATION_FAILED,
    }

    private final Error error;

    public ServerException(Error error) {
        this.error = error;
    }

    public ServerException(Error error, Throwable cause) {
        super(cause);
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
