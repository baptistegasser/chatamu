package client;

import client.event.Event;
import client.event.EventDispatcher;

import static client.event.EventDispatcher.EventTypes.*;
import static client.event.EventDispatcher.EventTypes.USER_LEAVED;

public interface IClient {
    /**
     *
     * @param client
     */
    static void registerClientListeners(IClient client) {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        dispatcher.addListener(ERROR,         client::onError);
        dispatcher.addListener(LOGIN_FAIL,    client::onLoginFail);
        dispatcher.addListener(LOGIN_SUCCESS, client::onLoginSuccess);
        dispatcher.addListener(MESSAGE,       client::onMessage);
        dispatcher.addListener(USER_JOINED,   client::onUserJoined);
        dispatcher.addListener(USER_LEAVED,   client::onUserLeaved);
    }

    void onError(Event event);

    void onLoginFail(Event event);

    void onLoginSuccess(Event event);

    void onMessage(Event event);

    void onUserJoined(Event event);

    void onUserLeaved(Event event);
}
