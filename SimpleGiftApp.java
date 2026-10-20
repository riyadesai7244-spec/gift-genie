import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main application controller and HTTP server entry point for GiftGenie.
 * Configures routes, authenticates requests, and delegates to service and presentation layers.
 */
public class SimpleGiftApp {

    private static final int PORT = 8080;
    private static final GiftService giftService = new GiftService();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new DashboardHandler());
        server.createContext("/login", new AuthHandler());
        server.createContext("/signup", exchange -> {
            exchange.getResponseHeaders().set("Location", "/login?tab=signup");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.createContext("/logout", exchange -> {
            exchange.getResponseHeaders().set("Set-Cookie", "gift_user=; Path=/; Max-Age=0");
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.start();
        System.out.println("==========================================================");
        System.out.println("  GiftGenie Web Server Started Successfully");
        System.out.println("  Local URL: http://localhost:" + PORT);
        System.out.println("==========================================================");
    }

    /**
     * Handler managing the main authenticated dashboard and recommendation queries.
     */
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"/".equals(exchange.getRequestURI().getPath())) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            String currentUser = getSessionUser(exchange);
            if (currentUser == null) {
                exchange.getResponseHeaders().set("Location", "/login");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            int age = 60;
            String gender = "Male";
            String interest = "Wellness";
            double budget = 2500.0;
            boolean hasSearched = false;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseParamString(body);

                try {
                    age = Integer.parseInt(params.getOrDefault("age", "60"));
                    gender = params.getOrDefault("gender", "Male");
                    interest = params.getOrDefault("interest", "Wellness");
                    budget = Double.parseDouble(params.getOrDefault("budget", "2500"));
                    hasSearched = true;

                    List<Gift> results = giftService.recommend(age, gender, interest, budget);
                    giftService.recordUserSearch(currentUser, age, gender, interest, budget, results.size());

                    String html = TemplateRenderer.renderDashboard(
                            currentUser, results, true, age, gender, interest, budget,
                            giftService.getUserHistory(currentUser)
                    );
                    sendHtmlResponse(exchange, 200, html);
                    return;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid numeric input in search parameters: " + e.getMessage());
                }
            } else {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && !query.trim().isEmpty()) {
                    Map<String, String> params = parseParamString(query);
                    try {
                        age = Integer.parseInt(params.getOrDefault("age", "60"));
                        gender = params.getOrDefault("gender", "Male");
                        interest = params.getOrDefault("interest", "Wellness");
                        budget = Double.parseDouble(params.getOrDefault("budget", "2500"));
                        hasSearched = true;
                    } catch (NumberFormatException ignored) {}
                } else {
                    hasSearched = true;
                }
            }

            List<Gift> results = giftService.recommend(age, gender, interest, budget);
            String html = TemplateRenderer.renderDashboard(
                    currentUser, results, hasSearched, age, gender, interest, budget,
                    giftService.getUserHistory(currentUser)
            );
            sendHtmlResponse(exchange, 200, html);
        }
    }

    /**
     * Handler managing user authentication (Sign In & Sign Up).
     */
    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseParamString(body);

                String action = params.getOrDefault("action", "login");
                String username = params.getOrDefault("username", "Riya").trim();
                String password = params.getOrDefault("password", "").trim();

                if (username.isEmpty()) username = "Riya";

                if ("signup".equalsIgnoreCase(action)) {
                    String confirmPassword = params.getOrDefault("confirm_password", "").trim();
                    if (!password.equals(confirmPassword)) {
                        String html = TemplateRenderer.renderAuth("signup", "Passwords do not match. Please re-enter.", username);
                        sendHtmlResponse(exchange, 200, html);
                        return;
                    }
                    if (giftService.userExists(username)) {
                        String html = TemplateRenderer.renderAuth("signup", "Account '" + username + "' already exists! Please sign in.", username);
                        sendHtmlResponse(exchange, 200, html);
                        return;
                    }
                    giftService.registerUser(username, password);
                } else {
                    if (!giftService.authenticateUser(username, password)) {
                        String html = TemplateRenderer.renderAuth("login", "Incorrect username or password. Please try again.", username);
                        sendHtmlResponse(exchange, 200, html);
                        return;
                    }
                }

                // Authenticated: set session cookie and redirect to dashboard
                exchange.getResponseHeaders().set("Set-Cookie", "gift_user=" + username + "; Path=/; Max-Age=86400; HttpOnly");
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            // GET request: show login or signup tab based on query param
            String query = exchange.getRequestURI().getQuery();
            String tab = (query != null && query.contains("tab=signup")) ? "signup" : "login";
            String html = TemplateRenderer.renderAuth(tab, null, "");
            sendHtmlResponse(exchange, 200, html);
        }
    }

    private static String getSessionUser(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] pair = cookie.trim().split("=");
                if (pair.length >= 2 && "gift_user".equals(pair[0])) {
                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    private static void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseParamString(String paramStr) {
        Map<String, String> map = new HashMap<>();
        if (paramStr == null || paramStr.trim().isEmpty()) {
            return map;
        }
        String[] pairs = paramStr.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            if (parts.length >= 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String val = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }
}
