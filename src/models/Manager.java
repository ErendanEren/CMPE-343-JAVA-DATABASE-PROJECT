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
 * and deleting users. It creates advanced statistical reports about the contacts
 * (age analytics, name frequencies) and implements a session-based undo mechanism.
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
     * <p>
     * This method acts as the main controller for the Manager's session.
     * </p>
     *
     * @param scanner The Scanner object used to read user input from the console.
     * @author Zafer Mert Serinken
     */
    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String headerTitle = ConsoleUI.BLUE_BOLD + "Manager Panel: " + getName() + " " + getSurname() + ConsoleUI.RESET;
            
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
     * <p>
     * It retrieves the last {@code User} object pushed onto the stack and executes an INSERT statement.
     * If the restoration fails, the user is pushed back onto the stack to prevent data loss.
     * </p>
     *
     * @author Zafer Mert Serinken
     */
    private void undoLastDelete() {
        if (deletedUsersStack.isEmpty()) {
            ConsoleUI.printError("Nothing to undo! Stack is empty.");
            return;
        }

        User restoredUser = deletedUsersStack.pop();
        System.out.println("Restoring user: " + restoredUser.getName() + " " + restoredUser.getSurname() + "...");

        String sql = "INSERT INTO users (username, password_hash, name, surname, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, restoredUser.getUsername());
            pstmt.setString(2, restoredUser.getPassword_hash());
            pstmt.setString(3, restoredUser.getName());
            pstmt.setString(4, restoredUser.getSurname());
            pstmt.setString(5, restoredUser.getRole());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                ConsoleUI.printInfo("UNDO SUCCESSFUL: User '" + restoredUser.getUsername() + "' is back!");
            }

        } catch (SQLException e) {
            deletedUsersStack.push(restoredUser);
            ConsoleUI.printError("Undo Failed (User pushed back to stack): " + e.getMessage());
        }
    }

    /**
     * Deletes a user from the system based on the provided User ID.
     * <p>
     * Before deletion, the user data is backed up to a Stack to enable the Undo operation.
     * Includes validation to prevent the manager from deleting their own account.
     * </p>
     *
     * @param scanner The Scanner object used to read the User ID from the console.
     * @author Zafer Mert Serinken
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

        if (targetId == getUserId()) {
            ConsoleUI.printError("CRITICAL: You cannot fire yourself!");
            return;
        }

        
        User userBackup = getUserById(targetId);
        if (userBackup == null) {
            ConsoleUI.printError("User ID not found. Cannot delete.");
            return;
        }

        System.out.print("Are you sure you want to fire " + userBackup.getName() + "? (yes/no): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) return;


        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, targetId);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
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
     * <p>
     * This is a helper method primarily used for backing up user data before deletion.
     * It uses polymorphism to instantiate the correct subclass based on the role string.
     * </p>
     *
     * @param id The unique identifier of the user to retrieve.
     * @return A User object (subclass determined by role) containing the user's data, or null if not found.
     * @author Zafer Mert Serinken
     */
    private User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
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
     * <p>
     * Displays ID, Username, Name, Surname, and Role in a formatted table output.
     * </p>
     *
     * @author Zafer Mert Serinken
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
     * <p>
     * Checks for duplicate usernames to ensure data integrity and hashes the password using SHA-256 before storage.
     * </p>
     *
     * @param scanner The Scanner object used to read user input.
     * @author Zafer Mert Serinken
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
     * Updates an existing user's information and ROLE (Promotion/Demotion).
     * <p>
     * Allows partial updates; if fields are left empty by the user, the existing database values are preserved.
     * </p>
     *
     * @param scanner The Scanner object used to read user input.
     * @author Zafer Mert Serinken
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
     * Displays comprehensive statistical information about the contacts.
     * <p>
     * Provides analytics such as total count, LinkedIn usage, age statistics (average, youngest, oldest),
     * and identifies the most frequently occurring first and last names.
     * </p>
     *
     * @author Zafer Mert Serinken
     */
    public void showContactStats() {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.BLUE_BOLD + "--- System Statistics & Analytics ---" + ConsoleUI.RESET);


        String sqlTotal = "SELECT COUNT(*) as total FROM contacts";
        String sqlLinkedin = "SELECT COUNT(*) as linked FROM contacts WHERE linkedinUrl IS NOT NULL AND linkedinUrl != ''";


        String sqlAvgAge = "SELECT AVG(TIMESTAMPDIFF(YEAR, birthdate, CURDATE())) as avg_age FROM contacts WHERE birthdate IS NOT NULL";
        String sqlYoungest = "SELECT name, surname, birthdate FROM contacts WHERE birthdate IS NOT NULL ORDER BY birthdate DESC LIMIT 1";
        String sqlOldest = "SELECT name, surname, birthdate FROM contacts WHERE birthdate IS NOT NULL ORDER BY birthdate ASC LIMIT 1";


        String sqlMostSharedName = "SELECT name, COUNT(*) as cnt FROM contacts GROUP BY name HAVING cnt > 1 ORDER BY cnt DESC LIMIT 1";
        String sqlMostSharedSurname = "SELECT surname, COUNT(*) as cnt FROM contacts GROUP BY surname HAVING cnt > 1 ORDER BY cnt DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {


            ResultSet rs = stmt.executeQuery(sqlTotal);
            if (rs.next()) System.out.println("Total Contacts: " + ConsoleUI.CYAN_BOLD + rs.getInt("total") + ConsoleUI.RESET);
            rs.close();

            rs = stmt.executeQuery(sqlLinkedin);
            if (rs.next()) System.out.println("With LinkedIn: " + ConsoleUI.CYAN_BOLD + rs.getInt("linked") + ConsoleUI.RESET);
            rs.close();

            System.out.println("--------------------------------");


            rs = stmt.executeQuery(sqlAvgAge);
            if (rs.next()) {
                double avg = rs.getDouble("avg_age");
                if (!rs.wasNull()) {
                    System.out.printf("Average Age: " + ConsoleUI.YELLOW_BOLD + "%.1f years" + ConsoleUI.RESET + "%n", avg);
                } else {
                    System.out.println("Average Age: N/A (No birth dates)");
                }
            }
            rs.close();


            rs = stmt.executeQuery(sqlYoungest);
            if (rs.next()) {
                System.out.println("Youngest Contact: " + ConsoleUI.GREEN_BOLD +
                        rs.getString("name") + " " + rs.getString("surname") +
                        " (" + rs.getDate("birthdate") + ")" + ConsoleUI.RESET);
            }
            rs.close();


            rs = stmt.executeQuery(sqlOldest);
            if (rs.next()) {
                System.out.println("Oldest Contact:   " + ConsoleUI.RED_BOLD +
                        rs.getString("name") + " " + rs.getString("surname") +
                        " (" + rs.getDate("birthdate") + ")" + ConsoleUI.RESET);
            }
            rs.close();

            System.out.println("--------------------------------");


            rs = stmt.executeQuery(sqlMostSharedName);
            if (rs.next()) {
                System.out.println("Most Shared Name: " + ConsoleUI.CYAN_BOLD + rs.getString("name") +
                        ConsoleUI.RESET + " (Shared by " + rs.getInt("cnt") + " individuals)");
            } else {
                System.out.println("Most Shared Name: None (All unique)");
            }
            rs.close();

            rs = stmt.executeQuery(sqlMostSharedSurname);
            if (rs.next()) {
                System.out.println("Most Shared Surname: " + ConsoleUI.CYAN_BOLD + rs.getString("surname") +
                        ConsoleUI.RESET + " (Shared by " + rs.getInt("cnt") + " individuals)");
            } else {
                System.out.println("Most Shared Surname: None (All unique)");
            }
            rs.close();

        } catch (SQLException e) {
            ConsoleUI.printError("Stats Error: " + e.getMessage());
        }
        ConsoleUI.pause();
    }

    /**
     * Changes the password of the currently logged-in Manager.
     * <p>
     * The new password is validated (min length) and hashed using SHA-256 before being stored.
     * </p>
     *
     * @param scanner The Scanner object used to read the new password.
     * @author Zafer Mert Serinken
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

    /**
     * Hashes a plain text password using the SHA-256 algorithm.
     * <p>
     * Provides one-way encryption for secure password storage.
     * </p>
     *
     * @param plainText The plain text password to hash.
     * @return A hexadecimal string representation of the hashed password.
     * @throws RuntimeException if the SHA-256 algorithm is not available in the environment.
     * @author Zafer Mert Serinken
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
     * <p>
     * Used during user creation to enforce unique usernames.
     * </p>
     *
     * @param username The username string to check.
     * @return {@code true} if the username exists, {@code false} otherwise.
     * @author Zafer Mert Serinken
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