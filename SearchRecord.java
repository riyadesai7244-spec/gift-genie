import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Domain entity recording a user's recommendation query.
 * Used for isolated analytics and recent search history tracking.
 */
public class SearchRecord {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String userId;
    private final int age;
    private final String gender;
    private final String interest;
    private final double budget;
    private final int recommendationsFound;
    private final String timestamp;

    public SearchRecord(String userId, int age, String gender, String interest, double budget, int recommendationsFound) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.age = age;
        this.gender = Objects.requireNonNull(gender, "gender cannot be null");
        this.interest = Objects.requireNonNull(interest, "interest cannot be null");
        this.budget = budget;
        this.recommendationsFound = recommendationsFound;
        this.timestamp = LocalTime.now().format(TIME_FORMATTER);
    }

    public String getUserId() {
        return userId;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getInterest() {
        return interest;
    }

    public double getBudget() {
        return budget;
    }

    public int getRecommendationsFound() {
        return recommendationsFound;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getRedirectQuery() {
        return String.format("/?age=%d&gender=%s&interest=%s&budget=%d",
                age, gender, interest, (int) budget);
    }

    @Override
    public String toString() {
        return String.format("SearchRecord{user='%s', age=%d, gender='%s', interest='%s', budget=₹%.0f, time='%s'}",
                userId, age, gender, interest, budget, timestamp);
    }
}
