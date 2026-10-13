import java.util.ArrayList;
import java.util.Scanner;

/**
 * ============================================================================
 * CONSOLE VERSION: Pure Beginner-Level Java (Scanner + ArrayList + If-Else)
 * Use this if your teacher or exam asks for a pure terminal/console demonstration!
 * ============================================================================
 */
public class ConsoleGiftApp {

    // Gift Class (OOP: Class & Object)
    static class Gift {
        String name;
        String category;
        String gender;
        int minAge;
        int maxAge;
        double price;

        public Gift(String name, String category, String gender, int minAge, int maxAge, double price) {
            this.name = name;
            this.category = category;
            this.gender = gender;
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.price = price;
        }
    }

    // Search Record Class (To store in database for analysis)
    static class SearchRecord {
        int age;
        String gender;
        String interest;
        double budget;
        int matches;

        public SearchRecord(int age, String gender, String interest, double budget, int matches) {
            this.age = age;
            this.gender = gender;
            this.interest = interest;
            this.budget = budget;
            this.matches = matches;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // In-memory Database of Gifts
        ArrayList<Gift> giftDatabase = new ArrayList<>();
        giftDatabase.add(new Gift("Wireless Earbuds", "Tech", "Any", 12, 60, 45.00));
        giftDatabase.add(new Gift("RGB Gaming Keyboard", "Gaming", "Any", 14, 50, 65.00));
        giftDatabase.add(new Gift("Handmade Leather Journal", "Books", "Any", 14, 80, 22.00));
        giftDatabase.add(new Gift("Classic Wrist Watch", "Fashion", "Male", 18, 70, 75.00));
        giftDatabase.add(new Gift("Fitness Activity Tracker", "Fitness", "Any", 12, 70, 49.00));
        giftDatabase.add(new Gift("Steel Water Bottle", "Fitness", "Any", 8, 80, 20.00));
        giftDatabase.add(new Gift("Silk Scarf", "Fashion", "Female", 16, 75, 38.00));

        // Database for Analytics
        ArrayList<SearchRecord> searchDatabase = new ArrayList<>();

        System.out.println("================================================");
        System.out.println(" 🎁 Basic Gift Recommendation & Analytics System");
        System.out.println("================================================");

        boolean running = true;
        while (running) {
            System.out.println("\nMENU:");
            System.out.println("1. Find Gift Recommendations");
            System.out.println("2. View Database Analytics");
            System.out.println("3. Exit");
            System.out.print("Enter choice (1-3): ");
            int choice = scanner.nextInt();

            // Control flow: switch statement
            switch (choice) {
                case 1:
                    // Take user input (Primitives & Strings)
                    System.out.print("Enter recipient age (e.g. 25): ");
                    int age = scanner.nextInt();

                    System.out.print("Enter gender (Male/Female/Any): ");
                    String gender = scanner.next();

                    System.out.print("Enter interest (Tech/Gaming/Books/Fashion/Fitness): ");
                    String interest = scanner.next();

                    System.out.print("Enter max budget ($): ");
                    double budget = scanner.nextDouble();

                    // Filter gifts using 'for' loop and 'if-else'
                    ArrayList<Gift> matches = new ArrayList<>();
                    for (Gift g : giftDatabase) {
                        boolean budgetOk = (g.price <= budget);
                        boolean ageOk = (age >= g.minAge && age <= g.maxAge);
                        boolean genderOk = g.gender.equalsIgnoreCase("Any") || g.gender.equalsIgnoreCase(gender);
                        boolean interestOk = g.category.equalsIgnoreCase(interest);

                        if (budgetOk && ageOk && genderOk && interestOk) {
                            matches.add(g);
                        }
                    }

                    // Save search query into Database
                    searchDatabase.add(new SearchRecord(age, gender, interest, budget, matches.size()));

                    // Display results
                    System.out.println("\n--- Recommended Gifts ---");
                    if (matches.isEmpty()) {
                        System.out.println("No matching gifts found within your budget and age criteria.");
                    } else {
                        for (Gift g : matches) {
                            System.out.println("• " + g.name + " - Price: $" + g.price + " (Ages: " + g.minAge + "-" + g.maxAge + ")");
                        }
                    }
                    break;

                case 2:
                    // Database Analytics
                    System.out.println("\n--- Database Analytics & Insights ---");
                    System.out.println("Total Searches Logged: " + searchDatabase.size());

                    if (searchDatabase.isEmpty()) {
                        System.out.println("No searches in database yet.");
                    } else {
                        // Calculate Average Budget (Accumulator loop)
                        double sum = 0;
                        for (SearchRecord s : searchDatabase) {
                            sum += s.budget;
                        }
                        double avgBudget = sum / searchDatabase.size();
                        System.out.printf("Average User Budget: $%.2f\n", avgBudget);

                        System.out.println("\nRecent Searches in Database:");
                        for (SearchRecord s : searchDatabase) {
                            System.out.println(" - Age: " + s.age + ", Gender: " + s.gender 
                                    + ", Interest: " + s.interest + ", Budget: $" + s.budget 
                                    + ", Matches: " + s.matches);
                        }
                    }
                    break;

                case 3:
                    running = false;
                    System.out.println("Exiting... Happy coding!");
                    break;

                default:
                    System.out.println("Invalid option! Please enter 1, 2, or 3.");
            }
        }

        scanner.close();
    }
}
