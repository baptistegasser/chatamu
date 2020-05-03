package client;

import java.util.*;

public class EventDispatcher {
    public enum EventTypes {
        ERROR,
        LOGIN_FAIL,
        LOGIN_SUCCESS,
        MESSAGE,
        USER_JOINED,
        USER_LEAVED,
    }

    private static EventDispatcher instance = null;
    private Map<EventTypes, Set<EventHandler>> eventToListeners;

    private EventDispatcher() {
        this.eventToListeners = Collections.synchronizedMap(new HashMap<>());
        for (EventTypes type : EventTypes.values()) {
            this.eventToListeners.put(type, new HashSet<>());
        }
    }

    public static EventDispatcher getInstance() {
        if (instance == null) {
            instance = new EventDispatcher();
        }
        return instance;
    }

    public void dispatchEvent(EventTypes type, Event event) {
        final Set<EventHandler> listeners = this.eventToListeners.get(type);
        if (listeners != null && listeners.size() > 0) {
            for (EventHandler listener : listeners) {
                try {
                    listener.handle(event);
                } catch (Exception e) {
                    System.err.println("A listener for event type '" + type + "' failed : ");
                    System.err.println(listener);
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean addListener(EventTypes type, EventHandler listener) {
        if (!this.eventToListeners.containsKey(type)) return false;
        return this.eventToListeners.get(type).add(listener);
    }

    public boolean addAllListener(EventTypes type, Collection<? extends EventHandler> listeners) {
        if (!this.eventToListeners.containsKey(type)) return false;
        return this.eventToListeners.get(type).addAll(listeners);
    }

    public boolean removeListener(EventTypes type, EventHandler listener) {
        if (!this.eventToListeners.containsKey(type)) return false;
        return this.eventToListeners.get(type).remove(listener);
    }

    public boolean removeAllListeners(EventTypes type, Collection<? extends EventHandler> listeners) {
        if (!this.eventToListeners.containsKey(type)) return false;
        return this.eventToListeners.get(type).removeAll(listeners);
    }
}
