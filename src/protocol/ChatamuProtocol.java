package protocol;

public class ChatamuProtocol {
    private static final int DEFAULT_PORT = 12345;

    public enum OP {
        LOGIN,
        ERROR_LOGIN,
        MESSAGE,
        ERROR_MESSAGE;

        @Override
        public String toString() {
            switch (this) {
                case LOGIN:
                    return "LOGIN";
                case MESSAGE:
                    return "MESSAGE";
                case ERROR_LOGIN:
                    return "ERROR_LOGIN";
                case ERROR_MESSAGE:
                    return "ERROR_MESSAGE";
                default:
                    return "";
            }
        }
    }
}
