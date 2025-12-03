package Database;

import models.User;
import models.Manager;
import models.Senior;
import models.Junior;
import models.Tester;

import java.sql.*;
import java.util.Locale;

public class DatabaseConnection {

    // ==== MySQL settings ====
    private static final String DATABASE_NAME = "group09_db";  // MySQL'deki DB ismin
    private static final String DATABASE_USER = "myuser";      // MySQL kullanıcı adın
    private static final String DATABASE_PASSWORD = "1234";    // MySQL şifren

    private static final String CONNECTION_STRING =
            "jdbc:mysql://localhost:3306/" + DATABASE_NAME +
                    "?useSSL=false&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

    // ==== basit singleton ====
    private static Connection connection;

    // ==== Connection get ====
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

    // ==== Connection close ====
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

    public static User login(String username, String password) {
        String sql =
                "SELECT user_id, username, password_hash, name, surname, role, " +
                        "       created_at, updated_at " +
                        "FROM users " +
                        "WHERE username = ? AND password_hash = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);  // şimdilik plaintext, DB’de de öyle

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // kullanıcı yok
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
}