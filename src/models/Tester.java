package models;

import Database.DatabaseConnection;
import util.ConsoleUI;

import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import dao.ContactSearchDAO;
import java.sql.Connection;
import java.util.List;
import Database.DatabaseConnection;
import java.sql.SQLException;

/**
 * Represents the "Tester" role within the application.
 * <p>
 * This class extends the {@link User} class and implements specific functionalities
 * tailored for testers. Testers have <b>read-only</b> access to the contact list,
 * meaning they can list, search, and sort contacts but cannot modify them.
 * However, they retain the ability to update their own passwords.
 * </p>
 * * <h3>Key Responsibilities:</h3>
 * <ul>
 * <li>Listing all contacts in a formatted table.</li>
 * <li>Sorting contacts dynamically by columns.</li>
 * <li>Changing their own account password securely.</li>
 * <li>Searching contacts via {@link ContactSearchDAO}.</li>
 * </ul>
 * * @author Arda Dulger
 * @author selcukaloba
 */

public class Tester extends User {
    private ContactSearchDAO searchDAO;

    public Tester() {
        super();
        this.setRole("Tester");

        try {

            Connection connection = DatabaseConnection.getConnection();
            this.searchDAO = new ContactSearchDAO(connection);
        } catch (Exception e) {
            System.out.println("Error:Tester failed to establish database connection!");
            e.printStackTrace();
        }
    }

    public Tester(int userId, String username, String name, String surname, Connection connection) {
        super();

        this.setUserId(userId);
        this.setUsername(username);
        this.setName(name);
        this.setSurname(surname);
        this.setRole("Tester");

        this.searchDAO = new ContactSearchDAO(connection);
    }

    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Tester Menu" + ConsoleUI.RESET,
                    new String[]{
                            "1) Change password",
                            "2) List all contacts",
                            "3) Search contacts",
                            "4) Sort contacts",
                            "",
                            "0) Logout"
                    },
                    scanner
            );

            switch (choice) {
                case "1" :
                    changePassword(scanner);
                    break;

                case "2" :
                    listAllContacts(scanner);
                    break;

                case "3" :
                    searchContacts(scanner);
                    break;

                case "4" :
                    sortContacts(scanner);
                    break;

                case "0" :
                    running = false;
                    break;

                default :
                    ConsoleUI.printInvalidChoice();
                    break;
            }
        }
    }

    /**
     * Helper method to hash passwords using SHA-256 algorithm.
     * <p>
     * This ensures that passwords are never stored or compared in plain text,
     * adhering to security best practices.
     * </p>
     * * @param plainPassword The plain text password entered by the user.
     * @return The hexadecimal string representation of the hashed password.
     * @throws RuntimeException If the SHA-256 algorithm is not available in the environment.
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
     * Allows the Tester to change their own password.
     * <p>
     * This method performs several security checks:
     * <ul>
     * <li>Verifies the old password by hashing the input and comparing it with the stored hash.</li>
     * <li>Enforces a minimum password length.</li>
     * <li>Updates the password in the database securely using hashing.</li>
     * </ul>
     * </p>
     * * @param scanner The {@link Scanner} object to receive password inputs.
     * @author selcukaloba
     */
    protected void changePassword(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Change Password ===" + ConsoleUI.RESET);

        System.out.print("Enter current password: ");
        String oldPassInput = scanner.nextLine();
        String hashedOldPass = hashPassword(oldPassInput);

        if (this.getPassword_hash() != null && !hashedOldPass.equals(this.getPassword_hash()))
        {
            ConsoleUI.printError("Incorrect current password!");
            return;
        }

        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();

        if (newPass.length() < 3)
        {
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
     * Retrieves and lists all contacts from the database.
     * <p>
     * Executes a {@code SELECT * FROM contacts} query and displays the results
     * in a tabular format using the helper method {@link #printResultSetTable(ResultSet)}.
     * </p>
     * * @param scanner The {@link Scanner} object (used for pausing the screen).
     * @author selcukaloba
     */
    protected void listAllContacts(Scanner scanner) {
        System.out.println("\n--- LIST ALL CONTACTS ---");
        String sql = "SELECT * FROM contacts";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            printResultSetTable(rs);

        } catch (SQLException e) {
            ConsoleUI.printError("Database error: " + e.getMessage());
        }
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
            System.out.println("-------------------------------------------------------------------------");
            System.out.printf("%-5s %-15s %-15s %-15s %-20s\n", "ID", "First Name", "Last Name", "Phone", "Email");
            System.out.println("-------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("contact_id");
                String name = rs.getString("first_name");
                String surname = rs.getString("last_name");
                String phone = rs.getString("phone_primary");
                String email = rs.getString("email");

                System.out.printf("%-5d %-15s %-15s %-15s %-20s\n", id, name, surname, phone, email);
            }

            if (!found) {
                System.out.println("No records found.");
            }
            System.out.println("-------------------------------------------------------------------------");
            ConsoleUI.pause();

        } catch (SQLException e) {
            ConsoleUI.printError("Error printing table: " + e.getMessage());
        }
    }

    /**
     * Performs various person search operations and allows viewing the search sub-menu.
     * <p>
     * This method allows users to perform searches without having to return to the main menu.
     * Two types of search criteria can be specified.
     * <ul>
     * <li><b>Single-Field Search:</b> Search by First Name, Last Name, or Phone Number.</li>
     * <li><b>Multi-Field Search:</b> Advanced search combinations (e.g., Name + Birth Month).</li>
     * </ul>
     * </p>
     * <p>
     * Before sending a query to the database via Dao, it performs <b>input validation</b>
     * For example, checking if the entered month is between 1-12 or if the entered phone number is a digit.
     * @param scanner {@code Scanner } object is used to receive input.
     * @author Arda Dulger
     */
    protected void searchContacts(Scanner scanner) {
        boolean searching = true;
        while (searching) {
            ConsoleUI.clearConsole();

            System.out.println(ConsoleUI.YELLOW_BOLD + "=== Search Contacts (Tester) ===" + ConsoleUI.RESET);
            System.out.println("1) Search by First Name (Partial)");
            System.out.println("2) Search by Last Name (Partial)");
            System.out.println("3) Search by Phone Number (Validated)");
            System.out.println("4) [Multi] Name AND Birth Month");
            System.out.println("5) [Multi] Lastname AND City/Address");
            System.out.println("6) [Multi] Phone Part AND Email Part");
            System.out.println("0) Back to Tester Menu");
            System.out.print("Select search type: ");

            String choice = scanner.nextLine();
            List<Contact> results = null;

            switch (choice) {
                case "1":

                    System.out.print("Enter first name (or part of it): ");
                    String firstNameQuery = scanner.nextLine();
                    results = searchDAO.searchByFirstName(firstNameQuery);
                    break;

                case "2":

                    System.out.print("Enter last name (or part of it): ");
                    String lastNameQuery = scanner.nextLine();
                    results = searchDAO.searchByLastName(lastNameQuery);
                    break;

                case "3":

                    System.out.print("Enter phone number (digits only): ");
                    String phone = scanner.nextLine();
                    if (searchDAO.isValidPhoneNumber(phone)) {
                        results = searchDAO.searchByPhoneNumber(phone);
                    } else {
                        System.out.println("Invalid format! Phone should contain only digits/spaces." );
                    }
                    break;

                case "4":

                    System.out.print("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter birth month (1-12): ");
                    try {
                        int month = Integer.parseInt(scanner.nextLine());
                        if (searchDAO.isValidMonth(month)) {
                            results = searchDAO.searchByNameAndBirthMonth(name, month);
                        } else {
                            System.out.println("Invalid month! Please enter between 1-12.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input! Please enter a number for month.");
                    }
                    break;

                case "5":

                    System.out.print("Enter lastname: ");
                    String lname = scanner.nextLine();
                    System.out.print("Enter city/address part: ");
                    String city = scanner.nextLine();
                    results = searchDAO.searchByLastnameAndCity(lname, city);
                    break;

                case "6":

                    System.out.print("Enter phone part (e.g. 555): ");
                    String phonePart = scanner.nextLine();
                    System.out.print("Enter email part (e.g. gmail): ");
                    String emailPart = scanner.nextLine();
                    results = searchDAO.searchByPhonePartAndEmailPart(phonePart, emailPart);
                    break;

                case "0":
                    searching = false;
                    break;

                default:
                    ConsoleUI.printInvalidChoice();
                    break;
            }

            //
            if (results != null) {
                if (results.isEmpty()) {
                    System.out.println("No contacts found matching criteria.");
                } else {
                    System.out.println("Found " + results.size() + " record(s):");
                    printSearchResults(results);
                }
                ConsoleUI.pause();
            } else if (!choice.equals("0")) {

                ConsoleUI.pause();
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
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%-5s %-15s %-15s %-15s %-20s\n", "ID", "First Name", "Last Name", "Phone", "Email");
        System.out.println("-------------------------------------------------------------------------");
        for (Contact c : contacts) {
            System.out.printf("%-5d %-15s %-15s %-15s %-20s\n",
                    c.getContactId(), c.getName(), c.getSurname(), c.getPrimaryPhone(), c.getEmail());
        }
        System.out.println("-------------------------------------------------------------------------");
    }
    protected void sortContacts(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Sort Contacts ===" + ConsoleUI.RESET);

        System.out.println("Sort by:");
        System.out.println("1. First Name");
        System.out.println("2. Last Name");
        System.out.println("3. Phone Number");
        System.out.print("Choice: ");
        String colChoice = scanner.nextLine().trim();

        String orderByColumn = "first_name";
        switch (colChoice) {
            case "1": orderByColumn = "first_name"; break;
            case "2": orderByColumn = "last_name"; break;
            case "3": orderByColumn = "phone_primary"; break;
            default:
                ConsoleUI.printError("Invalid column! Defaulting to First Name.");
        }

        System.out.println("Direction:");
        System.out.println("1. Ascending (A-Z)");
        System.out.println("2. Descending (Z-A)");
        System.out.print("Choice: ");
        String dirChoice = scanner.nextLine().trim();

        String direction = "ASC";
        if (dirChoice.equals("2")) {
            direction = "DESC";
        }

        String sql = "SELECT * FROM contacts ORDER BY " + orderByColumn + " " + direction;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("Sorting by " + orderByColumn + " (" + direction + ")...");
            printResultSetTable(rs);

        } catch (SQLException e) {
            ConsoleUI.printError("Database error: " + e.getMessage());
        }
    }
}
