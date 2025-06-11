package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Enhanced AI assistant for the Social Media Dashboard with sentiment analysis
 * and context-aware content suggestions.
 */
public class AIAssistant {
    private static final List<String> POST_SUGGESTIONS = new ArrayList<>();
    private static final List<String> COMMENT_SUGGESTIONS = new ArrayList<>();
    private static final List<String> GENERAL_RESPONSES = new ArrayList<>();
    private static final List<String> POSITIVE_WORDS = List.of("great", "awesome", "fantastic", "excited", "happy");
    private static final List<String> NEGATIVE_WORDS = List.of("sad", "disappointed", "sorry", "bad", "terrible");

    static {
        // Expanded post suggestions
        POST_SUGGESTIONS.add("Just finished a great book! Any recommendations for what to read next? #books");
        POST_SUGGESTIONS.add("Beautiful day outside today. Planning a hike this weekend! #nature");
        POST_SUGGESTIONS.add("Looking for recommendations on productivity apps. What's your favorite? #productivity");
        POST_SUGGESTIONS.add("Tried a new recipe today and it turned out amazing! Anyone want me to share it? #cooking");
        POST_SUGGESTIONS.add("Working on a new project. Will share updates soon! #work");
        POST_SUGGESTIONS.add("What's everyone watching these days? Need a new show to binge. #tvshows");
        POST_SUGGESTIONS.add("Just adopted a new pet! So excited about this new addition to the family. #pets");
        POST_SUGGESTIONS.add("Any travel recommendations for a weekend getaway? #travel");
        POST_SUGGESTIONS.add("Just learned a new skill! What's something new you've tried? #learning");
        POST_SUGGESTIONS.add("Feeling inspired today. What's motivating you? #motivation");

        // Expanded comment suggestions
        COMMENT_SUGGESTIONS.add("Great post! Thanks for sharing!");
        COMMENT_SUGGESTIONS.add("I totally agree with you on this one.");
        COMMENT_SUGGESTIONS.add("Interesting perspective. I hadn't thought about it that way.");
        COMMENT_SUGGESTIONS.add("Looking forward to hearing more about this!");
        COMMENT_SUGGESTIONS.add("This is really insightful, thanks for posting.");
        COMMENT_SUGGESTIONS.add("I had a similar experience recently.");
        COMMENT_SUGGESTIONS.add("Thanks for bringing this topic up!");
        COMMENT_SUGGESTIONS.add("Would love to discuss this more sometime.");
        COMMENT_SUGGESTIONS.add("Wow, that's impressive! Keep it up!");
        COMMENT_SUGGESTIONS.add("Sorry to hear that. Hope things get better!");

        // General responses
        GENERAL_RESPONSES.add("I can help you draft posts, suggest topics, or analyze engagement trends.");
        GENERAL_RESPONSES.add("Would you like me to suggest content ideas based on current trending topics?");
        GENERAL_RESPONSES.add("I notice your posts about technology get the most engagement. Would you like more topic ideas in this area?");
        GENERAL_RESPONSES.add("Your audience seems most active in the evenings. Would you like me to schedule your posts for optimal times?");
        GENERAL_RESPONSES.add("I can analyze the sentiment of your drafts to help optimize engagement.");
        GENERAL_RESPONSES.add("Would you like me to help you draft a response to recent comments?");
        GENERAL_RESPONSES.add("I can help you identify keywords to include in your posts for better visibility.");
        GENERAL_RESPONSES.add("Let me know if you'd like content suggestions tailored to your audience demographics.");
    }

    /**
     * Get a random post suggestion
     * @return A suggested post content
     */
    public static String getPostSuggestion() {
        return getRandomElement(POST_SUGGESTIONS);
    }

    /**
     * Get a random comment suggestion
     * @return A suggested comment content
     */
    public static String getCommentSuggestion() {
        return getRandomElement(COMMENT_SUGGESTIONS);
    }

    /**
     * Get a general response from the AI assistant
     * @return A general response or tip
     */
    public static String getGeneralResponse() {
        return getRandomElement(GENERAL_RESPONSES);
    }

    /**
     * Analyze sentiment of a text
     * @param text The text to analyze
     * @return "positive", "negative", or "neutral"
     */
    private static String analyzeSentiment(String text) {
        String lowerText = text.toLowerCase();
        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : POSITIVE_WORDS) {
            if (lowerText.contains(word)) positiveCount++;
        }
        for (String word : NEGATIVE_WORDS) {
            if (lowerText.contains(word)) negativeCount++;
        }

        if (positiveCount > negativeCount) return "positive";
        if (negativeCount > positiveCount) return "negative";
        return "neutral";
    }

    /**
     * Get a context-aware comment suggestion based on post content
     * @param postContent The content of the post
     * @return A tailored comment suggestion
     */
    public static String getContextAwareCommentSuggestion(String postContent) {
        String sentiment = analyzeSentiment(postContent);
        switch (sentiment) {
            case "positive":
                return getRandomElement(List.of(
                        "That's so exciting! Congrats!",
                        "Love the positive vibes! Keep it up!",
                        "This is awesome, thanks for sharing!"
                ));
            case "negative":
                return getRandomElement(List.of(
                        "Sorry to hear that. Hope things improve soon!",
                        "That sounds tough. Sending positive vibes!",
                        "Thanks for sharing, I'm here if you need to talk."
                ));
            default:
                return getCommentSuggestion();
        }
    }

    /**
     * Process a user query and provide a contextual response
     * @param query The user's query text
     * @return A response to the query
     */
    public static String processQuery(String query) {
        query = query.toLowerCase();

        if (query.contains("post") || query.contains("content") || query.contains("idea")) {
            return "Here's a content suggestion: " + getPostSuggestion();
        } else if (query.contains("comment") || query.contains("reply")) {
            return "Try this comment: " + getCommentSuggestion();
        } else if (query.contains("trend") || query.contains("popular")) {
            return "Currently trending topics include sustainability, work-life balance, and technology innovations. Would you like a post template on any of these?";
        } else if (query.contains("engagement") || query.contains("likes")) {
            return "To increase engagement, try asking open-ended questions, using relevant hashtags, and posting during peak hours (typically 7-9 AM and 5-7 PM).";
        } else {
            return getGeneralResponse();
        }
    }

    /**
     * Get a random element from a list
     * @param list The list to select from
     * @return A randomly selected element
     */
    private static String getRandomElement(List<String> list) {
        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }
}