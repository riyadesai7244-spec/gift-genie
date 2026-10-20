# 🎁 GiftGenie – Personalized Indian Gift Recommendation Web App

A clean, modular Java web application developed following **Java Module 1 Fundamentals** and standard **Object-Oriented Software Engineering Principles** (Model-View-Controller architecture, Encapsulation, Interfaces, Polymorphism, and Thread-Safe Data Isolation).

Tailored for an Indian audience with real-time Indian Rupee (₹) pricing and culturally accurate gifting options.

---

## 🏛️ Architecture & Project Structure

The project separates presentation markup, business logic, domain models, and repositories:

```text
simple-gift-app/
│
├── templates/                     # Presentation Layer (HTML5 & CSS)
│   ├── auth.html                  # Sign In & Account Creation view
│   └── dashboard.html             # Curated recommendations & filters dashboard
│
├── Gift.java                      # Domain Entity: Encapsulated gift item with age/gender validation
├── SearchRecord.java              # Domain Entity: Encapsulated search log with redirect query generator
├── User.java                      # Domain Entity: User credentials and authentication
│
├── GiftRecommender.java           # Business Interface: Contract for recommendation algorithms
├── UserAnalyticsTracker.java      # Business Interface: Contract for user metrics & data isolation
│
├── GiftCatalog.java               # Repository: Curated Indian product inventory
├── UserRepository.java            # Repository: Thread-safe user storage & search vaults (ConcurrentHashMap)
│
├── GiftService.java               # Service Layer: Implements GiftRecommender & UserAnalyticsTracker
├── TemplateRenderer.java          # View Renderer: Merges dynamic Java data with HTML templates
├── SimpleGiftApp.java             # Controller & Entry Point: Configures routes & built-in HTTP server
│
├── ConsoleGiftApp.java            # Standalone CLI demo of recommendation algorithm
├── run.bat                        # One-click Windows launch script
└── README.md                      # Project documentation
```

---

## 💡 Core Java Concepts Demonstrated

1. **Object-Oriented Encapsulation**:
   - All domain entities (`Gift`, `SearchRecord`, `User`) declare fields as `private final`, ensuring immutability and data integrity.
   - Access is mediated exclusively through public getters and validation methods (`isAgeAppropriate()`, `matchesGender()`, `validatePassword()`).

2. **Abstraction & Interfaces**:
   - `GiftRecommender`: Decouples the recommendation algorithm contract from its concrete implementation.
   - `UserAnalyticsTracker`: Enforces personal analytics tracking and isolated history retrieval contracts.

3. **Separation of Concerns (MVC)**:
   - **Model**: `Gift`, `SearchRecord`, `User`.
   - **Repository Layer**: `GiftCatalog`, `UserRepository`.
   - **Service Layer**: `GiftService`.
   - **View Layer**: `templates/dashboard.html`, `templates/auth.html`, `TemplateRenderer`.
   - **Controller Layer**: `SimpleGiftApp` with dedicated route handlers.

4. **Thread Safety & Multi-Threading**:
   - Employs `ConcurrentHashMap` and `CopyOnWriteArrayList` in `UserRepository` to guarantee safety across concurrent HTTP requests.

5. **Guardrails & Control Flow**:
   - Strict demographic bounds checking ensures senior recipients (e.g. 60yo uncle) never receive gaming controllers or youth gadgets.

---

## 🚀 How to Run Locally

### Prerequisites
- JDK 17 or higher. Verify installation:
  ```bash
  javac -version
  java -version
  ```

### Quick Start
1. Open PowerShell or Command Prompt in the project folder:
   ```bash
   cd c:\Users\riyad\OneDrive\Documents\riya\simple-gift-app
   ```
2. Double-click `run.bat` **or** run manually:
   ```bash
   javac -encoding UTF-8 SimpleGiftApp.java
   java SimpleGiftApp
   ```
3. Open your browser and visit:
   - **http://localhost:8080**
