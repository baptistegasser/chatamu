package client;

public class EventFactory {
    static Event createErrorEvent(String errorMessage) {
        Event e = new Event();
        e.errorMessage = errorMessage;
        return e;
    }

    static Event createMessageEvent(String pseudo, String message) {
        Event e = new Event();
        e.pseudo = pseudo;
        e.message = message;
        return e;
    }
}
