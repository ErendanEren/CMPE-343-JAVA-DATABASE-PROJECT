package models;

import Database.DatabaseConnection;
import dao.ContactSearchDAO;
import util.ConsoleUI;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Represents the "Tester" role within the application.
 * <p>
 * This class extends the {@link User} class and implements specific functionalities
 * tailored for testers. Testers have <b>read-only</b> access to the contact list,
 * meaning they can list, search, and sort contacts but cannot modify them.
 * However, they retain the ability to update their own passwords.
 * </p>
 * <h3>Key Responsibilities:</h3>
 * <ul>
 * <li>Listing all contacts in a <b>paginated card view</b>.</li>
 * <li>Sorting contacts dynamically by columns (including Middle Name, Secondary Phone, LinkedIn).</li>
 * <li>Changing their own account password securely.</li>
 * <li>Searching contacts via {@link ContactSearchDAO} using sectioned menus.</li>
 * </ul>
 *
 * @author selcukaloba
 * @author dulgerarda
 */
public class Tester extends User {

    private final ContactSearchDAO searchDAO;

    /**
     * Default constructor for Tester.
     * <p>
     * Initializes the role as "Tester" and establishes a database connection
     * for the search DAO.
     * </p>
     * @author selcukaloba
     * @author dulgerarda
     */
    public Tester() {
        super();
        this.setRole("Tester");
        this.searchDAO = new ContactSearchDAO();
    }

    /**
     * Parameterized constructor for creating a Tester object with specific details.
     *
     * @param userId   The unique ID of the user.
     * @param username The username for login.
     * @param name     The first name of the tester.
     * @param surname  The last name of the tester.
     */
    public Tester(int userId, String username, String name, String surname) {
        super();
        this.setUserId(userId);
        this.setUsername(username);
        this.setName(name);
        this.setSurname(surname);
        this.setRole("Tester");
        this.searchDAO = new ContactSearchDAO();
    }

    /**
     * Displays the main interactive menu for the Tester.
     * <p>
     * Handles user input to navigate between Listing, Searching, Sorting, and Password Management.
     * </p>
     *
     * @param scanner The {@link Scanner} object used to capture user input.
     * @author Eren Çakır Bircan
     */
    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            String fullName = (getName() != null) ? getName() + " " + getSurname() : getUsername();
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Tester Panel: " + fullName + ConsoleUI.RESET,
                    new String[]{
                            "1) Change password",
                            "2) List all contacts (Paginated)",
                            "3) Search contacts (Full)",
                            "4) Sort contacts (Full)",
                            "",
                            "0) Logout"
                    },
                    scanner
            );

            switch (choice) {
                case "1" -> changePassword(scanner);
                case "2" -> listAllContacts(scanner);
                case "3" -> searchContacts(scanner);
                case "4" -> sortContacts(scanner);
                case "0" -> running = false;
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }


    /**
     * Helper method to hash passwords using SHA-256 algorithm.
     *
     * @param plainPassword The plain text password entered by the user.
     * @return The hexadecimal string representation of the hashed password.
     * @throws RuntimeException If the SHA-256 algorithm is not available.
     * @author selcukaloba
     */
    private String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Hashing error", ex);
        }
    }

    /**
     * Allows the Tester to change their own password securely.
     * <p>
     * Validates the old password (using hash comparison) and updates the database
     * with the new hashed password.
     * </p>
     *
     * @param scanner The {@link Scanner} object to receive password inputs.
     * @author selcukaloba
     */
    protected void changePassword(Scanner scanner) {
        try {
            ConsoleUI.printSectionHeader("CHANGE PASSWORD");
        } catch (Exception e) {
            System.out.println("--- CHANGE PASSWORD ---");
        }

        System.out.print("Enter current password: ");
        String oldPassInput = scanner.nextLine();
        String hashedOldPass = hashPassword(oldPassInput);

        if (this.getPassword_hash() != null && !hashedOldPass.equals(this.getPassword_hash())) {
            ConsoleUI.printError("Incorrect current password!");
            return;
        }

        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();

        if (newPass.length() < 3) {
            ConsoleUI.printError("Password is too short!");
            return;
        }

        String hashedNewPass = hashPassword(newPass);
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedNewPass);
            ps.setInt(2, this.getUserId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                this.setPassword_hash(hashedNewPass);
                ConsoleUI.printInfo("Password updated successfully!");
            } else {
                ConsoleUI.printError("Failed to update password.");
            }
        } catch (SQLException e) {
            ConsoleUI.printError("Database error: " + e.getMessage());
        }
    }

    /**
     * Retrieves all contacts from the database and displays them in a paginated view.
     *
     * @param scanner The {@link Scanner} object used for navigation input.
     * @author selcukaloba
     */
    protected void listAllContacts(Scanner scanner) {
        String sql = "SELECT * FROM contacts";
        List<Contact> allContacts = fetchContactsFromQuery(sql);
        if (allContacts.isEmpty()) {
            ConsoleUI.printError("No contacts found in database.");
            return;
        }
        ConsoleUI.showPaginatedContactView(allContacts, scanner, "ALL CONTACTS");
    }

    /**
     * Handles the advanced search functionality with a sectioned menu.
     * <p>
     * Supports 8 different search criteria including single-field and multi-field searches.
     * Uses {@link ContactSearchDAO} for database operations.
     * </p>
     *
     * @param scanner The {@link Scanner} object to read search queries.
     * @author dulgerarda
     */
    protected void searchContacts(Scanner scanner) {
        boolean searching = true;
        while (searching) {
            ConsoleUI.clearConsole();

            String[][] sections = {
                    {
                            "1) Search by First Name",
                            "2) Search by Middle Name",
                            "3) Search by Last Name",
                            "4) Search by Primary Phone",
                            "5) Search by Secondary Phone"
                    },
                    {
                            "6) Name AND Birth Month",
                            "7) Lastname AND City",
                            "8) Phone Part AND Email Part"
                    },
                    {
                            ConsoleUI.RED_BOLD + "0) Back to Tester Menu" + ConsoleUI.RESET
                    }
            };
            String[] titles = {"Single Field", "Multi-Field", ""};

            try {
                ConsoleUI.printSectionedMenu("SEARCH CONTACTS", sections, titles);
            } catch (Exception e) {
                System.out.println("=== SEARCH CONTACTS ===");
            }

            System.out.print(ConsoleUI.CYAN_BOLD + "▶ " + ConsoleUI.RESET + "Select search type: ");
            String choice = scanner.nextLine();
            List<Contact> results = null;

            switch (choice) {
                case "1" -> {
                    System.out.print("Enter first name: ");
                    String name = scanner.nextLine();
                    if (!searchDAO.isValidName(name)) {
                        ConsoleUI.printError("Invalid format! Name must contain only letters.");
                    } else {
                        results = searchDAO.searchByFirstName(name);
                    }
                }
                case "2" -> {
                    System.out.print("Enter middle name: ");
                    String name = scanner.nextLine();
                    if (!searchDAO.isValidName(name)) {
                        ConsoleUI.printError("Invalid format! Name must contain only letters.");
                    } else {
                        results = searchDAO.searchByMiddleName(name);
                    }
                }
                case "3" -> {
                    System.out.print("Enter last name: ");
                    String name = scanner.nextLine();
                    if (!searchDAO.isValidName(name)) {
                        ConsoleUI.printError("Invalid format! Name must contain only letters.");
                    } else {
                        results = searchDAO.searchByLastName(name);
                    }
                }
                case "4" -> {
                    System.out.print("Enter primary phone: ");
                    String ph = scanner.nextLine();
                    if (searchDAO.isValidPhoneNumber(ph)) {
                        results = searchDAO.searchByPhoneNumber(ph);
                    } else {
                        ConsoleUI.printError("Invalid format! Phone must contain digits only.");
                    }
                }
                case "5" -> {
                    System.out.print("Enter secondary phone: ");
                    String ph = scanner.nextLine();
                    if (searchDAO.isValidPhoneNumber(ph)) {
                        results = searchDAO.searchBySecondaryPhoneNumber(ph);
                    } else {
                        ConsoleUI.printError("Invalid format! Phone must contain digits only.");
                    }
                }
                case "6" -> {
                    System.out.print("Name: ");
                    String n = scanner.nextLine();
                    if (!searchDAO.isValidName(n)) {
                        ConsoleUI.printError("Invalid format! Name must contain only letters.");
                    } else {
                        System.out.print("Month (1-12): ");
                        try {
                            int m = Integer.parseInt(scanner.nextLine());
                            if (searchDAO.isValidMonth(m)) {
                                results = searchDAO.searchByNameAndBirthMonth(n, m);
                            } else {
                                ConsoleUI.printError("Invalid month!");
                            }
                        } catch (NumberFormatException e) {
                            ConsoleUI.printError("Invalid input! Please enter a number for month.");
                        }
                    }
                }
                case "7" -> {
                    System.out.print("Lastname: ");
                    String l = scanner.nextLine();
                    System.out.print("City: ");
                    String c = scanner.nextLine();

                    if (!searchDAO.isValidName(l)) {
                        ConsoleUI.printError("Invalid format! Lastname must contain only letters.");
                    } else if (!searchDAO.isValidName(c)) {
                        ConsoleUI.printError("Invalid format! City must contain only letters.");
                    } else {
                        results = searchDAO.searchByLastnameAndCity(l, c);
                    }
                }
                case "8" -> {
                    System.out.print("Phone part: ");
                    String p = scanner.nextLine();
                    System.out.print("Email part: ");
                    String e = scanner.nextLine();

                    if (!searchDAO.isValidPhoneNumber(p)) {
                        ConsoleUI.printError("Invalid format! Phone part must be numeric.");
                    } else {
                        results = searchDAO.searchByPhonePartAndEmailPart(p, e);
                    }
                }
                case "0" -> searching = false;
                default -> ConsoleUI.printInvalidChoice();
            }

            if (results != null) {
                if (results.isEmpty()) {
                    ConsoleUI.printError("No contacts found.");
                } else {
                    ConsoleUI.showPaginatedContactView(results, scanner, "SEARCH RESULTS");
                }
            } else if (!"0".equals(choice)) {
                ConsoleUI.pause();
            }
        }
    }

    /**
     * Handles the advanced sorting functionality.
     * <p>
     * Allows sorting by 9 different columns including Middle Name, Secondary Phone, and LinkedIn.
     * Supports both Ascending and Descending order.
     * </p>
     *
     * @param scanner The {@link Scanner} object to read sort preferences.
     * @author selcukaloba
     */
    protected void sortContacts(Scanner scanner) {
        try {
            ConsoleUI.printSectionHeader("SORT CONTACTS");
        } catch (Exception e) {
            System.out.println("=== SORT CONTACTS ===");
        }

        System.out.println("1. First Name");
        System.out.println("2. Last Name");
        System.out.println("3. Primary Phone");
        System.out.println("4. Birth Date");
        System.out.println("5. City/State");
        System.out.println("6. Nickname");
        System.out.println("7. Middle Name");
        System.out.println("8. Secondary Phone");
        System.out.println("9. LinkedIn URL");

        System.out.print(ConsoleUI.CYAN_BOLD + "▶ " + ConsoleUI.RESET + "Choice: ");
        String colChoice = scanner.nextLine().trim();

        String orderByColumn = "first_name";
        String sortLabel = "First Name";

        switch (colChoice) {
            case "1" -> { orderByColumn = "first_name"; sortLabel = "First Name"; }
            case "2" -> { orderByColumn = "last_name"; sortLabel = "Last Name"; }
            case "3" -> { orderByColumn = "phone_primary"; sortLabel = "Primary Phone"; }
            case "4" -> { orderByColumn = "birthdate"; sortLabel = "Birth Date"; }
            case "5" -> { orderByColumn = "TRIM(SUBSTRING_INDEX(address, ',', -1))"; sortLabel = "City"; }
            case "6" -> { orderByColumn = "nickname"; sortLabel = "Nickname"; }
            case "7" -> { orderByColumn = "middle_name"; sortLabel = "Middle Name"; }
            case "8" -> { orderByColumn = "phone_secondary"; sortLabel = "Secondary Phone"; }
            case "9" -> { orderByColumn = "linkedin_url"; sortLabel = "LinkedIn"; }
            default -> ConsoleUI.printError("Invalid column! Defaulting to First Name.");
        }

        System.out.println("\nDirection:");
        System.out.println("1. Ascending (A-Z)");
        System.out.println("2. Descending (Z-A)");
        System.out.print(ConsoleUI.CYAN_BOLD + "▶ " + ConsoleUI.RESET + "Choice: ");
        String dir = scanner.nextLine().trim().equals("2") ? "DESC" : "ASC";

        String sql = "SELECT * FROM contacts ORDER BY " + orderByColumn + " " + dir;
        List<Contact> sorted = fetchContactsFromQuery(sql);

        if (sorted.isEmpty()) {
            ConsoleUI.printError("No contacts found.");
        } else {
            ConsoleUI.showPaginatedContactView(sorted, scanner, "SORTED BY: " + sortLabel + " (" + dir + ")");
        }
    }


    /**
     * Helper method to execute a SELECT query and map the ResultSet to a List of Contact objects.
     * <p>
     * Fetches all available columns including optional ones like Middle Name, Nickname, and LinkedIn.
     * </p>
     *
     * @param sql The SQL query string to execute.
     * @return A {@code List<Contact>} containing the results.
     * @author dulgerarda
     * @author selcukaloba
     */
    private List<Contact> fetchContactsFromQuery(String sql) {
        List<Contact> contacts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Contact c = new Contact();
                c.setContactId(rs.getInt("contact_id"));
                c.setName(rs.getString("first_name"));
                c.setSurname(rs.getString("last_name"));
                c.setPrimaryPhone(rs.getString("phone_primary"));
                c.setAddress(rs.getString("address"));
                c.setEmail(rs.getString("email"));

                try { c.setMiddleName(rs.getString("middle_name")); } catch (Exception e) {}
                try { c.setNickname(rs.getString("nickname")); } catch (Exception e) {}
                try { c.setSecondaryPhone(rs.getString("phone_secondary")); } catch (Exception e) {}
                try { c.setLinkedinUrl(rs.getString("linkedin_url")); } catch (Exception e) {}

                try { c.setBirthdate(rs.getDate("birthdate")); }
                catch (SQLException e) { try { c.setBirthdate(rs.getDate("birth_date")); } catch(Exception ex){} }

                contacts.add(c);
            }
        } catch (SQLException e) {
            ConsoleUI.printError("Database error: " + e.getMessage());
        }
        return contacts;
    }
}