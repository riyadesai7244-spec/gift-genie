# 🎁 GiftGenie – Java Web App with Eclipse & JDBC Database Connectivity

A clean, easy-to-read Java web application with **JDBC Database Connectivity** (SQLite) configured for **Eclipse IDE**. Built without using inventories — all gifts, user accounts, and search histories are stored and queried directly through SQL tables (`users`, `search_history`, `gifts`).

---

## 📁 Project Structure (Single Java File Architecture)

The entire application logic is contained in a single `.java` file for simplicity and ease of evaluation:

```text
simple-gift-app/
│
├── SimpleGiftApp.java    # The ONLY Java file: Models, Interface, DatabaseManager (JDBC) & Web Server
├── lib/
│   └── sqlite-jdbc.jar   # SQLite JDBC Database Driver
├── .classpath            # Eclipse IDE classpath configuration (pre-linked)
├── .project              # Eclipse IDE project definition
├── run.bat               # Windows one-click runner batch script
├── .gitignore            # Excludes compiled binaries and local database file
└── README.md             # Project documentation & Eclipse guide
```

---

## ☕ Running in Eclipse IDE (Step-by-Step)

This project includes pre-configured `.project` and `.classpath` files so it opens in Eclipse with zero manual build-path setup:

1. Open **Eclipse IDE**.
2. Click **File** ➔ **Open Projects from File System...** (or **Import...** ➔ **General** ➔ **Existing Projects into Workspace**).
3. Click **Directory...** and select:
   ```text
   c:\Users\riyad\OneDrive\Documents\riya\simple-gift-app
   ```
4. Click **Finish**. Eclipse will detect `simple-gift-app` and automatically link `lib/sqlite-jdbc.jar`.
5. In the Package Explorer, open `SimpleGiftApp.java`.
6. Right-click anywhere in `SimpleGiftApp.java` ➔ **Run As** ➔ **Java Application**.
7. Open your browser and visit: **http://localhost:8080**

---

## 🗄️ Database Connectivity (Pure JDBC - No Inventories)

Instead of using in-memory inventory lists or inventory management classes, all data persistence is handled by **JDBC (Java Database Connectivity)** via `DatabaseManager`:

1. **Database File**: `giftgenie.db` (auto-created via SQLite on first launch).
2. **Tables**:
   - `users`: Stores user credentials (`id`, `username`, `password`) for authentication and multi-user isolation.
   - `search_history`: Logs demographic queries (`id`, `username`, `age`, `gender`, `category`, `budget`, `timestamp`) for data analytics.
   - `gifts`: Stores gift data (`id`, `name`, `category`, `gender`, `min_age`, `max_age`, `price`, `occasion`, `description`).
3. **Core JDBC Classes Used**:
   - `Connection`: Established via `DriverManager.getConnection("jdbc:sqlite:giftgenie.db")`.
   - `PreparedStatement`: Parameterized SQL queries preventing SQL injection and handling type-safe data retrieval.
   - `ResultSet`: Reads relational query outputs into domain objects.
   - `Statement`: DDL execution for `CREATE TABLE IF NOT EXISTS`.

---

## 🚀 How to Run via Command Line / Terminal

### 1. Windows One-Click
Double-click `run.bat` or run:
```cmd
run.bat
```

### 2. Manual Command
```bash
# Compile with JDBC jar
javac -encoding UTF-8 -cp ".;lib/*" SimpleGiftApp.java

# Run
java -cp ".;lib/*" SimpleGiftApp
```

Visit: **http://localhost:8080**
Default credentials: Username: **Riya** | Password: **123456** (or create any new user account on the Sign In page).
