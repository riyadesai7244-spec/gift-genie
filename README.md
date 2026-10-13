# 🎁 GiftGenie – Personalized Indian Gift Recommendation Web App

A clean, full-stack Java web application built on **Java Module 1 Fundamentals** (Object-Oriented Programming, Interfaces, Control Flow, Collections, and Multi-User Isolation). Tailored for an Indian audience with real-time Indian Rupee (₹) pricing and culturally accurate gifting options.

---

## ✨ Features
- **Strict Demographic & Age-Appropriate Gating**: Ensures seniors and elders (e.g., 60yo uncle) receive thoughtful items (*Saregama Carvaan*, *Khadi Nehru Jacket*, *Omron BP Monitor*, *Ayurvedic Copper Dispenser*) rather than out-of-context gadgets (like gaming mice).
- **Direct Recipient Filter Wizard**: Instant selection by **Age**, **Gender / Relation**, **Category Focus**, and **Max Budget (₹ INR)**.
- **Multi-User Account System**: Includes dedicated **Sign In** and **Create Account (Sign Up)** tabs with user credential storage.
- **Isolated User History**: Search queries and recommendation logs are isolated per logged-in user profile with clickable recent searches.
- **Zero Heavy Dependencies**: Runs directly on Java's lightweight built-in HTTP server (`com.sun.net.httpserver.HttpServer`).
- **Warm Champagne / Cream Aesthetic**: High-contrast editorial serif typography (`Playfair Display`), warm amber accents, and responsive layout.

---

## 🛠️ Java Concepts Demonstrated
1. **Java Architecture**: JDK compilation (`javac`), bytecode execution on JVM (`java`).
2. **Interfaces & Polymorphism**:
   - `interface GiftRecommender`: Contract for generating filtered recommendation sets.
   - `interface UserAnalyticsTracker`: Contract for analyzing user budgets and category trends.
3. **Java Collections**:
   - `ArrayList<Gift>` for catalog and recommendation ranking.
   - `HashMap<String, ArrayList<SearchRecord>>` for user vault isolation.
4. **Control Flow & Guardrails**:
   - Strict bounds checking (`age >= minAge && age <= maxAge`).
   - Categorical fallback logic preserving demographic rules.

---

## 🚀 How to Run Locally

### Prerequisites
- Java Development Kit (JDK 17 or higher recommended). Verify using:
  ```bash
  javac -version
  java -version
  ```

### Steps to Run
1. Open PowerShell or Command Prompt in the project folder:
   ```bash
   cd simple-gift-app
   ```
2. Compile the Java source file:
   ```bash
   javac -encoding UTF-8 SimpleGiftApp.java
   ```
3. Start the web server:
   ```bash
   java SimpleGiftApp
   ```
4. Open your web browser and navigate to:
   - **http://localhost:8080**
