package models;

import Database.DatabaseConnection;
import util.ConsoleUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.Stack;

/**
 * Represents the "Senior Developer" role.
 * <p>
 * Inherits from {@link Junior} (and thus {@link Tester}) and adds:
 * <ul>
 *     <li>Ability to add new contacts</li>
 *     <li>Ability to delete contacts</li>
 *     <li>Ability to undo the last delete operation</li>
 * </ul>
 * </p>
 *
 * @author Eren Çakır Bircan
 */
public class Senior extends Junior {

    /**
     * Stack used to store snapshots of deleted contacts
     * so that they can be restored with the "Undo" feature.
     */
    private final Stack<ContactSnapshot> deletedContactsStack = new Stack<>();

    /**
     * Snapshot of a contact, used for undoing deletions.
     * <p>
     * Contains all fields that need to be restored in the
     * {@code contacts} table.
     * </p>
     *
     * @author Eren Çakır Bircan
     */
    private static class ContactSnapshot {
        int contactId;
        String firstName;
        String middleName;
        String lastName;
        String nickname;
        String phonePrimary;
        String phoneSecondary;
        String email;
        String linkedinUrl;
        String address;
        java.sql.Date birthdate;
    }

    /**
     * Default constructor for the Senior role.
     * <p>
     * Calls the parent constructor and sets the role name
     * to {@code "Senior Developer"}.
     * </p>
     *
     * @author Eren Çakır Bircan
     */
    public Senior() {
        super();
        this.setRole("Senior Developer");
    }

    /**
     * Constructor for creating a Senior with basic user information.
     *
     * @param userId    ID of the user
     * @param username  username of the user
     * @param name      first name of the user
     * @param surname   last name of the user
     * @param connection database connection (not stored; included for compatibility)
     *
     * @author Eren Çakır Bircan
     */
    public Senior(int userId, String username, String name, String surname, Connection connection) {
        super(userId, username, name, surname);
        this.setRole("Senior Developer");
    }

    /**
     * Shows the main menu for the Senior Developer.
     * <p>
     * Options include:
     * <ul>
     *     <li>Change password</li>
     *     <li>List / search / sort contacts</li>
     *     <li>Update existing contact</li>
     *     <li>Add new contact</li>
     *     <li>Delete contact</li>
     *     <li>Undo last delete</li>
     * </ul>
     * The loop continues until the user selects "Logout".
     * </p>
     *
     * @param scanner scanner used to read user input from the console
     *
     * @author Eren Çakır Bircan
     */
    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Senior Panel: " + getName() + " " + getSurname() + ConsoleUI.RESET,
                    new String[]{
                            "1) Change password",
                            "2) List all contacts",
                            "3) Search contacts",
                            "4) Sort contacts",
                            "5) Update existing contact",
                            "6) Add new contact",
                            "7) Delete contact",
                            "8) Undo last delete",
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
                case "5" -> updateContact(scanner);
                case "6" -> addContact(scanner);
                case "7" -> deleteContact(scanner);
                case "8" -> undoLastDelete();
                case "0" -> {
                    running = false;
                    ConsoleUI.printInfo("Logging out...");
                }
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    /**
     * Adds a new contact into the {@code contacts} table.
     * <p>
     * Fields:
     * <ul>
     *     <li>First name (required)</li>
     *     <li>Middle name (optional)</li>
     *     <li>Last name (required)</li>
     *     <li>Nickname (optional)</li>
     *     <li>Primary phone (required)</li>
     *     <li>Secondary phone (optional)</li>
     *     <li>Email (optional)</li>
     *     <li>LinkedIn URL (optional)</li>
     *     <li>Address (optional)</li>
     *     <li>Birthdate (optional, YYYY-MM-DD)</li>
     * </ul>
     * If required fields are missing or the date format is invalid,
     * the contact is not added.
     * </p>
     *
     * @param scanner scanner used to read input from the console
     *
     * @author Eren Çakır Bircan
     */
    public void addContact(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Add New Contact (Senior) ===" + ConsoleUI.RESET);

        String firstName;
        while (true) {
            System.out.print("First Name: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println(ConsoleUI.RED_BOLD +
                        "First name is required and must contain only letters." +
                        ConsoleUI.RESET);
                continue;
            }
            if (isValidName(input)) {
                firstName = input;
                break;
            }
            System.out.println(ConsoleUI.RED_BOLD +
                    "First name can contain only letters and spaces." +
                    ConsoleUI.RESET);
        }

        String middleName;
        while (true) {
            System.out.print("Middle Name (optional): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                middleName = "";
                break;
            }
            if (isValidName(input)) {
                middleName = input;
                break;
            }
            System.out.println(ConsoleUI.RED_BOLD +
                    "Middle name can contain only letters and spaces." +
                    ConsoleUI.RESET);
        }

        String lastName;
        while (true) {
            System.out.print("Last Name: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println(ConsoleUI.RED_BOLD +
                        "Last name is required and must contain only letters." +
                        ConsoleUI.RESET);
                continue;
            }
            if (isValidName(input)) {
                lastName = input;
                break;
            }
            System.out.println(ConsoleUI.RED_BOLD +
                    "Last name can contain only letters and spaces." +
                    ConsoleUI.RESET);
        }

        String nickname;
        while (true) {
            System.out.print("Nickname (optional): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                nickname = "";
                break;
            }
            if (isValidName(input)) {
                nickname = input;
                break;
            }
            System.out.println(ConsoleUI.RED_BOLD +
                    "Nickname can contain only letters and spaces." +
                    ConsoleUI.RESET);
        }

        String phone;
        while (true) {
            System.out.print("Phone (primary): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println(ConsoleUI.RED_BOLD +
                        "Primary phone is required." + ConsoleUI.RESET);
                continue;
            }
            if (isValidPhone(input)) {
                phone = input;
                break;
            }
            System.out.println(ConsoleUI.RED_BOLD +
                    "Primary phone must contain only digits (10–15 digits)." +
                    ConsoleUI.RESET);
        }

        String phoneSec;
        while (true) {
            System.out.print("Phone (secondary, optional): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                phoneSec = "";
                break;
            }
            if (isValidPhone(input)) {
                phoneSec = input;
                break;
            }
            System.out.println(ConsoleUI.RED_BOLD +
                    "Secondary phone must contain only digits (10–15 digits)." +
                    ConsoleUI.RESET);
        }

        String email;
        while (true) {
            System.out.print("Email (optional): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                email = "";
                break;
            }
            if (isValidEmail(input)) {
                email = input;
                break;
            }
            System.out.println(ConsoleUI.RED_BOLD +
                    "Email must be in a valid format (example@domain.com)." +
                    ConsoleUI.RESET);
        }

        String linkedin;
        while (true) {
            System.out.print("LinkedIn URL (optional): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                linkedin = "";
                break;
            }
            if (isValidLinkedin(input)) {
                linkedin = input;
                break;
            }
            System.out.println(ConsoleUI.RED_BOLD +
                    "LinkedIn URL must contain 'linkedin.com'." +
                    ConsoleUI.RESET);
        }

        System.out.print("Address (optional): ");
        String address = scanner.nextLine().trim();

        java.sql.Date birthDateSql = null;

        while (true) {
            System.out.print("Birth date (YYYY-MM-DD, empty = unknown): ");
            String birthInput = scanner.nextLine().trim();

            if (birthInput.isEmpty()) {
                break;
            }

            try {
                LocalDate ld = LocalDate.parse(birthInput);
                int year = ld.getYear();

                if (!isValidBirthYear(year)) {
                    System.out.println(ConsoleUI.RED_BOLD +
                            "Year must be between 1900 and 2025." +
                            ConsoleUI.RESET);
                    continue;
                }

                birthDateSql = java.sql.Date.valueOf(ld);
                break;

            } catch (DateTimeParseException e) {
                System.out.println(ConsoleUI.RED_BOLD +
                        "Invalid date format! Use YYYY-MM-DD." +
                        ConsoleUI.RESET);
            }
        }


        String sql = """
                INSERT INTO contacts
                  (first_name, middle_name, last_name, nickname, phone_primary, phone_secondary, email, linkedin_url, address, birthdate)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, middleName.isEmpty() ? null : middleName);
            ps.setString(3, lastName);
            ps.setString(4, nickname.isEmpty() ? null : nickname);
            ps.setString(5, phone);
            ps.setString(6, phoneSec.isEmpty() ? null : phoneSec);
            ps.setString(7, email.isEmpty() ? null : email);
            ps.setString(8, linkedin.isEmpty() ? null : linkedin);
            ps.setString(9, address.isEmpty() ? null : address);

            if (birthDateSql != null) {
                ps.setDate(10, birthDateSql);
            } else {
                ps.setNull(10, Types.DATE);
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ConsoleUI.printInfo("New contact added successfully!");
            } else {
                ConsoleUI.printError("Failed to add contact.");
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Database error while adding contact: " + e.getMessage());
        }

        ConsoleUI.pause();
    }

    /**
     * Deletes a contact by ID and stores a snapshot in the undo stack.
     * <p>
     * Workflow:
     * <ol>
     *     <li>Ask user for contact ID</li>
     *     <li>Fetch full contact from database</li>
     *     <li>Confirm deletion with the user</li>
     *     <li>Push snapshot onto {@link #deletedContactsStack}</li>
     *     <li>Execute DELETE on {@code contacts} table</li>
     * </ol>
     * If deletion fails, the snapshot is removed again from the stack.
     * </p>
     *
     * @param scanner scanner used to read input from the console
     *
     * @author Eren Çakır Bircan
     */
    public void deleteContact(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Delete Contact (Senior) ===" + ConsoleUI.RESET);

        System.out.print("Enter Contact ID to delete (0 to cancel): ");
        String idInput = scanner.nextLine().trim();
        int contactId;

        try {
            contactId = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid ID format!");
            ConsoleUI.pause();
            return;
        }

        if (contactId == 0) {
            ConsoleUI.printInfo("Delete cancelled.");
            return;
        }

        String checkSql = "SELECT * FROM contacts WHERE contact_id = ?";
        String deleteSql = "DELETE FROM contacts WHERE contact_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, contactId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (!rs.next()) {
                    ConsoleUI.printError("Contact with ID " + contactId + " not found.");
                    ConsoleUI.pause();
                    return;
                }

                ContactSnapshot snap = new ContactSnapshot();
                snap.contactId      = rs.getInt("contact_id");
                snap.firstName      = rs.getString("first_name");
                snap.middleName     = rs.getString("middle_name");
                snap.lastName       = rs.getString("last_name");
                snap.nickname       = rs.getString("nickname");
                snap.phonePrimary   = rs.getString("phone_primary");
                snap.phoneSecondary = rs.getString("phone_secondary");
                snap.email          = rs.getString("email");
                snap.linkedinUrl    = rs.getString("linkedin_url");
                snap.address        = rs.getString("address");
                snap.birthdate      = rs.getDate("birthdate");

                System.out.println("You are about to delete: " + ConsoleUI.RED_BOLD +
                        snap.firstName + " " + snap.lastName + ConsoleUI.RESET);

                System.out.print("Are you sure? (yes/no): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("yes")) {
                    ConsoleUI.printInfo("Delete cancelled.");
                    ConsoleUI.pause();
                    return;
                }

                deletedContactsStack.push(snap);

                try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                    delPs.setInt(1, contactId);
                    int affected = delPs.executeUpdate();
                    if (affected > 0) {
                        ConsoleUI.printInfo("Contact deleted successfully. (You can undo with option 8)");
                    } else {
                        ConsoleUI.printError("Delete failed.");
                        deletedContactsStack.pop();
                    }
                }
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Database error while deleting contact: " + e.getMessage());
        }

        ConsoleUI.pause();
    }

    /**
     * Restores the last deleted contact from the undo stack.
     * <p>
     * Pops a {@link ContactSnapshot} from {@link #deletedContactsStack}
     * and inserts it back into the {@code contacts} table.
     * All relevant fields are restored, including:
     * middle name, secondary phone, LinkedIn URL and birthdate.
     * </p>
     * If the insert fails, the snapshot is pushed back onto the stack.
     *
     * @author Eren Çakır Bircan
     */
    public void undoLastDelete() {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Undo Last Delete (Senior) ===" + ConsoleUI.RESET);

        if (deletedContactsStack.isEmpty()) {
            ConsoleUI.printInfo("There is no deleted contact to restore.");
            ConsoleUI.pause();
            return;
        }

        ContactSnapshot snap = deletedContactsStack.pop();

        String sql = """
                INSERT INTO contacts
                  (contact_id, first_name, middle_name, last_name, nickname, phone_primary, phone_secondary, email, linkedin_url, address, birthdate)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, snap.contactId);
            ps.setString(2, snap.firstName);
            ps.setString(3, snap.middleName);
            ps.setString(4, snap.lastName);
            ps.setString(5, snap.nickname);
            ps.setString(6, snap.phonePrimary);
            ps.setString(7, snap.phoneSecondary);
            ps.setString(8, snap.email);
            ps.setString(9, snap.linkedinUrl);
            ps.setString(10, snap.address);

            if (snap.birthdate != null) {
                ps.setDate(11, snap.birthdate);
            } else {
                ps.setNull(11, Types.DATE);
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ConsoleUI.printInfo("Last deleted contact restored successfully.");
            } else {
                ConsoleUI.printError("Failed to restore contact.");
                deletedContactsStack.push(snap);
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Database error while restoring contact: " + e.getMessage());
            deletedContactsStack.push(snap);
        }

        ConsoleUI.pause();
    }
    /**
     * Validates a name-like input to contain only letters and spaces.
     *
     * @param input the raw name input from the user
     * @return true if the input is non-empty and matches the allowed pattern, false otherwise
     * @author Eren Çakır Bircan
     */
    private boolean isValidName(String input) {
        return input != null
                && !input.trim().isEmpty()
                && input.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$");
    }

    /**
     * Validates a phone number to contain only digits with a length between 10 and 15.
     *
     * @param phone the raw phone input from the user
     * @return true if the phone consists only of digits and has a valid length, false otherwise
     * @author Eren Çakır Bircan
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        return phone.matches("\\d{10,15}");
    }

    /**
     * Performs a simple validation for an email address format.
     * Checks for the presence of '@' and at least one '.' after it, in valid positions.
     *
     * @param email the raw email input from the user
     * @return true if the email passes basic structural checks, false otherwise
     * @author Eren Çakır Bircan
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;

        int at = email.indexOf('@');
        if (at <= 0 || at == email.length() - 1) return false;

        int dotAfter = email.indexOf('.', at);
        return dotAfter > at + 1 && dotAfter < email.length() - 1;
    }

    /**
     * Validates a LinkedIn URL by checking whether it contains the domain 'linkedin.com'.
     *
     * @param url the raw LinkedIn URL input from the user
     * @return true if the URL is non-empty and contains 'linkedin.com', false otherwise
     * @author Eren Çakır Bircan
     */
    private boolean isValidLinkedin(String url) {
        if (url == null || url.isEmpty()) return false;
        String lower = url.toLowerCase();
        return lower.contains("linkedin.com");
    }
    /**
     * Validates that the birth year is within the allowed range [1900, 2025].
     *
     * @param year the year part of the birth date
     * @return true if the year is between 1900 and 2025 (inclusive), false otherwise
     * @author Eren Çakır Bircan
     */
    private boolean isValidBirthYear(int year) {
        return year >= 1900 && year <= 2025;
    }

}