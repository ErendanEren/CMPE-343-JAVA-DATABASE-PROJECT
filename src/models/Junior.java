package models;

import Database.DatabaseConnection;
import util.ConsoleUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Represents the "Junior Developer" role.
 * <p>
 * This class extends {@link Tester}, inheriting all read-only capabilities (List, Search, Sort).
 * Additionally, Junior Developers have the permission to <b>UPDATE</b> existing contact records.
 * </p>
 *
 * @author Arda Dulger & selcukaloba
 */
public class Junior extends Tester {

    public Junior() {
        super();
        this.setRole("Junior Developer");
    }

    public Junior(int userId, String username, String name, String surname, Connection connection) {
        super(userId, username, name, surname);
        this.setRole("Junior Developer");
    }

    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Junior Panel: " + getName() + " " + getSurname() + ConsoleUI.RESET,
                    new String[]{
                            "1) Change password",
                            "2) List all contacts",
                            "3) Search contacts",
                            "4) Sort contacts",
                            "5) Update existing contact",
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
                case "0" -> {
                    running = false;
                    ConsoleUI.printInfo("Logging out...");
                }
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    /**
     * Updates an existing contact's information in the database.
     * <p>
     * Logic:
     * 1. Asks for the Contact ID.
     * 2. Checks if the ID exists.
     * 3. Retrieves current data to show to the user.
     * 4. Asks for new values (Pressing Enter keeps the current value).
     * 5. Updates the record in the database.
     * </p>
     *
     * @param scanner Scanner for user input
     * @author Arda Dulger & selcukaloba
     */
    public void updateContact(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Update Existing Contact (Junior) ===" + ConsoleUI.RESET);

        System.out.print("Enter the ID of the contact to update: ");
        String idInput = scanner.nextLine();
        int contactId;

        try {
            contactId = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid ID format!");
            return;
        }

        String selectSql = "SELECT * FROM contacts WHERE contact_id = ?";

        String currentName = "", currentSurname = "", currentPhone = "", currentEmail = "";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {

            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    currentName = rs.getString("first_name");
                    currentSurname = rs.getString("last_name");
                    currentPhone = rs.getString("phone_primary");
                    currentEmail = rs.getString("email");
                } else {
                    ConsoleUI.printError("Contact with ID " + contactId + " not found!");
                    return;
                }
            }
        } catch (SQLException e) {
            ConsoleUI.printError("Database error during retrieval: " + e.getMessage());
            return;
        }

        System.out.println("\n" + ConsoleUI.CYAN_BOLD + "Enter new values (Press ENTER to keep current value):" + ConsoleUI.RESET);

        System.out.println("First Name (" + currentName + "): ");
        String newName = scanner.nextLine().trim();
        if (newName.isEmpty()) newName = currentName;

        System.out.println("Last Name (" + currentSurname + "): ");
        String newSurname = scanner.nextLine().trim();
        if (newSurname.isEmpty()) newSurname = currentSurname;

        System.out.println("Phone (" + currentPhone + "): ");
        String newPhone = scanner.nextLine().trim();
        if (newPhone.isEmpty()) newPhone = currentPhone;

        System.out.println("Email (" + currentEmail + "): ");
        String newEmail = scanner.nextLine().trim();
        if (newEmail.isEmpty()) newEmail = currentEmail;

        String updateSql = "UPDATE contacts SET first_name=?, last_name=?, phone_primary=?, email=?, updated_at=CURRENT_TIMESTAMP WHERE contact_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, newName);
            ps.setString(2, newSurname);
            ps.setString(3, newPhone);
            ps.setString(4, newEmail);
            ps.setInt(5, contactId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ConsoleUI.printInfo("Contact updated successfully!");
            } else {
                ConsoleUI.printError("Failed to update contact.");
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Database Update Error: " + e.getMessage());
        }
    }
}