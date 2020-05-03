package protocol;

public final class ChatamuProtocol {
    public static final int DEFAULT_PORT = 12345;
    public static final int BUFFER_SIZE = 256;

    public static final class Error {
        public static final String ERROR_LOGIN     = "ERROR LOGIN aborting chatamu protocol.";
        public static final String ERROR_MESSAGE   = "ERROR chatamu.";
    }

    public static final String PREFIX_LOGIN    = "LOGIN";
    public static final String PREFIX_MESSAGE  = "MESSAGE";
    public static final String PREFIX_USER_CONNECTED  = "USER_CONNECTED";
    public static final String PREFIX_USER_DISCONNECTED  = "USER_DISCONNECTED";
    public static final String PREFIX_MESSAGE_FROM = "MESSAGE_FROM";

    public static final String LOGOUT_MESSAGE = "LOGOUT";
    public static final String LOGIN_SUCCESS = "OK LOGIN";

    public static String prefixContent(String prefix, String content) {
        return prefix + " " + content;
    }
}
