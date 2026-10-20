import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain entity representing an authenticated user account in the system.
 */
public class User {

    private final String username;
    private final String password;
    private final LocalDateTime createdAt;

    public User(String username, String password) {
        this.username = Objects.requireNonNull(username, "Username cannot be null").trim();
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.createdAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public boolean validatePassword(String inputPassword) {
        if (inputPassword == null) return false;
        return this.password.equals(inputPassword.trim());
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return username.equalsIgnoreCase(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username.toLowerCase());
    }

    @Override
    public String toString() {
        return "User{" + "username='" + username + '\'' + ", createdAt=" + createdAt + '}';
    }
}
