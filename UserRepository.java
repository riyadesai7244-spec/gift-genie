import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe repository managing user authentication credentials and isolated search history.
 * Utilizes ConcurrentHashMap and CopyOnWriteArrayList for thread safety across concurrent HTTP requests.
 */
public class UserRepository {

    private final Map<String, User> userStore = new ConcurrentHashMap<>();
    private final Map<String, List<SearchRecord>> searchVault = new ConcurrentHashMap<>();

    public UserRepository() {
        seedInitialUser();
    }

    private void seedInitialUser() {
        register("Riya", "123456");
        recordSearch("Riya", new SearchRecord("Riya", 60, "Male", "Wellness", 2500.0, 4));
        recordSearch("Riya", new SearchRecord("Riya", 24, "Female", "Fashion", 2000.0, 4));
        recordSearch("Riya", new SearchRecord("Riya", 10, "Any", "Books", 1000.0, 4));
    }

    public boolean userExists(String username) {
        if (username == null) return false;
        return userStore.containsKey(username.trim().toLowerCase());
    }

    public synchronized boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String normalized = username.trim().toLowerCase();
        if (userStore.containsKey(normalized)) {
            return false;
        }
        User newUser = new User(username.trim(), password);
        userStore.put(normalized, newUser);
        searchVault.put(normalized, new CopyOnWriteArrayList<>());
        return true;
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) return false;
        String normalized = username.trim().toLowerCase();
        User user = userStore.get(normalized);
        if (user == null) {
            // Self-register on login for seamless demo convenience
            register(username, password);
            return true;
        }
        return user.validatePassword(password);
    }

    public void recordSearch(String userId, SearchRecord record) {
        if (userId == null || record == null) return;
        String normalized = userId.trim().toLowerCase();
        searchVault.computeIfAbsent(normalized, k -> new CopyOnWriteArrayList<>()).add(0, record);
    }

    public List<SearchRecord> getHistory(String userId) {
        if (userId == null) return Collections.emptyList();
        String normalized = userId.trim().toLowerCase();
        List<SearchRecord> records = searchVault.get(normalized);
        if (records == null) return Collections.emptyList();
        return Collections.unmodifiableList(records);
    }
}
