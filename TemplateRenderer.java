import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Lightweight template renderer that merges dynamic application data with HTML templates.
 * Decouples presentation markup from Java business and controller logic.
 */
public class TemplateRenderer {

    private static final String TEMPLATE_DIR = "templates";

    /**
     * Loads raw HTML content from the templates directory with path fallback support.
     */
    private static String loadTemplate(String fileName) {
        try {
            Path path = Path.of(TEMPLATE_DIR, fileName);
            if (!Files.exists(path)) {
                path = Path.of(fileName);
            }
            if (!Files.exists(path)) {
                return "<html><body><h1>Error: Template " + fileName + " not found</h1></body></html>";
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "<html><body><h1>Error reading template: " + e.getMessage() + "</h1></body></html>";
        }
    }

    /**
     * Renders the authentication view (Sign In / Create Account tabs).
     */
    public static String renderAuth(String activeTab, String errorMessage, String prefilledUsername) {
        String template = loadTemplate("auth.html");
        boolean isSignup = "signup".equalsIgnoreCase(activeTab);

        String loginTabClass = !isSignup ? "tab-active" : "tab-inactive";
        String signupTabClass = isSignup ? "tab-active" : "tab-inactive";
        String loginDisplay = !isSignup ? "block" : "none";
        String signupDisplay = isSignup ? "block" : "none";

        String errorHtml = "";
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            errorHtml = "<div class='error-box'>" + escapeHtml(errorMessage) + "</div>";
        }

        String usernameVal = (prefilledUsername != null && !prefilledUsername.trim().isEmpty())
                ? escapeHtml(prefilledUsername) : "Riya";

        return template
                .replace("{{TAB_LOGIN_CLASS}}", loginTabClass)
                .replace("{{TAB_SIGNUP_CLASS}}", signupTabClass)
                .replace("{{FORM_LOGIN_DISPLAY}}", loginDisplay)
                .replace("{{FORM_SIGNUP_DISPLAY}}", signupDisplay)
                .replace("{{ERROR_MESSAGE_BLOCK}}", errorHtml)
                .replace("{{PREFILLED_USERNAME}}", usernameVal);
    }

    /**
     * Renders the main authenticated dashboard view.
     */
    public static String renderDashboard(String currentUser, List<Gift> results, boolean hasSearched,
                                         int age, String gender, String interest, double budget,
                                         List<SearchRecord> history) {
        String template = loadTemplate("dashboard.html");

        // Form selection states
        String selMale = "Male".equalsIgnoreCase(gender) ? "selected" : "";
        String selFemale = "Female".equalsIgnoreCase(gender) ? "selected" : "";
        String selAny = "Any".equalsIgnoreCase(gender) ? "selected" : "";

        String selWellness = "Wellness".equalsIgnoreCase(interest) ? "selected" : "";
        String selFashion = "Fashion".equalsIgnoreCase(interest) ? "selected" : "";
        String selTech = "Tech".equalsIgnoreCase(interest) ? "selected" : "";
        String selBooks = "Books".equalsIgnoreCase(interest) ? "selected" : "";
        String selHamper = "Hamper".equalsIgnoreCase(interest) ? "selected" : "";
        String selGaming = "Gaming".equalsIgnoreCase(interest) ? "selected" : "";

        // Recommendations HTML
        StringBuilder resultsBuilder = new StringBuilder();
        if (hasSearched && results != null && !results.isEmpty()) {
            resultsBuilder.append("<div class='results-summary'>");
            resultsBuilder.append("<div>Matching items for <b>")
                    .append(age).append(" yr old (").append(escapeHtml(gender)).append(")</b></div>");
            resultsBuilder.append("<span class='results-count'>").append(results.size()).append(" Items Found</span>");
            resultsBuilder.append("</div>");

            resultsBuilder.append("<div class='recommendations-list'>");
            for (Gift gift : results) {
                resultsBuilder.append(buildGiftCard(gift));
            }
            resultsBuilder.append("</div>");
        } else if (hasSearched) {
            resultsBuilder.append("<div style='padding:20px; text-align:center; color:#64748b; font-size:13px;'>")
                    .append("No items matched this exact search. Try adjusting the category or budget.")
                    .append("</div>");
        }

        // History rows HTML
        StringBuilder historyBuilder = new StringBuilder();
        if (history == null || history.isEmpty()) {
            historyBuilder.append("<tr><td colspan='6' style='text-align:center; color:#64748b; padding:16px;'>No searches logged yet. Submit a search above to see history.</td></tr>");
        } else {
            int limit = Math.min(6, history.size());
            for (int i = 0; i < limit; i++) {
                historyBuilder.append(buildHistoryRow(history.get(i)));
            }
        }

        return template
                .replace("{{CURRENT_USER}}", escapeHtml(currentUser))
                .replace("{{RECIPIENT_AGE}}", String.valueOf(age))
                .replace("{{SELECT_MALE}}", selMale)
                .replace("{{SELECT_FEMALE}}", selFemale)
                .replace("{{SELECT_ANY}}", selAny)
                .replace("{{SELECT_WELLNESS}}", selWellness)
                .replace("{{SELECT_FASHION}}", selFashion)
                .replace("{{SELECT_TECH}}", selTech)
                .replace("{{SELECT_BOOKS}}", selBooks)
                .replace("{{SELECT_HAMPER}}", selHamper)
                .replace("{{SELECT_GAMING}}", selGaming)
                .replace("{{BUDGET_VAL}}", String.valueOf((int) budget))
                .replace("{{RESULTS_SECTION}}", resultsBuilder.toString())
                .replace("{{HISTORY_ROWS}}", historyBuilder.toString());
    }

    private static String buildGiftCard(Gift gift) {
        return "<div class='item-card'>" +
                "<div class='item-details'>" +
                "<div class='item-name'>" + escapeHtml(gift.getName()) + "</div>" +
                "<div class='item-desc'>" + escapeHtml(gift.getDescription()) + "</div>" +
                "<div class='item-tags'>" +
                "<span class='tag'>Ages: " + gift.getMinAge() + "-" + gift.getMaxAge() + " yrs</span>" +
                "<span class='tag'>Category: " + escapeHtml(gift.getCategory()) + "</span>" +
                "</div>" +
                "</div>" +
                "<div class='item-pricing'>" +
                "<div class='item-price'>" + gift.getFormattedPrice() + "</div>" +
                "<div class='item-occasion'>" + escapeHtml(gift.getOccasion()) + "</div>" +
                "</div>" +
                "</div>";
    }

    private static String buildHistoryRow(SearchRecord record) {
        String url = record.getRedirectQuery();
        return "<tr class='clickable-row' onclick=\"window.location.href='" + url + "'\">" +
                "<td style='color:#64748b; font-size:12px;'>" + record.getTimestamp() + "</td>" +
                "<td><b>" + record.getAge() + " yrs</b></td>" +
                "<td>" + escapeHtml(record.getGender()) + "</td>" +
                "<td>" + escapeHtml(record.getInterest()) + "</td>" +
                "<td style='font-weight:600;'>₹" + String.format("%,.0f", record.getBudget()) + "</td>" +
                "<td><a href='" + url + "' class='action-link'>View Results →</a></td>" +
                "</tr>";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
