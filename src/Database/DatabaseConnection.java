package Database; // or `database` if you rename the package

import models.User;
import models.Manager;
import models.Senior;
import models.Junior;
import models.Tester;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class DatabaseConnection {

    // ==== MySQL settings ====
    // Must match your MySQL configuration
    private static final String DATABASE_NAME = "group09_db";
    private static final String DATABASE_USER = "myuser";
    private static final String DATABASE_PASSWORD = "1234";

    private static final String CONNECTION_STRING =
            "jdbc:mysql://localhost:3306/" + DATABASE_NAME +
                    "?useSSL=false&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

    // Re-used connection (simple singleton style)
    private static Connection connection;

    // ==== Open / get connection ====
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

    // ==== Close connection ====
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

    // ==== LOGIN ====
    // Tries to log in with username + password.
    // Returns the correct User subclass (Manager / Senior / Junior / Tester) or null if failed.
    public static User login(String username, String password) {
        String sql =
                "SELECT user_id, username, password_hash, name, surname, role, " +
                        "       created_at, updated_at " +
                        "FROM users " +
                        "WHERE username = ? AND password_hash = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);  // currently plain text "1234" in DB

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // No user found
                    return null;
                }

                int id = rs.getInt("user_id");
                String dbUsername = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String role = rs.getString("role");
                Date createdAt = rs.getDate("created_at");
                Date updatedAt = rs.getDate("updated_at");

                // Create correct subclass based on role from DB
                User user = createUserByRole(role);
                if (user == null) {
                    System.err.println("Unknown role in DB: " + role);
                    return null;
                }

                user.setUserId(id);
                user.setUsername(dbUsername);
                user.setPassword_hash(passwordHash);
                user.setName(name);
                user.setSurname(surname);
                user.setRole(role);
                user.setCreated_at(createdAt);
                user.setUpdated_at(updatedAt);

                return user;
            }

        } catch (SQLException e) {
            System.err.println("Database login error: " + e.getMessage());
            return null;
        }
    }

    // ==== Helper: map role string to correct subclass ====
    private static User createUserByRole(String role) {
        if (role == null) return null;

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "TESTER" -> new Tester();
            case "JUNIOR DEVELOPER" -> new Junior();
            case "SENIOR DEVELOPER" -> new Senior();
            case "MANAGER" -> new Manager();
            default -> null;
        };
    }

    // Optional: small test
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connection OK: " + !conn.isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        User u = login("man", "1234");
        if (u != null) {
            System.out.println("Login OK: " + u.getUsername() + " - " + u.getRole());
        } else {
            System.out.println("Login failed");
        }
    }
}
