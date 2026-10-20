import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory repository managing the curated Indian product catalog.
 * Encapsulates catalog population and query methods.
 */
public class GiftCatalog {

    private final List<Gift> inventory = new ArrayList<>();

    public GiftCatalog() {
        seedInventory();
    }

    /**
     * Seeds initial Indian catalog items spanning seniors, middle adults, youth, and kids.
     */
    private void seedInventory() {
        // --- Seniors & Elders (50 - 95 yrs) ---
        inventory.add(new Gift(
                "Saregama Carvaan (Preloaded 5000 Classic Hindi Songs & Bhajans)",
                "Tech", "Male", 50, 95, 2490.0, "Evergreen Classics", "📻",
                "Pre-loaded golden-era songs by Kishore Kumar, Lata Mangeshkar, Rafi & Bhajans with stereo sound."
        ));
        inventory.add(new Gift(
                "Khadi Silk Handloom Nehru Jacket / Bandhgala Waistcoat",
                "Fashion", "Male", 45, 95, 1499.0, "Traditional Wear", "🧥",
                "Pure handloom silk-blend waistcoat, dignified and regal for pujas, family gatherings, and weddings."
        ));
        inventory.add(new Gift(
                "Omron Smart Digital Blood Pressure & Heart Monitor",
                "Wellness", "Any", 45, 95, 1850.0, "Health Care", "🩺",
                "Clinically validated one-touch automatic BP monitor with memory tracking and clear display."
        ));
        inventory.add(new Gift(
                "Pure Copper Ayurvedic Water Dispenser & Tumbler Set (5L)",
                "Wellness", "Any", 40, 95, 1699.0, "Ayurvedic Health", "🫖",
                "Traditional hammered pure copper storage promoting digestion, immunity, and overall well-being."
        ));
        inventory.add(new Gift(
                "Orthopedic Memory Foam Spine & Lumbar Support Back Cushion",
                "Wellness", "Any", 45, 95, 999.0, "Back Comfort", "🛋️",
                "Ergonomic contour relieving lower back pain and fatigue during sitting, prayer, or travel."
        ));
        inventory.add(new Gift(
                "Handcrafted Rosewood Walking Cane with Solid Brass Handle",
                "Wellness", "Male", 55, 95, 899.0, "Thoughtful Care", "🦯",
                "Sturdy seasoned Indian rosewood with slip-free rubber tip and royal polished brass grip."
        ));
        inventory.add(new Gift(
                "Shrimad Bhagavad Gita Deluxe Gold Foil Edition (Large Hindi Font)",
                "Books", "Any", 40, 95, 999.0, "Spiritual Keepsake", "🕉️",
                "Hardbound with gold-gilded edges, silk ribbon, and easy-to-read commentary."
        ));
        inventory.add(new Gift(
                "Electric Heated Knee & Joint Pain Relief Wrap",
                "Wellness", "Any", 50, 95, 1299.0, "Joint Relief", "⚡",
                "Therapeutic thermal heat soothing knee stiffness, arthritis, and joint fatigue."
        ));
        inventory.add(new Gift(
                "Titan Classic Gold Dial Analog Watch with Leather Strap",
                "Fashion", "Male", 40, 95, 2495.0, "Timeless Watch", "⌚",
                "Distinguished gold dial with Roman numerals and genuine brown leather strap."
        ));
        inventory.add(new Gift(
                "Pure Pashmina Kashmiri Embroidered Winter Shawl",
                "Fashion", "Female", 40, 95, 1799.0, "Winter Warmth", "🧣",
                "Handcrafted soft wool shawl featuring authentic Kashmiri Tilla needlework."
        ));
        inventory.add(new Gift(
                "Chanderi Handloom Silk Saree with Rich Zari Border",
                "Fashion", "Female", 40, 90, 2199.0, "Festive & Puja", "🥻",
                "Lightweight festive handloom saree with woven gold border and matching blouse piece."
        ));
        inventory.add(new Gift(
                "Saregama Carvaan Mini Bhakti Edition (Aartis & Amritwani)",
                "Tech", "Female", 45, 95, 2190.0, "Daily Devotional Peace", "🪔",
                "Dedicated spiritual audio player with morning aartis, bhajans, and sacred chants."
        ));
        inventory.add(new Gift(
                "Royal Afghan Dry Fruit Gift Box (Walnuts, Badam & Saffron)",
                "Hamper", "Any", 40, 95, 1399.0, "Nutrition & Health", "🌰",
                "Wooden gift box with premium Kashmiri walnuts, roasted cashews, and saffron."
        ));

        // --- Middle-Aged Adults (35 - 54 yrs) ---
        inventory.add(new Gift(
                "WildHorn Genuine RFID Leather Laptop Messenger Bag",
                "Fashion", "Male", 25, 55, 1899.0, "Office & Travel", "💼",
                "Executive full-grain leather briefcase with padded 15.6 inch laptop compartment."
        ));
        inventory.add(new Gift(
                "Philips Cordless Beard Trimmer & Grooming Kit Series 3000",
                "Tech", "Male", 20, 55, 1499.0, "Daily Grooming", "✂️",
                "Self-sharpening titanium blades with 20 length settings and 60min runtime."
        ));
        inventory.add(new Gift(
                "Ceramic Studio Handpainted 12-Piece Dinnerware Set",
                "Hamper", "Any", 25, 65, 1850.0, "Dining & Home", "🍽️",
                "Glazed artisanal ceramic plates and bowls for hosting family dinners."
        ));
        inventory.add(new Gift(
                "Deep Tissue Percussion Muscle Massage Gun (6 Heads)",
                "Fitness", "Any", 20, 60, 2499.0, "Muscle Recovery", "💆",
                "Targets sore neck, back, and leg muscles after long work hours or workouts."
        ));
        inventory.add(new Gift(
                "Forest Essentials Luxury Ayurvedic Facial Care Gift Box",
                "Fashion", "Female", 22, 60, 2250.0, "Pampering & Glow", "🌸",
                "Pure rose water mist, gentle herbal cleanser, and date & litchi moisturiser."
        ));

        // --- Young Adults & Teens (13 - 35 yrs) ---
        inventory.add(new Gift(
                "boAt Airdopes True Wireless Bluetooth Earbuds",
                "Tech", "Any", 14, 40, 1299.0, "Daily Audio", "🎧",
                "Quad Mics with ENx noise cancellation and 50 hours battery life."
        ));
        inventory.add(new Gift(
                "Noise ColorFit Ultra Smart Watch with Bluetooth Calling",
                "Tech", "Any", 15, 45, 1899.0, "Fitness & Calls", "⌚",
                "HD AMOLED display, heart rate, SpO2 tracker, and 100+ sports modes."
        ));
        inventory.add(new Gift(
                "Kundan Choker & Jhumka Traditional Jewellery Set",
                "Fashion", "Female", 16, 45, 1199.0, "Festive Glam", "✨",
                "Handcrafted Meenakari kundan necklace set with matching jhumkas."
        ));
        inventory.add(new Gift(
                "Logitech G102 Optical RGB Gaming Mouse (8000 DPI)",
                "Gaming", "Any", 12, 30, 1299.0, "Gaming & PC", "🖱️",
                "Tactile gaming-grade sensor with customizable RGB color lighting."
        ));
        inventory.add(new Gift(
                "Wireless Dual-Vibration Ergonomic Game Controller",
                "Gaming", "Any", 12, 32, 1699.0, "Gamer Special", "🎮",
                "Hall-effect analog joysticks with pressure-sensitive triggers."
        ));

        // --- Kids (4 - 12 yrs) ---
        inventory.add(new Gift(
                "Smartivity STEM DIY Hydraulic Crane & Mechanical Toy Kit",
                "Gaming", "Any", 6, 12, 899.0, "Learning & Play", "🏗️",
                "Eco-friendly wooden DIY kit teaching mechanics and hydraulics to kids."
        ));
        inventory.add(new Gift(
                "Panchatantra & Amar Chitra Katha Illustrated Stories (Box of 5)",
                "Books", "Any", 5, 12, 799.0, "Moral Tales", "📚",
                "Inspiring illustrated Indian folk stories fostering wisdom and heritage."
        ));
    }

    /**
     * Returns an unmodifiable view of all gifts in the catalog.
     */
    public List<Gift> getAllGifts() {
        return Collections.unmodifiableList(inventory);
    }
}
