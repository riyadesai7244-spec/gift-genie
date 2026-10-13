import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// ----------------------------------------------------------------------------
// INTERFACE 1: Contract for generating personalized gift suggestions
// ----------------------------------------------------------------------------
interface GiftRecommender {
    ArrayList<Gift> recommend(int userAge, String userGender, String userInterest, double userBudget);
}

// ----------------------------------------------------------------------------
// INTERFACE 2: Contract for personal activity tracking
// ----------------------------------------------------------------------------
interface UserAnalyticsTracker {
    double calculateUserAverageBudget(String userId);
    String findUserFavoriteInterest(String userId);
    ArrayList<SearchRecord> getUserHistory(String userId);
}

// ----------------------------------------------------------------------------
// Gift Entity
// ----------------------------------------------------------------------------
class Gift {
    String name;
    String category; // "Tech", "Books", "Gaming", "Fashion", "Fitness", "Wellness", "Hamper"
    String gender;   // "Male", "Female", "Any"
    int minAge;
    int maxAge;
    double price;    // in Indian Rupees (₹)
    String occasion; // Festive, Birthday, Health, Everyday
    String icon;
    String description;

    public Gift(String name, String category, String gender, int minAge, int maxAge, double price, String occasion, String icon, String description) {
        this.name = name;
        this.category = category;
        this.gender = gender;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.price = price;
        this.occasion = occasion;
        this.icon = icon;
        this.description = description;
    }
}

// ----------------------------------------------------------------------------
// Search Record Entity
// ----------------------------------------------------------------------------
class SearchRecord {
    String userId;
    int age;
    String gender;
    String interest;
    double budget;
    int recommendationsFound;
    String timestamp;

    public SearchRecord(String userId, int age, String gender, String interest, double budget, int recommendationsFound) {
        this.userId = userId;
        this.age = age;
        this.gender = gender;
        this.interest = interest;
        this.budget = budget;
        this.recommendationsFound = recommendationsFound;
        this.timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}

// ----------------------------------------------------------------------------
// Service Implementation (Catalog + Multi-User Isolated Vaults & Accounts)
// ----------------------------------------------------------------------------
class GiftService implements GiftRecommender, UserAnalyticsTracker {

    ArrayList<Gift> catalog = new ArrayList<>();
    Map<String, String> userCredentials = new HashMap<>();
    Map<String, ArrayList<SearchRecord>> userSearchVault = new HashMap<>();

    public GiftService() {
        seedIndianCatalog();

        // Default initial account: Riya
        userCredentials.put("Riya", "123456");
        userSearchVault.put("Riya", new ArrayList<>());
        userSearchVault.get("Riya").add(new SearchRecord("Riya", 60, "Male", "Wellness", 2500.0, 4));
        userSearchVault.get("Riya").add(new SearchRecord("Riya", 24, "Female", "Fashion", 2000.0, 4));
        userSearchVault.get("Riya").add(new SearchRecord("Riya", 10, "Any", "Books", 1000.0, 4));
    }

    public boolean userExists(String username) {
        if (username == null) return false;
        return userCredentials.containsKey(username.trim());
    }

    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) return false;
        String cleanUser = username.trim();
        if (userCredentials.containsKey(cleanUser)) return false;
        userCredentials.put(cleanUser, password != null ? password.trim() : "123456");
        userSearchVault.put(cleanUser, new ArrayList<>());
        return true;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) return false;
        String cleanUser = username.trim();
        if (!userCredentials.containsKey(cleanUser)) {
            // Self-register to enable effortless testing for new names
            userCredentials.put(cleanUser, password != null ? password.trim() : "123456");
            userSearchVault.putIfAbsent(cleanUser, new ArrayList<>());
            return true;
        }
        String stored = userCredentials.get(cleanUser);
        return stored.equals(password != null ? password.trim() : "");
    }

    private void seedIndianCatalog() {
        // Seniors & Elders (50 - 95 yrs) - e.g. 60yo Uncle / Aunty
        catalog.add(new Gift("Saregama Carvaan (Preloaded 5000 Classic Hindi Songs & Bhajans)", 
                "Tech", "Male", 50, 95, 2490.0, "Evergreen Classics", "📻", 
                "Pre-loaded golden-era songs by Kishore Kumar, Lata Mangeshkar, Rafi & Bhajans with stereo sound."));
        catalog.add(new Gift("Khadi Silk Handloom Nehru Jacket / Bandhgala Waistcoat", 
                "Fashion", "Male", 45, 95, 1499.0, "Traditional Wear", "🧥", 
                "Pure handloom silk-blend waistcoat, dignified and regal for pujas, family gatherings, and weddings."));
        catalog.add(new Gift("Omron Smart Digital Blood Pressure & Heart Monitor", 
                "Wellness", "Any", 45, 95, 1850.0, "Health Care", "🩺", 
                "Clinically validated one-touch automatic BP monitor with memory tracking and clear display."));
        catalog.add(new Gift("Pure Copper Ayurvedic Water Dispenser & Tumbler Set (5L)", 
                "Wellness", "Any", 40, 95, 1699.0, "Ayurvedic Health", "🫖", 
                "Traditional hammered pure copper storage promoting digestion, immunity, and overall well-being."));
        catalog.add(new Gift("Orthopedic Memory Foam Spine & Lumbar Support Back Cushion", 
                "Wellness", "Any", 45, 95, 999.0, "Back Comfort", "🛋️", 
                "Ergonomic contour relieving lower back pain and fatigue during sitting, prayer, or travel."));
        catalog.add(new Gift("Handcrafted Rosewood Walking Cane with Solid Brass Handle", 
                "Wellness", "Male", 55, 95, 899.0, "Thoughtful Care", "🦯", 
                "Sturdy seasoned Indian rosewood with slip-free rubber tip and royal polished brass grip."));
        catalog.add(new Gift("Shrimad Bhagavad Gita Deluxe Gold Foil Edition (Large Hindi Font)", 
                "Books", "Any", 40, 95, 999.0, "Spiritual Keepsake", "🕉️", 
                "Hardbound with gold-gilded edges, silk ribbon, and easy-to-read commentary."));
        catalog.add(new Gift("Electric Heated Knee & Joint Pain Relief Wrap", 
                "Wellness", "Any", 50, 95, 1299.0, "Joint Relief", "⚡", 
                "Therapeutic thermal heat soothing knee stiffness, arthritis, and joint fatigue."));
        catalog.add(new Gift("Titan Classic Gold Dial Analog Watch with Leather Strap", 
                "Fashion", "Male", 40, 95, 2495.0, "Timeless Watch", "⌚", 
                "Distinguished gold dial with Roman numerals and genuine brown leather strap."));
        catalog.add(new Gift("Pure Pashmina Kashmiri Embroidered Winter Shawl", 
                "Fashion", "Female", 40, 95, 1799.0, "Winter Warmth", "🧣", 
                "Handcrafted soft wool shawl featuring authentic Kashmiri Tilla needlework."));
        catalog.add(new Gift("Chanderi Handloom Silk Saree with Rich Zari Border", 
                "Fashion", "Female", 40, 90, 2199.0, "Festive & Puja", "🥻", 
                "Lightweight festive handloom saree with woven gold border and matching blouse piece."));
        catalog.add(new Gift("Saregama Carvaan Mini Bhakti Edition (Aartis & Amritwani)", 
                "Tech", "Female", 45, 95, 2190.0, "Daily Devotional Peace", "🪔", 
                "Dedicated spiritual audio player with morning aartis, bhajans, and sacred chants."));
        catalog.add(new Gift("Royal Afghan Dry Fruit Gift Box (Walnuts, Badam & Saffron)", 
                "Hamper", "Any", 40, 95, 1399.0, "Nutrition & Health", "🌰", 
                "Wooden gift box with premium Kashmiri walnuts, roasted cashews, and saffron."));

        // Middle-Aged Adults (35 - 54 yrs)
        catalog.add(new Gift("WildHorn Genuine RFID Leather Laptop Messenger Bag", 
                "Fashion", "Male", 25, 55, 1899.0, "Office & Travel", "💼", 
                "Executive full-grain leather briefcase with padded 15.6 inch laptop compartment."));
        catalog.add(new Gift("Philips Cordless Beard Trimmer & Grooming Kit Series 3000", 
                "Tech", "Male", 20, 55, 1499.0, "Daily Grooming", "✂️", 
                "Self-sharpening titanium blades with 20 length settings and 60min runtime."));
        catalog.add(new Gift("Ceramic Studio Handpainted 12-Piece Dinnerware Set", 
                "Hamper", "Any", 25, 65, 1850.0, "Dining & Home", "🍽️", 
                "Glazed artisanal ceramic plates and bowls for hosting family dinners."));
        catalog.add(new Gift("Deep Tissue Percussion Muscle Massage Gun (6 Heads)", 
                "Fitness", "Any", 20, 60, 2499.0, "Muscle Recovery", "💆", 
                "Targets sore neck, back, and leg muscles after long work hours or workouts."));
        catalog.add(new Gift("Forest Essentials Luxury Ayurvedic Facial Care Gift Box", 
                "Fashion", "Female", 22, 60, 2250.0, "Pampering & Glow", "🌸", 
                "Pure rose water mist, gentle herbal cleanser, and date & litchi moisturiser."));

        // Young Adults & Teens (13 - 35 yrs)
        catalog.add(new Gift("boAt Airdopes True Wireless Bluetooth Earbuds", 
                "Tech", "Any", 14, 40, 1299.0, "Daily Audio", "🎧", 
                "Quad Mics with ENx noise cancellation and 50 hours battery life."));
        catalog.add(new Gift("Noise ColorFit Ultra Smart Watch with Bluetooth Calling", 
                "Tech", "Any", 15, 45, 1899.0, "Fitness & Calls", "⌚", 
                "HD AMOLED display, heart rate, SpO2 tracker, and 100+ sports modes."));
        catalog.add(new Gift("Kundan Choker & Jhumka Traditional Jewellery Set", 
                "Fashion", "Female", 16, 45, 1199.0, "Festive Glam", "✨", 
                "Handcrafted Meenakari kundan necklace set with matching jhumkas."));
        catalog.add(new Gift("Logitech G102 Optical RGB Gaming Mouse (8000 DPI)", 
                "Gaming", "Any", 12, 30, 1299.0, "Gaming & PC", "🖱️", 
                "Tactile gaming-grade sensor with customizable RGB color lighting."));
        catalog.add(new Gift("Wireless Dual-Vibration Ergonomic Game Controller", 
                "Gaming", "Any", 12, 32, 1699.0, "Gamer Special", "🎮", 
                "Hall-effect analog joysticks with pressure-sensitive triggers."));

        // Kids (4 - 12 yrs)
        catalog.add(new Gift("Smartivity STEM DIY Hydraulic Crane & Mechanical Toy Kit", 
                "Gaming", "Any", 6, 12, 899.0, "Learning & Play", "🏗️", 
                "Eco-friendly wooden DIY kit teaching mechanics and hydraulics to kids."));
        catalog.add(new Gift("Panchatantra & Amar Chitra Katha Illustrated Stories (Box of 5)", 
                "Books", "Any", 5, 12, 799.0, "Moral Tales", "📚", 
                "Inspiring illustrated Indian folk stories fostering wisdom and heritage."));
    }

    @Override
    public ArrayList<Gift> recommend(int userAge, String userGender, String userInterest, double userBudget) {
        ArrayList<Gift> exactMatches = new ArrayList<>();
        ArrayList<Gift> ageAppropriateCategoryFallback = new ArrayList<>();
        ArrayList<Gift> ageAppropriateGeneralFallback = new ArrayList<>();

        for (Gift gift : catalog) {
            // STRICT AGE FILTER: Recipient must be within [minAge, maxAge]
            if (userAge < gift.minAge || userAge > gift.maxAge) {
                continue;
            }

            // Gender Filter
            boolean genderOk = gift.gender.equalsIgnoreCase("Any") 
                            || userGender.equalsIgnoreCase("Any") 
                            || gift.gender.equalsIgnoreCase(userGender);
            if (!genderOk) continue;

            boolean budgetOk = (gift.price <= userBudget);
            boolean categoryOk = gift.category.equalsIgnoreCase(userInterest);

            if (categoryOk && budgetOk) {
                exactMatches.add(gift);
            } else if (categoryOk && (gift.price <= userBudget * 1.25)) {
                ageAppropriateCategoryFallback.add(gift);
            } else if (budgetOk) {
                ageAppropriateGeneralFallback.add(gift);
            }
        }

        ArrayList<Gift> results = new ArrayList<>(exactMatches);
        if (results.size() < 4) {
            for (Gift g : ageAppropriateCategoryFallback) {
                if (!results.contains(g)) results.add(g);
                if (results.size() >= 4) break;
            }
        }
        if (results.size() < 4) {
            for (Gift g : ageAppropriateGeneralFallback) {
                if (!results.contains(g)) results.add(g);
                if (results.size() >= 4) break;
            }
        }
        return results;
    }

    public void recordUserSearch(String userId, int age, String gender, String interest, double budget, int matches) {
        userSearchVault.putIfAbsent(userId, new ArrayList<>());
        userSearchVault.get(userId).add(0, new SearchRecord(userId, age, gender, interest, budget, matches));
    }

    @Override
    public double calculateUserAverageBudget(String userId) {
        ArrayList<SearchRecord> records = userSearchVault.getOrDefault(userId, new ArrayList<>());
        if (records.isEmpty()) return 0.0;
        double total = 0.0;
        for (SearchRecord s : records) {
            total += s.budget;
        }
        return Math.round((total / records.size()) * 100.0) / 100.0;
    }

    @Override
    public String findUserFavoriteInterest(String userId) {
        ArrayList<SearchRecord> records = userSearchVault.getOrDefault(userId, new ArrayList<>());
        if (records.isEmpty()) return "None";
        HashMap<String, Integer> counts = new HashMap<>();
        for (SearchRecord s : records) {
            counts.put(s.interest, counts.getOrDefault(s.interest, 0) + 1);
        }
        String top = "Wellness";
        int max = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                top = e.getKey();
            }
        }
        return top;
    }

    @Override
    public ArrayList<SearchRecord> getUserHistory(String userId) {
        return userSearchVault.getOrDefault(userId, new ArrayList<>());
    }
}

// ----------------------------------------------------------------------------
// Web Server & Application Handlers
// ----------------------------------------------------------------------------
public class SimpleGiftApp {

    static GiftService service = new GiftService();

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MainAppHandler());
        server.createContext("/login", new AuthHandler());
        server.createContext("/signup", new SignupRedirectHandler());
        server.createContext("/logout", new LogoutHandler());
        server.start();

        System.out.println("==========================================================");
        System.out.println(" 🎁 GiftGenie – Server Running on http://localhost:" + port);
        System.out.println(" Full Login & Account Creation Available");
        System.out.println("==========================================================");
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Set-Cookie", "gift_user=; Path=/; Max-Age=0");
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    static class SignupRedirectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Location", "/login?tab=signup");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    // Handles both Sign In and Account Creation (Sign Up)
    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("POST".equalsIgnoreCase(method)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseParamString(body);
                String action = params.getOrDefault("action", "login");
                String username = params.getOrDefault("username", "Riya").trim();
                String password = params.getOrDefault("password", "123456").trim();

                if (username.isEmpty()) username = "Riya";

                if ("signup".equalsIgnoreCase(action)) {
                    String confirm = params.getOrDefault("confirm_password", password).trim();
                    if (!password.equals(confirm)) {
                        String html = buildAuthPage("signup", "Passwords do not match. Please re-enter.", username);
                        sendHtmlResponse(exchange, html);
                        return;
                    }
                    if (service.userExists(username)) {
                        String html = buildAuthPage("signup", "Account '" + username + "' already exists! Please sign in below.", username);
                        sendHtmlResponse(exchange, html);
                        return;
                    }
                    service.registerUser(username, password);
                } else {
                    // Login
                    if (!service.authenticate(username, password)) {
                        String html = buildAuthPage("login", "Incorrect username or password. Please try again.", username);
                        sendHtmlResponse(exchange, html);
                        return;
                    }
                }

                // Successful login or account creation: set cookie session & redirect
                exchange.getResponseHeaders().set("Set-Cookie", "gift_user=" + username + "; Path=/; Max-Age=86400");
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            // GET request: show auth page
            String query = exchange.getRequestURI().getQuery();
            String tab = "login";
            if (query != null && query.contains("tab=signup")) {
                tab = "signup";
            }
            String html = buildAuthPage(tab, null, "");
            sendHtmlResponse(exchange, html);
        }
    }

    static class MainAppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!path.equals("/")) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            String currentUser = getLoggedInUser(exchange);
            if (currentUser == null) {
                exchange.getResponseHeaders().set("Location", "/login");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            ArrayList<Gift> results = null;

            int lastAge = 60;
            String lastGender = "Male";
            String lastInterest = "Wellness";
            double lastBudget = 2500.0;
            boolean hasSearched = false;

            if ("POST".equalsIgnoreCase(method)) {
                String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseParamString(formData);

                try {
                    lastAge = Integer.parseInt(params.getOrDefault("age", "60"));
                    lastGender = params.getOrDefault("gender", "Male");
                    lastInterest = params.getOrDefault("interest", "Wellness");
                    lastBudget = Double.parseDouble(params.getOrDefault("budget", "2500"));

                    results = service.recommend(lastAge, lastGender, lastInterest, lastBudget);
                    hasSearched = true;

                    service.recordUserSearch(currentUser, lastAge, lastGender, lastInterest, lastBudget, results.size());
                } catch (Exception e) {
                    System.err.println("Search error: " + e.getMessage());
                }
            } else if (query != null && !query.isEmpty()) {
                Map<String, String> params = parseParamString(query);
                try {
                    lastAge = Integer.parseInt(params.getOrDefault("age", "60"));
                    lastGender = params.getOrDefault("gender", "Male");
                    lastInterest = params.getOrDefault("interest", "Wellness");
                    lastBudget = Double.parseDouble(params.getOrDefault("budget", "2500"));

                    results = service.recommend(lastAge, lastGender, lastInterest, lastBudget);
                    hasSearched = true;
                } catch (Exception e) {
                    System.err.println("Query parse error: " + e.getMessage());
                }
            } else {
                results = service.recommend(lastAge, lastGender, lastInterest, lastBudget);
                hasSearched = true;
            }

            String html = buildDashboardPage(currentUser, results, hasSearched, lastAge, lastGender, lastInterest, lastBudget);
            sendHtmlResponse(exchange, html);
        }
    }

    private static String getLoggedInUser(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String c : cookies) {
                String[] pair = c.trim().split("=");
                if (pair.length >= 2 && pair[0].equals("gift_user")) {
                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    private static void sendHtmlResponse(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static Map<String, String> parseParamString(String paramStr) {
        Map<String, String> map = new HashMap<>();
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

    // ------------------------------------------------------------------------
    // AUTH PAGE: Sign In & Create Account (Warm Champagne Cream Aesthetic)
    // ------------------------------------------------------------------------
    private static String buildAuthPage(String activeTab, String errorMessage, String prefilledUsername) {
        boolean isSignup = "signup".equalsIgnoreCase(activeTab);
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        sb.append("<title>Account Access – GiftGenie</title>");
        sb.append("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        sb.append("<link href='https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,600;0,700;1,600&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap' rel='stylesheet'>");
        sb.append("<style>");
        sb.append(":root { --bg-gradient: radial-gradient(circle at 50% 15%, #fbf5ea 0%, #fcf9f2 60%, #faf6ee 100%); --card: #ffffff; --border: #fae8cc; --text-dark: #221711; --text-muted: #786d63; --accent-amber: #c2781d; --accent-hover: #a86214; }");
        sb.append("body { font-family: 'Plus Jakarta Sans', sans-serif; margin: 0; padding: 40px 15px; background: var(--bg-gradient); color: var(--text-dark); display: flex; align-items: center; justify-content: center; min-height: 85vh; }");
        sb.append(".auth-card { max-width: 440px; width: 100%; background: var(--card); padding: 36px 32px; border-radius: 28px; box-shadow: 0 10px 35px -5px rgba(184,106,26,0.08); border: 1px solid var(--border); text-align: center; }");
        sb.append(".brand-icon { width: 50px; height: 50px; background: #c2781d; color: white; font-size: 24px; border-radius: 14px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 14px; box-shadow: 0 4px 12px rgba(194,120,29,0.22); }");
        sb.append(".brand-title { font-family: 'Playfair Display', serif; font-size: 26px; font-weight: 700; color: var(--text-dark); margin: 0; }");
        sb.append(".subtitle { font-size: 13px; color: var(--text-muted); margin-top: 6px; margin-bottom: 20px; line-height: 1.4; }");
        sb.append(".tab-bar { display: flex; background: #fdf6ec; border-radius: 999px; padding: 4px; border: 1px solid #f4dfbe; margin-bottom: 22px; }");
        sb.append(".tab-btn { flex: 1; padding: 9px 12px; border: none; border-radius: 999px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; text-decoration: none; display: inline-block; }");
        sb.append(".tab-active { background: #c2781d; color: white; box-shadow: 0 2px 6px rgba(194,120,29,0.25); }");
        sb.append(".tab-inactive { background: transparent; color: #8e5414; }");
        sb.append(".tab-inactive:hover { color: #5a3205; }");
        sb.append(".error-box { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; padding: 10px 14px; border-radius: 12px; font-size: 12.5px; font-weight: 600; margin-bottom: 18px; text-align: left; }");
        sb.append(".form-group { text-align: left; margin-bottom: 16px; }");
        sb.append("label { display: block; font-size: 11.5px; font-weight: 700; color: var(--text-dark); margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }");
        sb.append("input { width: 100%; padding: 11.5px 14px; border: 1.5px solid #f0e2d0; border-radius: 12px; font-size: 13.5px; box-sizing: border-box; background: #fffcf9; color: var(--text-dark); outline: none; }");
        sb.append("input:focus { border-color: var(--accent-amber); box-shadow: 0 0 0 3px rgba(194,120,29,0.12); }");
        sb.append(".btn-submit { width: 100%; background: var(--accent-amber); color: white; border: none; padding: 13px; font-size: 14px; font-weight: 700; border-radius: 999px; cursor: pointer; transition: background 0.15s; margin-top: 6px; box-shadow: 0 3px 10px rgba(194,120,29,0.2); }");
        sb.append(".btn-submit:hover { background: var(--accent-hover); }");
        sb.append(".quick-login { margin-top: 20px; padding-top: 18px; border-top: 1px solid #f6ecdc; }");
        sb.append(".btn-quick { background: #fdf6ec; color: var(--accent-amber); border: 1.5px solid #f5dfbe; padding: 9px 16px; border-radius: 999px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.15s; width: 100%; }");
        sb.append(".btn-quick:hover { background: #fbebd4; }");
        sb.append("</style></head><body>");

        sb.append("<div class='auth-card'>");
        sb.append("<div class='brand-icon'>🎁</div>");
        sb.append("<h1 class='brand-title'>GiftGenie <em>AI</em></h1>");
        sb.append("<div class='subtitle'>Curated Indian gift ideas tailored to your loved ones.</div>");

        // Dual Tabs (Sign In vs Create Account)
        sb.append("<div class='tab-bar'>");
        sb.append("<a href='/login?tab=login' class='tab-btn ").append(!isSignup ? "tab-active" : "tab-inactive").append("'>🔑 Sign In</a>");
        sb.append("<a href='/login?tab=signup' class='tab-btn ").append(isSignup ? "tab-active" : "tab-inactive").append("'>✨ Create Account</a>");
        sb.append("</div>");

        // Error message notification
        if (errorMessage != null && !errorMessage.isEmpty()) {
            sb.append("<div class='error-box'>⚠️ ").append(errorMessage).append("</div>");
        }

        if (!isSignup) {
            // SIGN IN FORM
            sb.append("<form method='POST' action='/login'>");
            sb.append("<input type='hidden' name='action' value='login'>");
            sb.append("<div class='form-group'>");
            sb.append("<label>Your Username</label>");
            sb.append("<input type='text' name='username' value='").append(prefilledUsername.isEmpty() ? "Riya" : prefilledUsername).append("' required placeholder='Enter your username'>");
            sb.append("</div>");
            sb.append("<div class='form-group'>");
            sb.append("<label>Password</label>");
            sb.append("<input type='password' name='password' value='123456' required placeholder='••••••'>");
            sb.append("</div>");
            sb.append("<button type='submit' class='btn-submit'>Sign In to Your Account →</button>");
            sb.append("</form>");

            sb.append("<div class='quick-login'>");
            sb.append("<form method='POST' action='/login'>");
            sb.append("<input type='hidden' name='action' value='login'>");
            sb.append("<input type='hidden' name='username' value='Riya'>");
            sb.append("<input type='hidden' name='password' value='123456'>");
            sb.append("<button type='submit' class='btn-quick'>⚡ One-Click Login as <b>Riya</b></button>");
            sb.append("</form>");
            sb.append("<div style='margin-top:12px; font-size:12px; color:var(--text-muted);'>");
            sb.append("New here? <a href='/login?tab=signup' style='color:var(--accent-amber); font-weight:700;'>Create an account</a>");
            sb.append("</div></div>");
        } else {
            // CREATE ACCOUNT (SIGN UP) FORM
            sb.append("<form method='POST' action='/login'>");
            sb.append("<input type='hidden' name='action' value='signup'>");
            sb.append("<div class='form-group'>");
            sb.append("<label>Create New Username</label>");
            sb.append("<input type='text' name='username' value='").append(prefilledUsername).append("' required placeholder='e.g. Aarav, Priya, Kabir'>");
            sb.append("</div>");
            sb.append("<div class='form-group'>");
            sb.append("<label>Create Password</label>");
            sb.append("<input type='password' name='password' required placeholder='Create a password'>");
            sb.append("</div>");
            sb.append("<div class='form-group'>");
            sb.append("<label>Confirm Password</label>");
            sb.append("<input type='password' name='confirm_password' required placeholder='Confirm password'>");
            sb.append("</div>");
            sb.append("<button type='submit' class='btn-submit'>✨ Create Account &amp; Start Gifting</button>");
            sb.append("</form>");

            sb.append("<div class='quick-login'>");
            sb.append("<div style='font-size:12px; color:var(--text-muted);'>");
            sb.append("Already have an account? <a href='/login?tab=login' style='color:var(--accent-amber); font-weight:700;'>Sign In here</a>");
            sb.append("</div></div>");
        }

        sb.append("</div></body></html>");
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // DASHBOARD PAGE: Hero Header + Direct Filter Wizard + Vertical Stack
    // ------------------------------------------------------------------------
    private static String buildDashboardPage(String currentUser, ArrayList<Gift> results, boolean hasSearched,
                                             int age, String gender, String interest, double budget) {
        StringBuilder sb = new StringBuilder();

        ArrayList<SearchRecord> userHistory = service.getUserHistory(currentUser);

        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        sb.append("<title>GiftGenie – Find the Perfect Gift in Seconds</title>");
        sb.append("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        sb.append("<link href='https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,600;0,700;0,800;1,600;1,700&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap' rel='stylesheet'>");
        sb.append("<style>");
        sb.append(":root { --bg-gradient: radial-gradient(circle at 50% 12%, #fbf5ea 0%, #fcf9f2 50%, #faf6ee 100%); --text-dark: #231815; --text-muted: #6b6158; --accent-amber: #c2781d; --accent-hover: #a66112; --border-warm: #fae8cc; --card: #ffffff; }");
        sb.append("body { font-family: 'Plus Jakarta Sans', sans-serif; margin: 0; padding: 0; background: var(--bg-gradient); color: var(--text-dark); min-height: 100vh; }");
        sb.append(".top-nav { max-width: 960px; margin: auto; padding: 22px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(243,227,203,0.7); }");
        sb.append(".logo { display: flex; align-items: center; gap: 10px; font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--text-dark); text-decoration: none; }");
        sb.append(".logo-icon { width: 34px; height: 34px; background: #c2781d; color: white; border-radius: 9px; display: flex; align-items: center; justify-content: center; font-size: 18px; box-shadow: 0 3px 8px rgba(194,120,29,0.25); }");
        sb.append(".logo span em { font-style: italic; color: var(--accent-amber); }");
        sb.append(".nav-actions { display: flex; align-items: center; gap: 14px; }");
        sb.append(".user-badge { display: inline-flex; align-items: center; gap: 6px; background: #fdf6ec; border: 1px solid #f5dfbe; padding: 5px 14px; border-radius: 999px; font-size: 12.5px; font-weight: 700; color: #7a480b; }");
        sb.append(".btn-pill { background: var(--accent-amber); color: white; border: none; padding: 8px 16px; border-radius: 999px; font-size: 12.5px; font-weight: 700; cursor: pointer; text-decoration: none; transition: background 0.15s; }");
        sb.append(".btn-pill:hover { background: var(--accent-hover); }");

        // Hero Section
        sb.append(".hero { max-width: 820px; margin: 35px auto 25px auto; text-align: center; padding: 0 20px; }");
        sb.append(".badge-pill { display: inline-flex; align-items: center; gap: 6px; background: #fef5e7; border: 1px solid #f8dfba; color: #a45e0d; padding: 6px 16px; border-radius: 999px; font-size: 12.5px; font-weight: 600; margin-bottom: 18px; }");
        sb.append(".hero-title { font-family: 'Playfair Display', serif; font-size: 48px; font-weight: 700; line-height: 1.18; color: var(--text-dark); margin: 0 0 14px 0; letter-spacing: -0.5px; }");
        sb.append(".hero-title em { font-style: italic; color: var(--accent-amber); font-weight: 700; }");
        sb.append(".hero-desc { font-size: 15px; color: var(--text-muted); max-width: 620px; margin: 0 auto; line-height: 1.6; }");

        // Guided Filter Box (Directly below hero)
        sb.append(".guided-box { max-width: 820px; margin: 28px auto 35px auto; background: white; border: 1px solid var(--border-warm); border-radius: 24px; padding: 24px 28px; box-shadow: 0 6px 25px -4px rgba(184,106,26,0.06); }");
        sb.append(".guided-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f6ecdc; padding-bottom: 14px; margin-bottom: 18px; }");
        sb.append(".guided-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 16px; margin-bottom: 18px; }");
        sb.append(".guided-grid label { display: block; font-size: 11.5px; font-weight: 700; color: var(--text-dark); margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }");
        sb.append(".guided-grid input, .guided-grid select { width: 100%; padding: 10px 12px; border: 1.5px solid #f2e2cf; border-radius: 12px; font-size: 13.5px; box-sizing: border-box; background: #fffcf9; color: var(--text-dark); outline: none; }");
        sb.append(".guided-grid input:focus, .guided-grid select:focus { border-color: var(--accent-amber); }");
        sb.append(".btn-search { width: 100%; background: var(--accent-amber); color: white; border: none; padding: 13px; font-size: 14px; font-weight: 700; border-radius: 12px; cursor: pointer; transition: background 0.15s; box-shadow: 0 3px 10px rgba(194,120,29,0.18); }");
        sb.append(".btn-search:hover { background: var(--accent-hover); }");

        // Recommendations Stack
        sb.append(".content-container { max-width: 820px; margin: auto; padding: 0 20px 40px 20px; }");
        sb.append(".results-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }");
        sb.append(".results-title { font-family: 'Playfair Display', serif; font-size: 22px; font-weight: 700; color: var(--text-dark); }");
        sb.append(".results-count { font-size: 12px; font-weight: 700; background: #fef5e7; color: var(--accent-amber); border: 1px solid #f6e0be; padding: 4px 12px; border-radius: 999px; }");
        sb.append(".recommendation-stack { display: flex; flex-direction: column; gap: 14px; margin-bottom: 45px; }");
        sb.append(".gift-row-card { display: flex; align-items: center; justify-content: space-between; background: white; border: 1.5px solid #f3e6d3; border-radius: 18px; padding: 18px 22px; transition: all 0.2s; box-shadow: 0 4px 16px rgba(184,106,26,0.04); }");
        sb.append(".gift-row-card:hover { border-color: #e5cdb0; box-shadow: 0 8px 25px rgba(184,106,26,0.08); transform: translateY(-2px); }");
        sb.append(".gift-left { display: flex; align-items: center; gap: 16px; flex: 1; min-width: 0; }");
        sb.append(".gift-icon { font-size: 28px; background: #fbf5eb; padding: 12px; border-radius: 14px; display: flex; align-items: center; justify-content: center; }");
        sb.append(".gift-info { display: flex; flex-direction: column; min-width: 0; }");
        sb.append(".gift-name { font-size: 16px; font-weight: 700; color: var(--text-dark); margin-bottom: 3px; }");
        sb.append(".gift-desc { font-size: 13px; color: var(--text-muted); margin-bottom: 5px; line-height: 1.4; }");
        sb.append(".gift-meta { font-size: 12px; color: #8e8074; font-weight: 500; }");
        sb.append(".gift-right { display: flex; flex-direction: column; align-items: flex-end; gap: 5px; min-width: 130px; text-align: right; }");
        sb.append(".gift-price { font-size: 20px; font-weight: 800; color: var(--accent-amber); }");
        sb.append(".gift-occasion-badge { font-size: 11px; font-weight: 700; background: #fbf2e3; color: #8d4f07; padding: 3px 10px; border-radius: 999px; }");

        // Bottom 3 Feature Highlights
        sb.append(".features-row { max-width: 820px; margin: 0 auto 50px auto; display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; text-align: center; }");
        sb.append(".feat-card { background: transparent; padding: 10px; }");
        sb.append(".feat-icon-wrap { width: 48px; height: 48px; background: white; border: 1.5px solid #f2e2cf; border-radius: 14px; margin: 0 auto 12px auto; display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: 0 4px 12px rgba(184,106,26,0.06); }");
        sb.append(".feat-title { font-family: 'Playfair Display', serif; font-size: 17px; font-weight: 700; color: var(--text-dark); margin-bottom: 5px; }");
        sb.append(".feat-desc { font-size: 12px; color: var(--text-muted); line-height: 1.5; }");

        // History Section
        sb.append(".history-card { background: white; border: 1.5px solid var(--border-warm); border-radius: 22px; padding: 24px; box-shadow: 0 6px 20px rgba(184,106,26,0.04); }");
        sb.append(".table-wrapper { border: 1px solid #f4e7d5; border-radius: 14px; overflow: hidden; margin-top: 12px; }");
        sb.append("table { width: 100%; border-collapse: collapse; font-size: 13px; }");
        sb.append("th, td { padding: 11px 14px; text-align: left; border-bottom: 1px solid #f6ecdc; }");
        sb.append("th { background: #fdfaf4; color: var(--text-muted); font-weight: 700; font-size: 12px; }");
        sb.append(".clickable-row { cursor: pointer; transition: background 0.15s; }");
        sb.append(".clickable-row:hover { background: #fef8f0 !important; }");
        sb.append(".reload-link { color: var(--accent-amber); font-weight: 700; text-decoration: none; font-size: 12px; }");
        sb.append("</style></head><body>");

        // Top Navigation
        sb.append("<div class='top-nav'>");
        sb.append("<a href='/' class='logo'>");
        sb.append("<div class='logo-icon'>🎁</div>");
        sb.append("<span>GiftGenie</span>");
        sb.append("</a>");
        sb.append("<div class='nav-actions'>");
        sb.append("<div class='user-badge'>👤 <b>").append(currentUser).append("</b></div>");
        sb.append("<a href='/login?tab=signup' class='nav-link' style='font-size:12.5px; font-weight:600; color:var(--text-dark); text-decoration:none;'>+ New Account</a>");
        sb.append("<a href='/logout' class='btn-pill'>Sign Out</a>");
        sb.append("</div>");
        sb.append("</div>");

        // Hero Section (Clean, without generate input)
        sb.append("<div class='hero'>");
        sb.append("<div class='badge-pill'>✨ Magical Gift Recommendations</div>");
        sb.append("<h1 class='hero-title'>Find the <em>Perfect Gift</em><br>in Seconds.</h1>");
        sb.append("<p class='hero-desc'>Select your recipient's age, gender, category, and budget in Indian Rupees (₹) to instantly discover thoughtful recommendations curated from top Indian stores.</p>");
        sb.append("</div>");

        // Guided Filter Box
        sb.append("<div class='content-container'>");
        sb.append("<div class='guided-box'>");
        sb.append("<div class='guided-header'>");
        sb.append("<div style='font-family:\"Playfair Display\", serif; font-size:18px; font-weight:700;'>⚙️ Recipient Preference Filters</div>");
        sb.append("<span style='font-size:11px; font-weight:700; color:#a45e0d; background:#fef5e7; border:1px solid #f8dfba; padding:3px 10px; border-radius:999px;'>Currency: Indian Rupee (₹)</span>");
        sb.append("</div>");

        sb.append("<form method='POST' action='/'>");
        sb.append("<div class='guided-grid'>");

        // Age
        sb.append("<div>");
        sb.append("<label>Age (Years)</label>");
        sb.append("<input type='number' name='age' min='1' max='95' value='").append(age).append("'>");
        sb.append("</div>");

        // Gender
        sb.append("<div>");
        sb.append("<label>Gender / Relation</label>");
        sb.append("<select name='gender'>");
        sb.append("<option value='Male' ").append(gender.equals("Male") ? "selected" : "").append(">Male (Uncle, Dad, Brother)</option>");
        sb.append("<option value='Female' ").append(gender.equals("Female") ? "selected" : "").append(">Female (Aunty, Mom, Sister)</option>");
        sb.append("<option value='Any' ").append(gender.equals("Any") ? "selected" : "").append(">Any / Unisex</option>");
        sb.append("</select>");
        sb.append("</div>");

        // Category
        sb.append("<div>");
        sb.append("<label>Category Focus</label>");
        sb.append("<select name='interest'>");
        sb.append("<option value='Wellness' ").append(interest.equals("Wellness") ? "selected" : "").append(">Health & Wellness 🩺</option>");
        sb.append("<option value='Fashion' ").append(interest.equals("Fashion") ? "selected" : "").append(">Traditional Wear 🧥</option>");
        sb.append("<option value='Tech' ").append(interest.equals("Tech") ? "selected" : "").append(">Audio & Tech 📻</option>");
        sb.append("<option value='Books' ").append(interest.equals("Books") ? "selected" : "").append(">Spiritual & Books 📚</option>");
        sb.append("<option value='Hamper' ").append(interest.equals("Hamper") ? "selected" : "").append(">Festive Hampers 🪔</option>");
        sb.append("<option value='Gaming' ").append(interest.equals("Gaming") ? "selected" : "").append(">Games & Play 🎯</option>");
        sb.append("</select>");
        sb.append("</div>");

        // Budget
        sb.append("<div>");
        sb.append("<label>Max Budget (₹ INR)</label>");
        sb.append("<input type='number' name='budget' min='200' max='50000' step='100' value='").append((int)budget).append("'>");
        sb.append("</div>");

        sb.append("</div>"); // end grid
        sb.append("<button type='submit' class='btn-search'>🔍 Recommend Curated Gifts</button>");
        sb.append("</form>");
        sb.append("</div>"); // end guided-box

        // Vertically Stacked Recommendations
        if (hasSearched && results != null && !results.isEmpty()) {
            sb.append("<div class='results-header'>");
            sb.append("<div class='results-title'>Handpicked Recommendations for ").append(age).append(" yr old (").append(gender).append(")</div>");
            sb.append("<div class='results-count'>").append(results.size()).append(" Curated Ideas</div>");
            sb.append("</div>");

            sb.append("<div class='recommendation-stack'>");
            for (Gift g : results) {
                sb.append("<div class='gift-row-card'>");
                sb.append("<div class='gift-left'>");
                sb.append("<div class='gift-icon'>").append(g.icon).append("</div>");
                sb.append("<div class='gift-info'>");
                sb.append("<div class='gift-name'>").append(g.name).append("</div>");
                sb.append("<div class='gift-desc'>").append(g.description).append("</div>");
                sb.append("<div class='gift-meta'>Ages: <b>").append(g.minAge).append("-").append(g.maxAge).append(" yrs</b> • Gender: <b>").append(g.gender).append("</b> • Category: ").append(g.category).append("</div>");
                sb.append("</div>");
                sb.append("</div>");
                sb.append("<div class='gift-right'>");
                sb.append("<div class='gift-price'>₹").append(String.format("%,.0f", g.price)).append("</div>");
                sb.append("<div class='gift-occasion-badge'>").append(g.occasion).append("</div>");
                sb.append("</div>");
                sb.append("</div>");
            }
            sb.append("</div>");
        }

        // Feature Highlights
        sb.append("<div class='features-row'>");
        sb.append("<div class='feat-card'>");
        sb.append("<div class='feat-icon-wrap'>🛍️</div>");
        sb.append("<div class='feat-title'>Multi-Store Search</div>");
        sb.append("<div class='feat-desc'>We scour Amazon India, Khadi, Nykaa, and specialty craft boutiques for authentic presents.</div>");
        sb.append("</div>");

        sb.append("<div class='feat-card'>");
        sb.append("<div class='feat-icon-wrap'>💖</div>");
        sb.append("<div class='feat-title'>Personalized Ranking</div>");
        sb.append("<div class='feat-desc'>Our demographic engine guarantees age-accurate gifts—seniors get Carvaans, not gaming mice.</div>");
        sb.append("</div>");

        sb.append("<div class='feat-card'>");
        sb.append("<div class='feat-icon-wrap'>🚚</div>");
        sb.append("<div class='feat-title'>Shipping &amp; Prices</div>");
        sb.append("<div class='feat-desc'>Get realistic Indian Rupee (₹) pricing and estimated delivery for upcoming celebrations.</div>");
        sb.append("</div>");
        sb.append("</div>");

        // Recent Searches for Current User
        sb.append("<div class='history-card'>");
        sb.append("<div style='font-family:\"Playfair Display\", serif; font-size:18px; font-weight:700; margin-bottom:6px;'>📋 Recent Searches for ").append(currentUser).append("</div>");
        sb.append("<div style='font-size:12.5px; color:var(--text-muted); margin-bottom:12px;'>Click any past search below to instantly reload its exact recommendations:</div>");

        sb.append("<div class='table-wrapper'>");
        sb.append("<table>");
        sb.append("<tr><th>Time</th><th>Recipient</th><th>Gender</th><th>Category</th><th>Max Budget</th><th>Action</th></tr>");
        if (userHistory.isEmpty()) {
            sb.append("<tr><td colspan='6' style='text-align:center; color:var(--text-muted); padding:16px;'>No searches logged yet for ").append(currentUser).append(". Try your first search above!</td></tr>");
        } else {
            int limit = Math.min(5, userHistory.size());
            for (int i = 0; i < limit; i++) {
                SearchRecord s = userHistory.get(i);
                String redirectUrl = "/?age=" + s.age + "&gender=" + s.gender + "&interest=" + s.interest + "&budget=" + (int)s.budget;
                sb.append("<tr class='clickable-row' onclick=\"window.location.href='").append(redirectUrl).append("'\">");
                sb.append("<td style='color:var(--text-muted); font-size:12px;'>").append(s.timestamp).append("</td>");
                sb.append("<td><b>").append(s.age).append(" yrs</b></td>");
                sb.append("<td>").append(s.gender).append("</td>");
                sb.append("<td>").append(s.interest).append("</td>");
                sb.append("<td style='font-weight:700; color:var(--accent-amber);'>₹").append(String.format("%,.0f", s.budget)).append("</td>");
                sb.append("<td><a href='").append(redirectUrl).append("' class='reload-link'>View Results →</a></td>");
                sb.append("</tr>");
            }
        }
        sb.append("</table>");
        sb.append("</div>");
        sb.append("</div>");

        sb.append("</div>"); // end content-container

        sb.append("</body></html>");
        return sb.toString();
    }
}
