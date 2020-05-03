package client;

import client.event.Event;
import client.event.EventDispatcher;

import static client.event.EventDispatcher.EventTypes.*;

public abstract class AbstractClient {
    protected final Core core;

    public AbstractClient(String address, int port) {
        this.core = new Core(address, port);

        EventDispatcher dispatcher = EventDispatcher.getInstance();
        dispatcher.addListener(ERROR,         this::onError);
        dispatcher.addListener(LOGIN_FAIL,    this::onLoginFail);
        dispatcher.addListener(LOGIN_SUCCESS, this::onLoginSuccess);
        dispatcher.addListener(MESSAGE,       this::onMessage);
        dispatcher.addListener(USER_JOINED,   this::onUserJoined);
        dispatcher.addListener(USER_LEAVED,   this::onUserLeaved);
    }

    public final void start() {
        core.start();
        this.launch();
    }

    protected abstract void launch();

    protected abstract void onError(Event event);

    protected abstract void onLoginFail(Event event);

    protected abstract void onLoginSuccess(Event event);

    protected abstract void onMessage(Event event);

    protected abstract void onUserJoined(Event event);

    protected abstract void onUserLeaved(Event event);
}
