package main;

/**
 * Listener interface for analytics update events.
 */
public interface AnalyticsUpdateListener {
    void onAnalyticsUpdate(AnalyticsUpdateEvent event);
}