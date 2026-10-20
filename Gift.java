import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Domain entity representing a curated gift item in the catalog.
 * Follows standard JavaBean encapsulation principles.
 */
public class Gift {

    private final String name;
    private final String category;
    private final String gender;
    private final int minAge;
    private final int maxAge;
    private final double price;
    private final String occasion;
    private final String icon;
    private final String description;

    public Gift(String name, String category, String gender, int minAge, int maxAge, 
                double price, String occasion, String icon, String description) {
        this.name = Objects.requireNonNull(name, "Gift name cannot be null");
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.gender = Objects.requireNonNull(gender, "Gender cannot be null");
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.price = price;
        this.occasion = occasion;
        this.icon = icon;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getGender() {
        return gender;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public double getPrice() {
        return price;
    }

    public String getOccasion() {
        return occasion;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if this gift satisfies strict age boundaries.
     */
    public boolean isAgeAppropriate(int age) {
        return age >= minAge && age <= maxAge;
    }

    /**
     * Checks if this gift satisfies gender compatibility.
     */
    public boolean matchesGender(String targetGender) {
        if ("Any".equalsIgnoreCase(this.gender) || "Any".equalsIgnoreCase(targetGender)) {
            return true;
        }
        return this.gender.equalsIgnoreCase(targetGender);
    }

    /**
     * Returns formatted price in Indian currency (INR / ₹).
     */
    public String getFormattedPrice() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(price).replace("INR", "₹").replace(".00", "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gift)) return false;
        Gift gift = (Gift) o;
        return minAge == gift.minAge &&
               maxAge == gift.maxAge &&
               Double.compare(gift.price, price) == 0 &&
               Objects.equals(name, gift.name) &&
               Objects.equals(category, gift.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, minAge, maxAge, price);
    }

    @Override
    public String toString() {
        return String.format("Gift{name='%s', price=₹%.0f, ages=%d-%d, category='%s'}",
                name, price, minAge, maxAge, category);
    }
}
