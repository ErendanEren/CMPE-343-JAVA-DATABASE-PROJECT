package models;

import Database.DatabaseConnection;
import util.ConsoleUI;

import java.security.MessageDigest;
import java.sql.*;
import java.util.Scanner;
import java.util.Stack;

/**
 * Represents the Manager role in the Contact Management System.
 * <p>
 * This class provides administrative functionalities such as adding, updating,
 * and deleting users, as well as viewing system statistics.
 * It also implements a session-based undo mechanism using a Stack.
 * </p>
 *
 * @author Zafer Mert Serinken
 */
public class Manager extends User {

    /**
     * A Stack data structure to store deleted users temporarily.
     * This allows for the "Undo" operation within the current session.
     */
    private Stack<User> deletedUsersStack = new Stack<>();

    /**
     * Displays the main menu for the Manager role and handles user input.
     * The menu loop continues until the user chooses to logout.
     *
     * @param scanner The Scanner object used to read user input from the console.
     */
    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String headerTitle = ConsoleUI.BLUE_BOLD + "Manager Panel: " + getName() + " " + getSurname() + ConsoleUI.RESET;

            // Stack durumu (Boş mu dolu mu) menüde gösterilir
            String undoOption = deletedUsersStack.isEmpty() ? "7) Undo Last Delete (Stack Empty)" : ConsoleUI.GREEN_BOLD + "7) UNDO LAST DELETE (" + deletedUsersStack.size() + ")" + ConsoleUI.RESET;

            String choice = ConsoleUI.showMenu(
                    headerTitle,
                    new String[]{
                            "1) List all users",
                            "2) Add new user (Employ)",
                            "3) Update existing user (Promote)",
                            "4) Delete user (Fire)",
                            "5) Show system statistics",
                            "6) Change password",
                            undoOption,
                            "",
                            "0) Logout"
                    },
                    scanner
            );

            switch (choice) {
                case "1" -> listAllUsers();
                case "2" -> addUser(scanner);
                case "3" -> updateUser(scanner);
                case "4" -> deleteUser(scanner);
                case "5" -> showContactStats();
                case "6" -> changePassword(scanner);
                case "7" -> undoLastDelete();
                case "0" -> {
                    System.out.println("Logging out... (Undo stack will be cleared)");
                    deletedUsersStack.clear();
                    running = false;
                }
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    /**
     * Restores the most recently deleted user from the stack back to the database.
     * If the stack is empty, it informs the user that there is nothing to undo.
     */
    private void undoLastDelete() {
        if (deletedUsersStack.isEmpty()) {
            ConsoleUI.printError("Nothing to undo! Stack is empty.");
            return;
        }

        // 1. Stack'in tepesinden son silinen kişiyi al (POP)
        User restoredUser = deletedUsersStack.pop();

        System.out.println("Restoring user: " + restoredUser.getName() + " " + restoredUser.getSurname() + "...");

        String sql = "INSERT INTO users (username, password_hash, name, surname, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, restoredUser.getUsername());
            pstmt.setString(2, restoredUser.getPassword_hash()); // Zaten hashli duruyordu
            pstmt.setString(3, restoredUser.getName());
            pstmt.setString(4, restoredUser.getSurname());
            pstmt.setString(5, restoredUser.getRole());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ConsoleUI.printInfo("UNDO SUCCESSFUL: User '" + restoredUser.getUsername() + "' is back!");
            }

        } catch (SQLException e) {
            // Hata olursa kullanıcıyı kaybetmemek için yığına geri koy
            deletedUsersStack.push(restoredUser);
            ConsoleUI.printError("Undo Failed (User pushed back to stack): " + e.getMessage());
        }
    }

    /**
     * Deletes a user from the system based on the provided User ID.
     * Before deletion, the user data is backed up to a Stack to enable the Undo operation.
     *
     * @param scanner The Scanner object used to read the User ID from the console.
     */
    public void deleteUser(Scanner scanner) {
        listAllUsers();
        System.out.println(ConsoleUI.RED_BOLD + "--- Fire (Delete) User ---" + ConsoleUI.RESET);
        System.out.print("Enter User ID to DELETE: ");

        int targetId;
        try {
            targetId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid ID.");
            return;
        }

        // KENDİNİ SİLME KORUMASI
        if (targetId == getUserId()) {
            ConsoleUI.printError("CRITICAL: You cannot fire yourself!");
            return;
        }

        // ADIM 1: Silmeden önce veriyi çek (BACKUP)
        User userBackup = getUserById(targetId);

        if (userBackup == null) {
            ConsoleUI.printError("User ID not found. Cannot delete.");
            return;
        }

        System.out.print("Are you sure you want to fire " + userBackup.getName() + "? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) return;

        // ADIM 2: Veritabanından Sil
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, targetId);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                // ADIM 3: Başarılıysa Stack'e at (PUSH)
                deletedUsersStack.push(userBackup);
                ConsoleUI.printInfo("User deleted. (Added to Undo Stack)");
            } else {
                ConsoleUI.printError("Delete failed.");
            }

        } catch (SQLException e) {
            ConsoleUI.printError("DB Error: " + e.getMessage());
        }
    }

    /**
     * Retrieves a User object from the database using the given User ID.
     * This is a helper method primarily used for backing up user data before deletion.
     *
     * @param id The unique identifier of the user to retrieve.
     * @return A User object (subclass determined by role) containing the user's data, or null if not found.
     */
    private User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                // Polymorphism: Rolüne göre doğru nesneyi oluştur
                User u = switch (role) {
                    case "Tester" -> new Tester();
                    case "Junior Developer" -> new Junior();
                    case "Senior Developer" -> new Senior();
                    case "Manager" -> new Manager();
                    default -> new Tester();
                };

                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPassword_hash(rs.getString("password_hash"));
                u.setName(rs.getString("name"));
                u.setSurname(rs.getString("surname"));
                u.setRole(role);
                return u;
            }
        } catch (SQLException e) {
            System.out.println("Backup error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lists all users currently registered in the database.
     * Displays ID, Username, Name, Surname, and Role in a formatted table.
     */
    public void listAllUsers() {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.BLUE_BOLD + "--- Current Staff List ---" + ConsoleUI.RESET);
        System.out.printf("%-5s %-15s %-15s %-15s %-15s%n", "ID", "Username", "Name", "Surname", "Role");
        System.out.println("---------------------------------------------------------------------");

        String sql = "SELECT user_id, username, name, surname, role FROM users ORDER BY role, user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.printf("%-5d %-15s %-15s %-15s %-15s%n",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("role"));
            }
        } catch (SQLException e) {
            ConsoleUI.printError("DB Error: " + e.getMessage());
        }
        ConsoleUI.pause();
    }

    /**
     * Adds a new user to the system.
     * Checks for duplicate usernames and hashes the password before storage.
     *
     * @param scanner The Scanner object used to read user input.
     */
    public void addUser(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.BLUE_BOLD + "--- Add New User ---" + ConsoleUI.RESET);

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (isUsernameExists(username)) {
            ConsoleUI.printError("ERROR: Username '" + username + "' is already taken!");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Surname: ");
        String surname = scanner.nextLine().trim();

        System.out.println("Select Role: 1) Tester  2) Junior Dev  3) Senior Dev  4) Manager");
        System.out.print("Choice: ");
        String roleChoice = scanner.nextLine().trim();

        String role = switch (roleChoice) {
            case "1" -> "Tester";
            case "2" -> "Junior Developer";
            case "3" -> "Senior Developer";
            case "4" -> "Manager";
            default -> "Tester";
        };

        String hashedPassword = hashPassword(password);

        String sql = "INSERT INTO users (username, password_hash, name, surname, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, name);
            pstmt.setString(4, surname);
            pstmt.setString(5, role);

            int affected = pstmt.executeUpdate();
            if (affected > 0) ConsoleUI.printInfo("User added successfully!");

        } catch (SQLException e) {
            ConsoleUI.printError("Error adding user: " + e.getMessage());
        }
    }

    /**
     * Updates an existing user's information, including Name, Surname, and Role (Promotion).
     * If fields are left empty, the existing values are preserved.
     *
     * @param scanner The Scanner object used to read user input.
     */
    public void updateUser(Scanner scanner) {
        listAllUsers();
        System.out.println(ConsoleUI.BLUE_BOLD + "--- Update User / Promotion ---" + ConsoleUI.RESET);
        System.out.print("Enter User ID to update (0 to cancel): ");

        int targetId;
        try {
            targetId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid ID format.");
            return;
        }

        if (targetId == 0) return;

        System.out.print("New Name (Press Enter to keep): ");
        String newName = scanner.nextLine().trim();
        System.out.print("New Surname (Press Enter to keep): ");
        String newSurname = scanner.nextLine().trim();

        System.out.println("Select New Role (Press Enter to keep):");
        System.out.println("1) Tester  2) Junior Dev  3) Senior Dev  4) Manager");
        String roleChoice = scanner.nextLine().trim();

        String newRole = null;
        if (!roleChoice.isEmpty()) {
            newRole = switch (roleChoice) {
                case "1" -> "Tester";
                case "2" -> "Junior Developer";
                case "3" -> "Senior Developer";
                case "4" -> "Manager";
                default -> null;
            };
        }

        if (newName.isEmpty() && newSurname.isEmpty() && newRole == null) {
            ConsoleUI.printError("No changes made.");
            return;
        }

        // Eski verileri çekip, boş geçilen alanları eski verilerle dolduruyoruz
        User current = getUserById(targetId);
        if (current == null) {
            ConsoleUI.printError("User not found!");
            return;
        }

        if (newName.isEmpty()) newName = current.getName();
        if (newSurname.isEmpty()) newSurname = current.getSurname();
        if (newRole == null) newRole = current.getRole();

        String sql = "UPDATE users SET name = ?, surname = ?, role = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setString(2, newSurname);
            pstmt.setString(3, newRole);
            pstmt.setInt(4, targetId);

            int affected = pstmt.executeUpdate();
            if (affected > 0) ConsoleUI.printInfo("User updated successfully.");

        } catch (SQLException e) {
            ConsoleUI.printError("DB Error: " + e.getMessage());
        }
    }

    /**
     * Displays statistical information about the contacts in the database.
     * Currently shows total contact count and LinkedIn usage.
     */
    public void showContactStats() {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.BLUE_BOLD + "--- System Statistics ---" + ConsoleUI.RESET);

        String sqlTotal = "SELECT COUNT(*) as total FROM contacts";
        String sqlLinkedin = "SELECT COUNT(*) as linked FROM contacts WHERE linkedin_url IS NOT NULL AND linkedin_url != ''";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sqlTotal);
            if (rs.next()) {
                System.out.println("Total Contacts: " + ConsoleUI.CYAN_BOLD + rs.getInt("total") + ConsoleUI.RESET);
            }
            rs.close();

            rs = stmt.executeQuery(sqlLinkedin);
            if (rs.next()) {
                System.out.println("With LinkedIn: " + ConsoleUI.CYAN_BOLD + rs.getInt("linked") + ConsoleUI.RESET);
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Stats Error: " + e.getMessage());
        }
        ConsoleUI.pause();
    }

    /**
     * Changes the password of the currently logged-in Manager.
     * The new password is hashed before being stored.
     *
     * @param scanner The Scanner object used to read the new password.
     */
    public void changePassword(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("--- Change My Password ---");
        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine().trim();

        if (newPass.length() < 3) {
            ConsoleUI.printError("Password is too short!");
            return;
        }

        String hashed = hashPassword(newPass);
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashed);
            pstmt.setInt(2, getUserId());

            int affected = pstmt.executeUpdate();
            if (affected > 0) ConsoleUI.printInfo("Password updated successfully.");

        } catch (SQLException e) {
            ConsoleUI.printError("DB Error: " + e.getMessage());
        }
    }

    // ================= YARDIMCI METODLAR =================

    /**
     * Hashes a plain text password using the SHA-256 algorithm.
     *
     * @param plainText The plain text password to hash.
     * @return A hexadecimal string representation of the hashed password.
     * @throws RuntimeException if the SHA-256 algorithm is not available.
     */
    private String hashPassword(String plainText) {
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
            throw new RuntimeException("Hashing failed");
        }
    }

    /**
     * Checks if a given username already exists in the database.
     *
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     */
    private boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}