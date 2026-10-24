// ============================================================================
// GiftGenie - Simple Gift Recommendation Web Application
// Core Java & JDBC Database Connectivity (SQLite / Eclipse Compatible)
// ============================================================================

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ----------------------------------------------------------------------------
// 1. INTERFACE: Contract for Recommendation Business Logic
// ----------------------------------------------------------------------------
interface GiftRecommender {
    List<Gift> recommend(int age, String gender, String interest, double budget);
}

// ----------------------------------------------------------------------------
// 2. MODEL: Gift Entity (Encapsulated JavaBean)
// ----------------------------------------------------------------------------
class Gift {
    private final int id;
    private final String name;
    private final String category;
    private final String gender;
    private final int minAge;
    private final int maxAge;
    private final double price;
    private final String occasion;
    private final String description;

    public Gift(int id, String name, String category, String gender, int minAge, int maxAge, 
                double price, String occasion, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.gender = gender;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.price = price;
        this.occasion = occasion;
        this.description = description;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getGender() { return gender; }
    public int getMinAge() { return minAge; }
    public int getMaxAge() { return maxAge; }
    public double getPrice() { return price; }
    public String getOccasion() { return occasion; }
    public String getDescription() { return description; }
}

// ----------------------------------------------------------------------------
// 3. MODEL: Search Record Entity (For User Analytics & Query History)
// ----------------------------------------------------------------------------
class SearchRecord {
    private final String timestamp;
    private final int age;
    private final String gender;
    private final String interest;
    private final double budget;

    public SearchRecord(String timestamp, int age, String gender, String interest, double budget) {
        this.timestamp = timestamp;
        this.age = age;
        this.gender = gender;
        this.interest = interest;
        this.budget = budget;
    }

    public String getTimestamp() { return timestamp; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getInterest() { return interest; }
    public double getBudget() { return budget; }
}

// ----------------------------------------------------------------------------
// 4. DATABASE MANAGER: Pure JDBC Operations (No in-memory inventories)
// ----------------------------------------------------------------------------
class DatabaseManager implements GiftRecommender {

    private static final String DB_URL = "jdbc:sqlite:giftgenie.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[Database Warning] SQLite JDBC driver loading: " + e.getMessage());
        }
    }

    public DatabaseManager() {
        initDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Initializes database tables: users, search_history, and gifts.
     */
    private void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. Users Table (Authentication)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                );
            """);

            // 2. Search History Table (User Activity & Data Analytics)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS search_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    age INTEGER NOT NULL,
                    gender TEXT NOT NULL,
                    category TEXT NOT NULL,
                    budget REAL NOT NULL,
                    timestamp TEXT NOT NULL
                );
            """);

            // 3. Gifts Table (Data-Driven Gift Records - No In-Memory Inventories)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS gifts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    gender TEXT NOT NULL,
                    min_age INTEGER NOT NULL,
                    max_age INTEGER NOT NULL,
                    price REAL NOT NULL,
                    occasion TEXT NOT NULL,
                    description TEXT NOT NULL
                );
            """);

            // Seed initial records if tables are fresh
            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("[Database Error] Initialization failed: " + e.getMessage());
        }
    }

    private void seedInitialData(Connection conn) throws SQLException {
        // Seed default user 'Riya'
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO users (username, password) VALUES (?, ?)")) {
            pstmt.setString(1, "Riya");
            pstmt.setString(2, "123456");
            pstmt.executeUpdate();
        }

        // Seed initial gifts table if currently empty
        boolean giftsEmpty = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM gifts")) {
            if (rs.next() && rs.getInt(1) == 0) {
                giftsEmpty = true;
            }
        }
        if (giftsEmpty) {
            insertGift(conn, "Omron Smart Digital Blood Pressure Monitor", "Wellness", "Any", 45, 95, 1850, "Health Care", "Clinically validated automatic BP monitor with memory tracking and clear display.");
            insertGift(conn, "Pure Copper Ayurvedic Water Dispenser Set (5L)", "Wellness", "Any", 40, 95, 1699, "Ayurvedic Health", "Traditional hammered 100% pure copper vessel promoting digestion and natural immunity.");
            insertGift(conn, "Orthopedic Memory Foam Lumbar Support Cushion", "Wellness", "Any", 45, 95, 999, "Comfort Care", "Ergonomic contour relieving lower back pain during long sitting or prayer time.");
            insertGift(conn, "Handcrafted Rosewood Walking Cane with Brass Handle", "Wellness", "Male", 50, 95, 899, "Thoughtful Care", "Sturdy seasoned Indian rosewood with slip-free rubber tip and royal brass grip.");
            insertGift(conn, "Herbal Ayurvedic Pain Relief Thermal Compress Pack", "Wellness", "Any", 40, 95, 749, "Natural Comfort", "100% natural herbal heating compress for shoulder, neck, and joint relaxation.");
            insertGift(conn, "Saregama Carvaan (Preloaded 5000 Classic Hindi Songs)", "Tech", "Male", 45, 95, 2490, "Evergreen Classics", "Pre-loaded golden-era songs by Kishore Kumar, Lata Mangeshkar, and devotional bhajans.");
            insertGift(conn, "Khadi Silk Handloom Nehru Jacket / Waistcoat", "Fashion", "Male", 45, 95, 1499, "Traditional Wear", "Pure handloom silk-blend waistcoat, dignified and regal for pujas, family gatherings, and weddings.");
            insertGift(conn, "Shrimad Bhagavad Gita Deluxe Gold Foil Edition", "Books", "Any", 40, 95, 999, "Spiritual Keepsake", "Hardbound sacred text with gold-gilded edges and large Hindi font for comfortable reading.");
            insertGift(conn, "Pure Pashmina Kashmiri Embroidered Winter Shawl", "Fashion", "Female", 40, 95, 1799, "Winter Warmth", "Handcrafted soft Kashmiri wool shawl featuring authentic traditional Tilla needle embroidery.");
            insertGift(conn, "Chanderi Handloom Silk Saree with Zari Border", "Fashion", "Female", 40, 90, 2199, "Festive & Puja", "Lightweight festive handloom saree with woven gold border and matching blouse piece.");
            insertGift(conn, "WildHorn Genuine Leather Laptop Messenger Bag", "Fashion", "Male", 25, 55, 1899, "Office & Travel", "Executive full-grain leather briefcase with padded 15.6-inch laptop compartment.");
            insertGift(conn, "Philips Cordless Beard Trimmer Series 3000", "Tech", "Male", 20, 55, 1499, "Daily Grooming", "Self-sharpening titanium blades with 20 lock-in length settings.");
            insertGift(conn, "boAt Airdopes True Wireless Bluetooth Earbuds", "Tech", "Any", 14, 40, 1299, "Daily Audio", "Quad Mics with noise cancellation and 50 hours battery life.");
            insertGift(conn, "Kundan Choker & Jhumka Traditional Jewellery Set", "Fashion", "Female", 16, 45, 1199, "Festive Glam", "Handcrafted Meenakari kundan necklace set with matching jhumkas.");
            insertGift(conn, "Logitech G102 Optical RGB Gaming Mouse", "Gaming", "Any", 12, 30, 1299, "Gaming & PC", "Tactile gaming-grade sensor with customizable RGB color lighting.");
            insertGift(conn, "Wireless Dual-Vibration Ergonomic Game Controller", "Gaming", "Any", 12, 32, 1699, "Gamer Special", "Ergonomic gamepad with dual rumble motors and linear triggers.");
            insertGift(conn, "Smartivity STEM DIY Hydraulic Crane Toy Kit", "Gaming", "Any", 6, 12, 899, "Learning & Play", "Eco-friendly wooden DIY kit teaching mechanics and hydraulics to kids without screens.");
            insertGift(conn, "Panchatantra & Amar Chitra Katha Illustrated Stories", "Books", "Any", 5, 12, 799, "Moral Tales", "Inspiring illustrated Indian folk stories fostering wisdom and heritage.");
        }

        // Seed initial history for demo user Riya if empty
        boolean historyEmpty = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM search_history WHERE username = 'Riya'")) {
            if (rs.next() && rs.getInt(1) == 0) {
                historyEmpty = true;
            }
        }
        if (historyEmpty) {
            String sqlHistory = "INSERT INTO search_history (username, age, gender, category, budget, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlHistory)) {
                pstmt.setString(1, "Riya");
                pstmt.setInt(2, 60);
                pstmt.setString(3, "Male");
                pstmt.setString(4, "Wellness");
                pstmt.setDouble(5, 2500);
                pstmt.setString(6, "11:50:12");
                pstmt.executeUpdate();

                pstmt.setString(1, "Riya");
                pstmt.setInt(2, 24);
                pstmt.setString(3, "Female");
                pstmt.setString(4, "Fashion");
                pstmt.setDouble(5, 2000);
                pstmt.setString(6, "09:15:00");
                pstmt.executeUpdate();
            }
        }
    }

    private void insertGift(Connection conn, String name, String category, String gender, 
                            int minAge, int maxAge, double price, String occasion, String desc) throws SQLException {
        String sql = "INSERT INTO gifts (name, category, gender, min_age, max_age, price, occasion, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setString(3, gender);
            pstmt.setInt(4, minAge);
            pstmt.setInt(5, maxAge);
            pstmt.setDouble(6, price);
            pstmt.setString(7, occasion);
            pstmt.setString(8, desc);
            pstmt.executeUpdate();
        }
    }

    // ------------------------------------------------------------------------
    // DATABASE RECOMMENDATION QUERY (SQL-driven filtering with strict age rules)
    // ------------------------------------------------------------------------
    @Override
    public List<Gift> recommend(int age, String gender, String interest, double budget) {
        List<Gift> results = new ArrayList<>();

        // SQL Query 1: Exact category and budget match with strict age & gender rules
        String sqlExact = """
            SELECT id, name, category, gender, min_age, max_age, price, occasion, description 
            FROM gifts 
            WHERE min_age <= ? AND max_age >= ? 
              AND (LOWER(gender) = 'any' OR LOWER(gender) = LOWER(?) OR LOWER(?) = 'any')
              AND LOWER(category) = LOWER(?)
              AND price <= ?
            ORDER BY price DESC
        """;

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sqlExact)) {
            pstmt.setInt(1, age);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, gender);
            pstmt.setString(5, interest);
            pstmt.setDouble(6, budget);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapGift(rs));
                }
            }

            // SQL Query 2: Fallback query if under 4 results (Strict age bounds preserved)
            if (results.size() < 4) {
                String sqlFallback = """
                    SELECT id, name, category, gender, min_age, max_age, price, occasion, description 
                    FROM gifts 
                    WHERE min_age <= ? AND max_age >= ? 
                      AND (LOWER(gender) = 'any' OR LOWER(gender) = LOWER(?) OR LOWER(?) = 'any')
                      AND price <= ?
                    ORDER BY price DESC
                """;
                try (PreparedStatement fallbackStmt = conn.prepareStatement(sqlFallback)) {
                    fallbackStmt.setInt(1, age);
                    fallbackStmt.setInt(2, age);
                    fallbackStmt.setString(3, gender);
                    fallbackStmt.setString(4, gender);
                    fallbackStmt.setDouble(5, budget * 1.25);

                    try (ResultSet rsFallback = fallbackStmt.executeQuery()) {
                        while (rsFallback.next() && results.size() < 4) {
                            Gift g = mapGift(rsFallback);
                            if (results.stream().noneMatch(existing -> existing.getId() == g.getId())) {
                                results.add(g);
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[Database Error] recommend query failed: " + e.getMessage());
        }

        return results;
    }

    private Gift mapGift(ResultSet rs) throws SQLException {
        return new Gift(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("gender"),
            rs.getInt("min_age"),
            rs.getInt("max_age"),
            rs.getDouble("price"),
            rs.getString("occasion"),
            rs.getString("description")
        );
    }

    // ------------------------------------------------------------------------
    // DATABASE AUTHENTICATION & USER MANAGEMENT
    // ------------------------------------------------------------------------
    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setString(2, password.trim());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Username already exists or SQL error
            return false;
        }
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        String sql = "SELECT password FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password").equals(password.trim());
                } else {
                    // Auto-register for seamless testing convenience
                    registerUser(username, password);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] authenticate failed: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // DATABASE SEARCH HISTORY & ANALYTICS
    // ------------------------------------------------------------------------
    public void recordSearch(String username, int age, String gender, String interest, double budget) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String sql = "INSERT INTO search_history (username, age, gender, category, budget, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, interest);
            pstmt.setDouble(5, budget);
            pstmt.setString(6, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database Error] recordSearch failed: " + e.getMessage());
        }
    }

    public List<SearchRecord> getUserHistory(String username) {
        List<SearchRecord> list = new ArrayList<>();
        String sql = "SELECT timestamp, age, gender, category, budget FROM search_history WHERE LOWER(username) = LOWER(?) ORDER BY id DESC LIMIT 6";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new SearchRecord(
                        rs.getString("timestamp"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("category"),
                        rs.getDouble("budget")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] getUserHistory failed: " + e.getMessage());
        }
        return list;
    }
}

// ----------------------------------------------------------------------------
// 5. MAIN CONTROLLER & HTTP SERVER (Routes: /, /login, /logout)
// ----------------------------------------------------------------------------
public class SimpleGiftApp {

    private static final int PORT = 8080;
    private static final DatabaseManager dbManager = new DatabaseManager();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new DashboardHandler());
        server.createContext("/login", new AuthHandler());
        server.createContext("/logout", exchange -> {
            exchange.getResponseHeaders().set("Set-Cookie", "gift_user=; Path=/; Max-Age=0");
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        server.start();
        System.out.println("==========================================================");
        System.out.println("  GiftGenie Started with JDBC Database Connectivity");
        System.out.println("  Database File: giftgenie.db (SQLite)");
        System.out.println("  Local URL:     http://localhost:" + PORT);
        System.out.println("==========================================================");
    }

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

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                try {
                    age = Integer.parseInt(params.getOrDefault("age", "60"));
                    gender = params.getOrDefault("gender", "Male");
                    interest = params.getOrDefault("interest", "Wellness");
                    budget = Double.parseDouble(params.getOrDefault("budget", "2500"));
                    dbManager.recordSearch(currentUser, age, gender, interest, budget);
                } catch (NumberFormatException ignored) {}
            } else {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && !query.isEmpty()) {
                    Map<String, String> params = parseFormData(query);
                    try {
                        age = Integer.parseInt(params.getOrDefault("age", "60"));
                        gender = params.getOrDefault("gender", "Male");
                        interest = params.getOrDefault("interest", "Wellness");
                        budget = Double.parseDouble(params.getOrDefault("budget", "2500"));
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Query recommendations and user history from database
            List<Gift> results = dbManager.recommend(age, gender, interest, budget);
            List<SearchRecord> history = dbManager.getUserHistory(currentUser);

            String html = renderDashboardHtml(currentUser, results, age, gender, interest, budget, history);
            sendHtml(exchange, 200, html);
        }
    }

    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String errorMsg = null;
            String tab = "login";

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(body);
                String action = params.getOrDefault("action", "login");
                String username = params.getOrDefault("username", "").trim();
                String password = params.getOrDefault("password", "").trim();

                if (username.isEmpty()) username = "Riya";

                if ("signup".equalsIgnoreCase(action)) {
                    tab = "signup";
                    String confirm = params.getOrDefault("confirm_password", "").trim();
                    if (!password.equals(confirm)) {
                        errorMsg = "Passwords do not match.";
                    } else if (!dbManager.registerUser(username, password)) {
                        errorMsg = "Account '" + username + "' already exists. Please sign in.";
                    } else {
                        setSessionAndRedirect(exchange, username);
                        return;
                    }
                } else {
                    if (dbManager.authenticate(username, password)) {
                        setSessionAndRedirect(exchange, username);
                        return;
                    } else {
                        errorMsg = "Incorrect username or password.";
                    }
                }
            }

            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("tab=signup")) tab = "signup";

            String html = renderAuthHtml(tab, errorMsg);
            sendHtml(exchange, 200, html);
        }

        private void setSessionAndRedirect(HttpExchange exchange, String username) throws IOException {
            exchange.getResponseHeaders().set("Set-Cookie", "gift_user=" + username + "; Path=/; Max-Age=86400");
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    // ------------------------------------------------------------------------
    // SIMPLE, CLEAN HTML PRESENTATION (No Gimmicks, Standard Web Layout)
    // ------------------------------------------------------------------------
    private static String renderAuthHtml(String activeTab, String errorMsg) {
        boolean isSignup = "signup".equalsIgnoreCase(activeTab);
        String errorBlock = (errorMsg != null) ? "<div class='error-box'>" + escapeHtml(errorMsg) + "</div>" : "";

        String html = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>GiftGenie – Sign In</title>
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #f8fafc; color: #0f172a; display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 16px; }
            .card { width: 100%; max-width: 380px; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 28px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
            .title { font-size: 22px; font-weight: 700; text-align: center; margin-bottom: 4px; }
            .subtitle { font-size: 13px; color: #64748b; text-align: center; margin-bottom: 20px; }
            .tabs { display: flex; border-bottom: 1px solid #e2e8f0; margin-bottom: 18px; }
            .tab { flex: 1; text-align: center; padding: 8px 0; font-size: 13px; font-weight: 600; text-decoration: none; color: #64748b; border-bottom: 2px solid transparent; }
            .tab.active { color: #2563eb; border-bottom-color: #2563eb; }
            .field { margin-bottom: 14px; }
            label { display: block; font-size: 12px; font-weight: 600; color: #475569; margin-bottom: 4px; }
            input { width: 100%; padding: 8px 10px; font-size: 13px; border: 1px solid #cbd5e1; border-radius: 6px; outline: none; }
            input:focus { border-color: #2563eb; }
            .btn { width: 100%; padding: 10px; background: #2563eb; color: #fff; font-size: 13px; font-weight: 600; border: none; border-radius: 6px; cursor: pointer; margin-top: 6px; }
            .btn:hover { background: #1d4ed8; }
            .btn-demo { width: 100%; padding: 8px; background: #f1f5f9; color: #334155; font-size: 12px; font-weight: 600; border: 1px solid #cbd5e1; border-radius: 6px; cursor: pointer; margin-top: 14px; }
            .btn-demo:hover { background: #e2e8f0; }
            .error-box { background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; font-size: 12px; padding: 8px 10px; border-radius: 6px; margin-bottom: 14px; }
          </style>
        </head>
        <body>
          <div class="card">
            <h1 class="title">GiftGenie</h1>
            <p class="subtitle">Gift Recommendation &amp; Database Analytics</p>
            <div class="tabs">
              <a href="/login?tab=login" class="tab {{TAB_LOGIN}}">Sign In</a>
              <a href="/login?tab=signup" class="tab {{TAB_SIGNUP}}">Create Account</a>
            </div>
            {{ERROR_BLOCK}}
            <form method="POST" action="/login">
              <input type="hidden" name="action" value="{{ACTION}}">
              <div class="field">
                <label>Username</label>
                <input type="text" name="username" value="{{USERNAME}}" required placeholder="Enter username">
              </div>
              <div class="field">
                <label>Password</label>
                <input type="password" name="password" value="{{PASSWORD}}" required placeholder="Enter password">
              </div>
              {{CONFIRM_FIELD}}
              <button type="submit" class="btn">{{SUBMIT_TEXT}}</button>
            </form>
            {{DEMO_BLOCK}}
          </div>
        </body>
        </html>
        """;

        String demoBlock = !isSignup ? """
          <form method="POST" action="/login">
            <input type="hidden" name="action" value="login">
            <input type="hidden" name="username" value="Riya">
            <input type="hidden" name="password" value="123456">
            <button type="submit" class="btn-demo">One-Click Demo Login as <b>Riya</b></button>
          </form>
        """ : "";

        String confirmField = isSignup ? "<div class='field'><label>Confirm Password</label><input type='password' name='confirm_password' required placeholder='Confirm password'></div>" : "";

        return html
            .replace("{{TAB_LOGIN}}", !isSignup ? "active" : "")
            .replace("{{TAB_SIGNUP}}", isSignup ? "active" : "")
            .replace("{{ERROR_BLOCK}}", errorBlock)
            .replace("{{ACTION}}", isSignup ? "signup" : "login")
            .replace("{{USERNAME}}", !isSignup ? "Riya" : "")
            .replace("{{PASSWORD}}", !isSignup ? "123456" : "")
            .replace("{{CONFIRM_FIELD}}", confirmField)
            .replace("{{SUBMIT_TEXT}}", isSignup ? "Create Account" : "Sign In")
            .replace("{{DEMO_BLOCK}}", demoBlock);
    }

    private static String renderDashboardHtml(String user, List<Gift> results, int age, String gender, 
                                             String interest, double budget, List<SearchRecord> history) {
        StringBuilder itemsHtml = new StringBuilder();
        for (Gift g : results) {
            itemsHtml.append("<div class='item-card'>")
                     .append("<div class='item-details'>")
                     .append("<div class='item-name'>").append(escapeHtml(g.getName())).append("</div>")
                     .append("<div class='item-desc'>").append(escapeHtml(g.getDescription())).append("</div>")
                     .append("<div class='item-tags'>")
                     .append("<span class='tag'>Ages: ").append(g.getMinAge()).append("-").append(g.getMaxAge()).append(" yrs</span>")
                     .append("<span class='tag'>Category: ").append(escapeHtml(g.getCategory())).append("</span>")
                     .append("</div></div>")
                     .append("<div class='item-pricing'>")
                     .append("<div class='item-price'>₹").append(String.format("%,.0f", g.getPrice())).append("</div>")
                     .append("<div class='item-occasion'>").append(escapeHtml(g.getOccasion())).append("</div>")
                     .append("</div></div>");
        }

        StringBuilder historyHtml = new StringBuilder();
        if (history.isEmpty()) {
            historyHtml.append("<tr><td colspan='6' style='text-align:center; color:#64748b; padding:12px;'>No searches recorded in database yet.</td></tr>");
        } else {
            for (SearchRecord r : history) {
                String link = "/?age=" + r.getAge() + "&gender=" + r.getGender() + "&interest=" + r.getInterest() + "&budget=" + (int) r.getBudget();
                historyHtml.append("<tr onclick=\"window.location.href='").append(link).append("'\" style='cursor:pointer;'>")
                           .append("<td style='color:#64748b;'>").append(r.getTimestamp()).append("</td>")
                           .append("<td><b>").append(r.getAge()).append(" yrs</b></td>")
                           .append("<td>").append(escapeHtml(r.getGender())).append("</td>")
                           .append("<td>").append(escapeHtml(r.getInterest())).append("</td>")
                           .append("<td style='font-weight:600;'>₹").append(String.format("%,.0f", r.getBudget())).append("</td>")
                           .append("<td><a href='").append(link).append("' style='color:#2563eb; text-decoration:none; font-weight:600;'>View Results &rarr;</a></td>")
                           .append("</tr>");
            }
        }

        String template = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>GiftGenie</title>
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background: #f8fafc; color: #0f172a; line-height: 1.5; padding: 24px 16px; }
            .container { max-width: 820px; margin: 0 auto; }
            
            /* Top Navbar */
            .navbar { display: flex; justify-content: space-between; align-items: center; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px 20px; margin-bottom: 20px; }
            .brand { font-size: 19px; font-weight: 700; color: #0f172a; text-decoration: none; }
            .nav-right { display: flex; align-items: center; gap: 12px; }
            .user-pill { background: #f1f5f9; color: #64748b; padding: 4px 12px; border-radius: 6px; font-size: 13px; font-weight: 500; }
            .user-pill b { color: #0f172a; font-weight: 700; }
            .btn-logout { color: #dc2626; background: #ffffff; border: 1px solid #fecaca; padding: 4px 12px; border-radius: 6px; text-decoration: none; font-size: 13px; font-weight: 600; transition: background 0.15s; }
            .btn-logout:hover { background: #fef2f2; }

            /* Card Containers */
            .card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
            .card-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 16px; }

            /* Form Layout */
            .form-row-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px; }
            .form-row-budget { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 18px; }
            .form-group label { display: block; font-size: 13px; font-weight: 500; color: #334155; margin-bottom: 6px; }
            .form-group input, .form-group select { width: 100%; height: 38px; padding: 0 12px; font-size: 13.5px; border: 1px solid #cbd5e1; border-radius: 6px; outline: none; background: #ffffff; color: #0f172a; }
            .form-group input:focus, .form-group select:focus { border-color: #2563eb; }

            /* Action Button */
            .btn-submit { width: 100%; background: #2563eb; color: #ffffff; border: none; height: 42px; border-radius: 8px; font-size: 14.5px; font-weight: 600; cursor: pointer; transition: background 0.15s; }
            .btn-submit:hover { background: #1d4ed8; }

            /* Recommendations Section */
            .rec-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 12px; margin-bottom: 14px; }
            .rec-subtitle { font-size: 13.5px; color: #475569; }
            .badge { background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; padding: 2px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; }

            /* Item Card */
            .item-card { display: flex; justify-content: space-between; align-items: flex-start; border: 1px solid #e2e8f0; border-radius: 6px; padding: 12px 16px; margin-bottom: 10px; transition: border-color 0.15s; }
            .item-card:hover { border-color: #cbd5e1; background: #f8fafc; }
            .item-name { font-size: 14px; font-weight: 600; color: #0f172a; margin-bottom: 4px; }
            .item-desc { font-size: 12.5px; color: #64748b; margin-bottom: 6px; line-height: 1.4; }
            .item-tags { display: flex; gap: 6px; font-size: 11px; color: #475569; }
            .tag { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; border: 1px solid #e2e8f0; }
            .item-pricing { text-align: right; min-width: 90px; }
            .item-price { font-size: 16px; font-weight: 700; color: #0f172a; }
            .item-occasion { font-size: 11.5px; color: #2563eb; font-weight: 600; margin-top: 2px; }

            /* Table */
            table { width: 100%; border-collapse: collapse; font-size: 12.5px; }
            th, td { padding: 9px 12px; text-align: left; border-bottom: 1px solid #e2e8f0; }
            th { background: #f8fafc; color: #475569; font-weight: 600; }
            tr:hover td { background: #f8fafc; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="navbar">
              <span class="brand">GiftGenie</span>
              <div class="nav-right">
                <span class="user-pill">User: <b>{{USER}}</b></span>
                <a href="/logout" class="btn-logout">Sign Out</a>
              </div>
            </div>

            <!-- Recipient Search Parameters -->
            <div class="card">
              <div class="card-title">Recipient Search Parameters</div>
              <form method="POST" action="/">
                <div class="form-row-3">
                  <div class="form-group">
                    <label>Age (Years)</label>
                    <input type="number" name="age" min="1" max="95" value="{{AGE}}">
                  </div>
                  <div class="form-group">
                    <label>Gender</label>
                    <select name="gender">
                      <option value="Male" {{SEL_MALE}}>Male</option>
                      <option value="Female" {{SEL_FEMALE}}>Female</option>
                      <option value="Any" {{SEL_ANY}}>Any</option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label>Category</label>
                    <select name="interest">
                      <option value="Wellness" {{SEL_WELLNESS}}>Health &amp; Wellness</option>
                      <option value="Fashion" {{SEL_FASHION}}>Traditional Wear</option>
                      <option value="Tech" {{SEL_TECH}}>Audio &amp; Electronics</option>
                      <option value="Books" {{SEL_BOOKS}}>Spiritual &amp; Books</option>
                      <option value="Gaming" {{SEL_GAMING}}>Games &amp; Toys</option>
                    </select>
                  </div>
                </div>

                <div class="form-row-budget">
                  <div class="form-group">
                    <label>Max Budget (₹)</label>
                    <input type="number" name="budget" min="200" max="50000" step="100" value="{{BUDGET}}">
                  </div>
                </div>

                <button type="submit" class="btn-submit">Find Recommendations</button>
              </form>
            </div>

            <!-- Recommendations Output -->
            <div class="card">
              <div class="card-title" style="margin-bottom:8px;">Recommendations</div>
              <div class="rec-header">
                <span class="rec-subtitle">Matching items for {{AGE}} yr old ({{GENDER}})</span>
                <span class="badge">{{RESULTS_COUNT}} Items Found</span>
              </div>
              {{ITEMS}}
            </div>

            <!-- Search History from Database -->
            <div class="card">
              <div class="card-title">Search History &amp; Analytics (Database Log)</div>
              <p style="font-size:12px; color:#64748b; margin-bottom:10px;">Retrieved via JDBC from `search_history` table:</p>
              <table>
                <thead>
                  <tr>
                    <th>Time</th>
                    <th>Age</th>
                    <th>Gender</th>
                    <th>Category</th>
                    <th>Budget</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {{HISTORY}}
                </tbody>
              </table>
            </div>
          </div>
        </body>
        </html>
        """;

        return template
            .replace("{{USER}}", escapeHtml(user))
            .replace("{{AGE}}", String.valueOf(age))
            .replace("{{GENDER}}", escapeHtml(gender))
            .replace("{{SEL_MALE}}", "Male".equalsIgnoreCase(gender) ? "selected" : "")
            .replace("{{SEL_FEMALE}}", "Female".equalsIgnoreCase(gender) ? "selected" : "")
            .replace("{{SEL_ANY}}", "Any".equalsIgnoreCase(gender) ? "selected" : "")
            .replace("{{SEL_WELLNESS}}", "Wellness".equalsIgnoreCase(interest) ? "selected" : "")
            .replace("{{SEL_FASHION}}", "Fashion".equalsIgnoreCase(interest) ? "selected" : "")
            .replace("{{SEL_TECH}}", "Tech".equalsIgnoreCase(interest) ? "selected" : "")
            .replace("{{SEL_BOOKS}}", "Books".equalsIgnoreCase(interest) ? "selected" : "")
            .replace("{{SEL_GAMING}}", "Gaming".equalsIgnoreCase(interest) ? "selected" : "")
            .replace("{{BUDGET}}", String.valueOf((int) budget))
            .replace("{{RESULTS_COUNT}}", String.valueOf(results.size()))
            .replace("{{ITEMS}}", itemsHtml.toString())
            .replace("{{HISTORY}}", historyHtml.toString());
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String getSessionUser(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String c : cookies) {
                String[] pair = c.trim().split("=");
                if (pair.length >= 2 && "gift_user".equals(pair[0])) {
                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    private static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseFormData(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        for (String pair : body.split("&")) {
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
