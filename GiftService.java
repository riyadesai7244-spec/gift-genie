import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation fulfilling both recommendation and user analytics contracts.
 * Coordinates between GiftCatalog and UserRepository layers.
 */
public class GiftService implements GiftRecommender, UserAnalyticsTracker {

    private final GiftCatalog catalog;
    private final UserRepository userRepository;

    public GiftService(GiftCatalog catalog, UserRepository userRepository) {
        this.catalog = catalog;
        this.userRepository = userRepository;
    }

    public GiftService() {
        this(new GiftCatalog(), new UserRepository());
    }

    @Override
    public List<Gift> recommend(int userAge, String userGender, String userInterest, double userBudget) {
        List<Gift> exactMatches = new ArrayList<>();
        List<Gift> categoryFallback = new ArrayList<>();
        List<Gift> generalFallback = new ArrayList<>();

        for (Gift gift : catalog.getAllGifts()) {
            // Strict age bounds check: Ensures senior gifts are never substituted with youth gadgets
            if (!gift.isAgeAppropriate(userAge)) {
                continue;
            }

            // Gender compatibility check
            if (!gift.matchesGender(userGender)) {
                continue;
            }

            boolean budgetOk = (gift.getPrice() <= userBudget);
            boolean categoryOk = gift.getCategory().equalsIgnoreCase(userInterest);

            if (categoryOk && budgetOk) {
                exactMatches.add(gift);
            } else if (categoryOk && (gift.getPrice() <= userBudget * 1.25)) {
                categoryFallback.add(gift);
            } else if (budgetOk) {
                generalFallback.add(gift);
            }
        }

        List<Gift> results = new ArrayList<>(exactMatches);

        // Fill remaining slots up to 4 items using age-appropriate fallbacks
        if (results.size() < 4) {
            for (Gift g : categoryFallback) {
                if (!results.contains(g)) results.add(g);
                if (results.size() >= 4) break;
            }
        }
        if (results.size() < 4) {
            for (Gift g : generalFallback) {
                if (!results.contains(g)) results.add(g);
                if (results.size() >= 4) break;
            }
        }

        return results;
    }

    public void recordUserSearch(String userId, int age, String gender, String interest, double budget, int matches) {
        userRepository.recordSearch(userId, new SearchRecord(userId, age, gender, interest, budget, matches));
    }

    @Override
    public double calculateUserAverageBudget(String userId) {
        List<SearchRecord> records = userRepository.getHistory(userId);
        if (records.isEmpty()) return 0.0;

        double sum = 0.0;
        for (SearchRecord record : records) {
            sum += record.getBudget();
        }
        return Math.round((sum / records.size()) * 100.0) / 100.0;
    }

    @Override
    public String findUserFavoriteInterest(String userId) {
        List<SearchRecord> records = userRepository.getHistory(userId);
        if (records.isEmpty()) return "Wellness";

        Map<String, Integer> frequencyMap = new HashMap<>();
        for (SearchRecord r : records) {
            frequencyMap.put(r.getInterest(), frequencyMap.getOrDefault(r.getInterest(), 0) + 1);
        }

        String topInterest = "Wellness";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topInterest = entry.getKey();
            }
        }
        return topInterest;
    }

    @Override
    public List<SearchRecord> getUserHistory(String userId) {
        return userRepository.getHistory(userId);
    }

    public boolean registerUser(String username, String password) {
        return userRepository.register(username, password);
    }

    public boolean authenticateUser(String username, String password) {
        return userRepository.authenticate(username, password);
    }

    public boolean userExists(String username) {
        return userRepository.userExists(username);
    }
}
