package main;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton to dispatch analytics update events to registered listeners.
 */
public class AnalyticsEventDispatcher {
    private static AnalyticsEventDispatcher instance;
    private final List<AnalyticsUpdateListener> listeners;

    private AnalyticsEventDispatcher() {
        listeners = new ArrayList<>();
    }

    public static AnalyticsEventDispatcher getInstance() {
        if (instance == null) {
            instance = new AnalyticsEventDispatcher();
        }
        return instance;
    }

    public void addListener(AnalyticsUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AnalyticsUpdateListener listener) {
        listeners.remove(listener);
    }

    public void fireEvent(AnalyticsUpdateEvent event) {
        for (AnalyticsUpdateListener listener : listeners) {
            listener.onAnalyticsUpdate(event);
        }
    }
}