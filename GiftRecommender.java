import java.util.List;

/**
 * Interface defining the business contract for generating personalized gift recommendations.
 * Implementations apply demographic filtering, preference scoring, and budget gating.
 */
public interface GiftRecommender {

    /**
     * Recommends a ranked list of gifts based on recipient parameters.
     *
     * @param userAge      Recipient age in years (used for strict developmental/elder appropriateness).
     * @param userGender   Recipient gender preference ("Male", "Female", or "Any").
     * @param userInterest Preferred product category or interest domain.
     * @param userBudget   Maximum allowed budget in Indian Rupees (₹).
     * @return List of matching, age-appropriate gift recommendations.
     */
    List<Gift> recommend(int userAge, String userGender, String userInterest, double userBudget);
}
