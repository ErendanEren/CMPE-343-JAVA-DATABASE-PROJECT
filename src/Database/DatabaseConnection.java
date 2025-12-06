package Database;

import models.User;
import models.Manager;
import models.Senior;
import models.Junior;
import models.Tester;

import java.security.MessageDigest;
import java.sql.*;
import java.util.Locale;

/**
 * Central database utility class responsible for managing the JDBC connection
 * and handling user authentication logic.
 * <p>
 * This class provides:
 * <ul>
 *     <li>A simple singleton-style connection manager</li>
 *     <li>A hybrid login method that supports both plain-text and hashed passwords
 *         (for backward compatibility with existing records)</li>
 *     <li>Role-based user object creation</li>
 * </ul>
 *
 * @author Eren Çakır Bircan
 */
public class DatabaseConnection {

    private static final String DATABASE_NAME = "group09_db";
    private static final String DATABASE_USER = "myuser";
    private static final String DATABASE_PASSWORD = "1234";

    private static final String CONNECTION_STRING =
            "jdbc:mysql://localhost:3306/" + DATABASE_NAME +
                    "?useSSL=false&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

    /**
     * Shared JDBC connection instance used as a simple singleton.
     */
    private static Connection connection;

    /**
     * Returns a valid JDBC {@link Connection} to the configured database.
     * <p>
     * If no connection exists or the existing one is closed, a new connection
     * is created using the configured connection string and credentials.
     * Subsequent calls reuse the same connection instance while it is open.
     * </p>
     *
     * @return an open {@link Connection} to the database
     * @throws SQLException if acquiring the connection fails
     * @author Eren Çakır Bircan
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    CONNECTION_STRING,
                    DATABASE_USER,
                    DATABASE_PASSWORD
            );
        }
        return connection;
    }

    /**
     * Closes the shared JDBC connection if it is currently open.
     * <p>
     * Any {@link SQLException} thrown during close is ignored,
     * as this method is intended for best-effort cleanup.
     * </p>
     *
     * @author Eren Çakır Bircan
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * Authenticates a user with the given username and password.
     * <p>
     * The authentication uses a hybrid strategy:
     * <ul>
     *     <li>If the stored value matches the raw password, it is accepted
     *         (for legacy, non-hashed records).</li>
     *     <li>Otherwise, the input is hashed with SHA-256 and compared
     *         against the stored hash.</li>
     * </ul>
     * On successful authentication, a {@link User} instance is created based on
     * the role in the database (Tester, Junior, Senior, Manager), and its fields
     * are populated from the user record.
     * </p>
     *
     * @param username the username supplied by the client
     * @param password the plain-text password supplied by the client
     * @return a populated {@link User} instance if authentication succeeds,
     *         or {@code null} if the user cannot be found or the password is invalid
     * @author Eren Çakır Bircan
     */
    public static User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String storedPassword = rs.getString("password_hash");
                String hashedInput = hashPassword(password);

                boolean isMatch = false;

                if (storedPassword.equals(password)) {
                    isMatch = true;
                } else if (storedPassword.equals(hashedInput)) {
                    isMatch = true;
                }

                if (!isMatch) {
                    return null;
                }

                int id = rs.getInt("user_id");
                String dbUsername = rs.getString("username");
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String role = rs.getString("role");
                Date createdAt = rs.getDate("created_at");

                User user = createUserByRole(role);
                if (user == null) {
                    System.err.println("Unknown role in DB: " + role);
                    return null;
                }

                user.setUserId(id);
                user.setUsername(dbUsername);
                user.setPassword_hash(storedPassword);
                user.setName(name);
                user.setSurname(surname);
                user.setRole(role);
                user.setCreated_at(createdAt);

                return user;
            }

        } catch (SQLException e) {
            System.err.println("Database login error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a concrete {@link User} implementation based on the given role string.
     * <p>
     * The role value is normalized to upper case and matched against known roles:
     * <ul>
     *     <li>"TESTER" → {@link Tester}</li>
     *     <li>"JUNIOR" or "JUNIOR DEVELOPER" → {@link Junior}</li>
     *     <li>"SENIOR" or "SENIOR DEVELOPER" → {@link Senior}</li>
     *     <li>"MANAGER" → {@link Manager}</li>
     * </ul>
     * If the role is unknown or {@code null}, {@code null} is returned.
     * </p>
     *
     * @param role the role string retrieved from the database
     * @return a new {@link User} instance corresponding to the role,
     *         or {@code null} if the role is not recognized
     * @author Eren Çakır Bircan
     */
    private static User createUserByRole(String role) {
        if (role == null) return null;

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "TESTER" -> new Tester();
            case "JUNIOR", "JUNIOR DEVELOPER" -> new Junior();
            case "SENIOR", "SENIOR DEVELOPER" -> new Senior();
            case "MANAGER" -> new Manager();
            default -> null;
        };
    }

    /**
     * Generates a SHA-256 hash of the given plain-text password.
     * <p>
     * The resulting hash is returned as a lowercase hexadecimal string
     * without any separators.
     * </p>
     *
     * @param plainText the password or text to be hashed
     * @return the SHA-256 hash encoded as a hexadecimal string
     * @throws RuntimeException if the hashing algorithm is not available
     *                          or an unexpected error occurs
     * @author Eren Çakır Bircan
     */
    private static String hashPassword(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainText.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed inside DB Connection", e);
        }
    }
}
