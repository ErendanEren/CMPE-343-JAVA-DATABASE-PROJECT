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
 *   <li>Listing all contacts in a <b>paginated card view</b>.</li>
 *   <li>Sorting contacts dynamically by columns (paginated view).</li>
 *   <li>Changing their own account password securely.</li>
 *   <li>Searching contacts via {@link ContactSearchDAO} (paginated view).</li>
 * </ul>
 */
public class Tester extends User {

    private final ContactSearchDAO searchDAO;

    public Tester() {
        super();
        this.setRole("Tester");
        this.searchDAO = new ContactSearchDAO();
    }

    public Tester(int userId, String username, String name, String surname) {
        super();

        this.setUserId(userId);
        this.setUsername(username);
        this.setName(name);
        this.setSurname(surname);
        this.setRole("Tester");

        this.searchDAO = new ContactSearchDAO();
    }

    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Tester Panel: " + getName() + " " + getSurname() + ConsoleUI.RESET,
                    new String[]{
                            "1) Change password",
                            "2) List all contacts (Paginated Cards)",
                            "3) Search contacts (Paginated Cards)",
                            "4) Sort contacts (Paginated Cards)",
                            "",
                            "0) Logout"
                    },
                    scanner
            );

            switch (choice) {
                case "1" -> changePassword(scanner);
                case "2" -> listAllContacts(scanner);        // wrapper: paginated
                case "3" -> searchContacts(scanner);         // paginated
                case "4" -> sortContacts(scanner);           // paginated
                case "0" -> running = false;
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    // =========================================================
    // PASSWORD
    // =========================================================

    /**
     * Helper method to hash passwords using SHA-256 algorithm.
     *
     * @param plainPassword The plain text password entered by the user.
     * @return The hexadecimal string representation of the hashed password.
     * @throws RuntimeException If the SHA-256 algorithm is not available.
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
     * Allows the Tester to change their own password.
     *
     * @param scanner The {@link Scanner} object to receive password inputs.
     */
    protected void changePassword(Scanner scanner) {
        ConsoleUI.printSectionHeader("CHANGE PASSWORD");

        System.out.print(ConsoleUI.CYAN_BOLD + "▶ " + ConsoleUI.RESET + "Enter current password: ");
        String oldPassInput = scanner.nextLine();
        String hashedOldPass = hashPassword(oldPassInput);

        if (this.getPassword_hash() != null && !hashedOldPass.equals(this.getPassword_hash())) {
            ConsoleUI.printError("Incorrect current password!");
            return;
        }

        System.out.print(ConsoleUI.CYAN_BOLD + "▶ " + ConsoleUI.RESET + "Enter new password: ");
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

    // =========================================================
    // LIST ALL CONTACTS  (PAGINATED CARDS)
    // =========================================================

    /**
     * Wrapper kept for backward compatibility.
     * Uses paginated card-based viewer instead of plain table.
     */
    protected void listAllContacts(Scanner scanner) {
        listAllContactsPaginated(scanner);
    }

    /**
     * Retrieves all contacts and shows them in paginated card view.
     */
    protected void listAllContactsPaginated(Scanner scanner) {
        String sql = "SELECT * FROM contacts";
        List<Contact> allContacts = fetchContactsFromQuery(sql);

        if (allContacts.isEmpty()) {
            ConsoleUI.printError("No contacts found in database.");
            return;
        }

        ConsoleUI.showPaginatedContactView(allContacts, scanner, "ALL CONTACTS");
    }
    /**
     *Iterates over the SQL{@code ResultSet} and displays the contact information in a format of table.
     *<p>
     *The main purpose of this helper is to be used in listing and sorting operations.
     *It prints the following columns: <b>ID, First Name, Last Name, Phone, and Email</b>.
     *</p>
     *<p>
     *The method handles iteration internally and catches any {@link SQLException} errors
     *which can occur during data retrieval.
     *</p>
     *
     *@param rs The {@link ResultSet} object obtained from executing a SQL query.
     *If {@code null} or empty, appropriate messages are displayed.
     * @author Arda Dülger
     */
    private void printResultSetTable(ResultSet rs) {
        try {

            String line = "----------------------------------------------------------------------------------------------------------------------------------------------------------------";

            System.out.println(ConsoleUI.LIGHT_GRAY + line + ConsoleUI.RESET);
            System.out.printf(
                    ConsoleUI.YELLOW_BOLD +
                            "%-4s %-20s %-15s %-10s %-12s %-12s %-12s %-25s %-25s%n" +
                            ConsoleUI.RESET,
                    "ID", "First (+Mid) Name", "Last Name", "Nickname", "Pri. Phone", "Sec. Phone", "Birthdate", "Address", "Email"
            );
            System.out.println(ConsoleUI.LIGHT_GRAY + line + ConsoleUI.RESET);

            boolean found = false;
            while (rs.next()) {
                found = true;

                int id = rs.getInt("contact_id");
                String first = rs.getString("first_name");
                String middle = rs.getString("middle_name");
                String last = rs.getString("last_name");
                String nick = rs.getString("nickname");
                String phone = rs.getString("phone_primary");
                String secPhone = rs.getString("phone_secondary");

                Date birth = null;
                try { birth = rs.getDate("birthdate"); } catch (SQLException e) { }

                String address = rs.getString("address");
                String email = rs.getString("email");


                String fullName = first + (middle != null && !middle.isEmpty() ? " " + middle : "");

                if (nick == null) nick = "-";
                if (address == null) address = "-";
                if (secPhone == null || secPhone.isEmpty()) secPhone = "-";

                String birthStr = (birth == null) ? "-" : birth.toString();

                System.out.printf(
                        "%-4d %-20s %-15s %-10s %-12s %-12s %-12s %-25s %-25s%n",
                        id, fullName, last, nick, phone, secPhone, birthStr, address, email
                );
            }

            if (!found) {
                System.out.println(ConsoleUI.RED_BOLD + "No records found." + ConsoleUI.RESET);
            }
            System.out.println(ConsoleUI.LIGHT_GRAY + line + ConsoleUI.RESET);

        } catch (SQLException e) {
            ConsoleUI.printError("Error printing table: " + e.getMessage());
        }
    }

    /**
     * Performs various contact search operations and shows results as paginated cards.
     */
    protected void searchContacts(Scanner scanner) {
        boolean searching = true;

        while (searching) {
            ConsoleUI.clearConsole();

            System.out.println(ConsoleUI.YELLOW_BOLD + "=== Search Contacts (Tester) ===" + ConsoleUI.RESET);
            System.out.println("1) Search by First(+Mid) Name (Partial)");
            System.out.println("2) Search by Last Name (Partial)");
            System.out.println("3) Search by Phone Number (Validated)");
            System.out.println("4) [Multi] Name AND Birth Month");
            System.out.println("5) [Multi] Lastname AND City");
            System.out.println("6) [Multi] Phone Part AND Email Part");
            System.out.println("0) Back to Tester Menu");
            System.out.print("Select search type: ");

            String choice = scanner.nextLine();
            List<Contact> results = null;

            switch (choice) {
                case "1" -> {
                    System.out.print("Enter first name (or part of it or middle name): ");
                    String NameQuery = scanner.nextLine();
                    results = searchDAO.searchByFirstOrMiddleName(NameQuery);
                }
                case "2" -> {
                    System.out.print("Enter last name (or part of it): ");
                    String lastNameQuery = scanner.nextLine();
                    results = searchDAO.searchByLastName(lastNameQuery);
                }
                case "3" -> {
                    System.out.print("Enter phone number (digits only): ");
                    String phone = scanner.nextLine();
                    if (searchDAO.isValidPhoneNumber(phone)) {
                        results = searchDAO.searchByPhoneNumber(phone);
                    } else {
                        System.out.println(ConsoleUI.RED_BOLD +
                                "Invalid format! Phone should contain only digits/spaces." +
                                ConsoleUI.RESET);
                    }
                }
                case "4" -> {
                    System.out.print("Enter name(First or Middle): ");
                    String name = scanner.nextLine();
                    System.out.print("Enter birth month (1-12): ");
                    try {
                        int month = Integer.parseInt(scanner.nextLine());
                        if (searchDAO.isValidMonth(month)) {
                            results = searchDAO.searchByNameAndBirthMonth(name, month);
                        } else {
                            System.out.println(ConsoleUI.RED_BOLD +
                                    "Invalid month! Please enter between 1-12." +
                                    ConsoleUI.RESET);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleUI.RED_BOLD +
                                "Invalid input! Please enter a number for month." +
                                ConsoleUI.RESET);
                    }
                }
                case "5" -> {
                    System.out.print("Enter lastname: ");
                    String lname = scanner.nextLine();
                    System.out.print("Enter city part: ");
                    String city = scanner.nextLine();
                    results = searchDAO.searchByLastnameAndCity(lname, city);
                }
                case "6" -> {
                    System.out.print("Enter phone part (e.g. 555): ");
                    String phonePart = scanner.nextLine();
                    System.out.print("Enter email part (e.g. gmail): ");
                    String emailPart = scanner.nextLine();
                    results = searchDAO.searchByPhonePartAndEmailPart(phonePart, emailPart);
                }
                case "0" -> searching = false;
                default -> ConsoleUI.printInvalidChoice();
            }

            if (results != null) {
                if (results.isEmpty()) {
                    System.out.println(ConsoleUI.YELLOW_BOLD +
                            "No contacts found matching criteria." +
                            ConsoleUI.RESET);
                    ConsoleUI.pause();
                } else {
                    System.out.println(ConsoleUI.GREEN_BOLD +
                            "Found " + results.size() + " record(s)." +
                            ConsoleUI.RESET);
                    System.out.println();

                    // TABLO YERİNE PAGİNATİON KULLANIYORUZ
                    ConsoleUI.showPaginatedContactView(results, scanner, "SEARCH RESULTS");
                }
            }
        }
    }
    /**
     * Prints the list of found people in tabular form.
     * <p>
     * This helper allows to iterate and display the contact list.
     * key attributes (ID, Name, Surname, Phone, Email) in arranged columns using {@code printf}.
     * </p>
     * @param contacts A {@code List} of {@link Contact} objects retrieved from the database.
     * If the list empty, it will be not printed.(handled by caller)
     * @author Arda Dulger
     */
    private void printSearchResults(List<Contact> contacts) {
        String line = "--------------------------------------------------------------------------------------------------------------";

        System.out.println(ConsoleUI.LIGHT_GRAY + line + ConsoleUI.RESET);
        System.out.printf(
                ConsoleUI.YELLOW_BOLD +
                        "%-4s %-22s %-15s %-15s %-12s %-25s %-25s%n" +
                        ConsoleUI.RESET,
                "ID", "First(+Mid) Name", "Last Name", "Phone", "Birthdate", "Address", "Email"
        );
        System.out.println(ConsoleUI.LIGHT_GRAY + line + ConsoleUI.RESET);

        if (contacts.isEmpty()) {
            System.out.println(ConsoleUI.RED_BOLD + "No records found." + ConsoleUI.RESET);
        } else {
            for (Contact c : contacts) {
                String birthStr = (c.getBirthdate() == null) ? "-" : c.getBirthdate().toString();
                String firstName = c.getName();
                String middleName = c.getMiddleName();
                String fullName = firstName + (middleName != null && !middleName.isEmpty() ? " " + middleName : "");
                System.out.printf(
                        "%-4d %-22s %-15s %-15s %-12s %-25s %-25s%n",
                        c.getContactId(),
                        fullName,
                        c.getSurname(),
                        c.getPrimaryPhone(),
                        birthStr,
                        c.getAddress(),
                        c.getEmail()
                );
            }
        }

    // =========================================================
    // SORT CONTACTS  (PAGINATED CARDS)
    // =========================================================

    /**
     * Sorts and displays contacts based on user-selected criteria using paginated card view.
     */
    protected void sortContacts(Scanner scanner) {
        ConsoleUI.printSectionHeader("SORT CONTACTS");

        System.out.println("Sort by:");
        System.out.println("1. First Name");
        System.out.println("2. Last Name");
        System.out.println("3. Phone Number");
        System.out.println("4. Birth Date");
        System.out.println("5. City/State (from Address)");
        System.out.println("6. Nickname");
        System.out.print("Choice: ");
        String colChoice = scanner.nextLine().trim();

        String orderByColumn = "first_name";
        String sortLabel = "First Name";
        switch (colChoice) {
            case "1" -> orderByColumn = "first_name";
            case "2" -> orderByColumn = "last_name";
            case "3" -> orderByColumn = "phone_primary";
            case "4" -> orderByColumn = "birthdate";
            case "5" -> orderByColumn = "TRIM(SUBSTRING_INDEX(address, ',', -1))";
            case "6" -> orderByColumn = "nickname";
            default -> ConsoleUI.printError("Invalid column! Defaulting to First Name.");
        }

        System.out.println("Direction:");
        System.out.println("1. Ascending (A-Z / Oldest first)");
        System.out.println("2. Descending (Z-A / Newest first)");
        System.out.print("Choice: ");
        String dirChoice = scanner.nextLine().trim();

        String direction = "ASC";
        String dirLabel = "Ascending";
        if (dirChoice.equals("2")) {
            direction = "DESC";
            dirLabel = "Descending";
        }

        String sql = "SELECT * FROM contacts ORDER BY " + orderByColumn + " " + direction;
        List<Contact> sortedContacts = fetchContactsFromQuery(sql);

        if (sortedContacts.isEmpty()) {
            ConsoleUI.printError("No contacts found.");
            return;
        }

        ConsoleUI.showPaginatedContactView(
                sortedContacts,
                scanner,
                "SORTED BY: " + sortLabel + " (" + dirLabel + ")"
        );
    }

    // =========================================================
    // HELPER: FETCH CONTACTS INTO LIST<Contact>
    // =========================================================

    /**
     * Helper method to execute a SELECT query on contacts and map rows into a List&lt;Contact&gt;.
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
                c.setSecondaryPhone(rs.getString("phone_secondary"));
                c.setBirthdate(rs.getDate("birthdate"));
                c.setAddress(rs.getString("address"));
                c.setEmail(rs.getString("email"));
                c.setLinkedinUrl(rs.getString("linkedin_url"));
                contacts.add(c);
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Database error: " + e.getMessage());
        }

        return contacts;
    }
}
