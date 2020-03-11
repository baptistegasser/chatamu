package protocol;

public class ChatamuProtocol {
    public static final int DEFAULT_PORT = 12345;

    public static class Error {
        public static final String ERROR_LOGIN     = "ERROR LOGIN aborting chatamu protocol.";
        public static final String ERROR_MESSAGE   = "ERROR chatamu.";
    }

    public static final String PREFIX_LOGIN    = "LOGIN ";
    public static final String PREFIX_MESSAGE  = "MESSAGE ";
}
