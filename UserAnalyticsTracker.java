import java.util.List;

/**
 * Interface defining analytical capabilities for tracking and aggregating user activity.
 * Ensures data isolation across different user accounts.
 */
public interface UserAnalyticsTracker {

    /**
     * Calculates the historical average search budget for a given user.
     *
     * @param userId The unique user identifier.
     * @return Average search budget in INR (₹), or 0.0 if no search history exists.
     */
    double calculateUserAverageBudget(String userId);

    /**
     * Identifies the most frequently searched interest category for a user.
     *
     * @param userId The unique user identifier.
     * @return The user's top interest category name.
     */
    String findUserFavoriteInterest(String userId);

    /**
     * Retrieves the isolated chronological search history for a user.
     *
     * @param userId The unique user identifier.
     * @return List of previous search records belonging exclusively to this user.
     */
    List<SearchRecord> getUserHistory(String userId);
}
