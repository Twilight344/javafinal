package main;

/**
 * Event class to signal an update in analytics data (posts, likes, or comments).
 */
public class AnalyticsUpdateEvent {
    private final int userId;

    public AnalyticsUpdateEvent(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}