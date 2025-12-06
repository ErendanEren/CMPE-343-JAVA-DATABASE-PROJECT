package models;

import Database.DatabaseConnection;
import util.ConsoleUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Stack;

/**
 * Represents the "Junior Developer" role.
 * <p>
 * This class extends {@link Tester}, inheriting all read-only capabilities (List, Search, Sort).
 * Additionally, Junior Developers have the permission to <b>UPDATE</b> existing contact records.
 * It also supports an <b>UNDO</b> mechanism for the update operations using a Stack.
 * </p>
 *
 * @author Arda Dulger & Selcuk Aloba
 */
public class Junior extends Tester {

    private final Stack<ContactBackup> undoStack = new Stack<>();

    private static class ContactBackup {
        int contactId;
        String firstName;
        String lastName;
        String phonePrimary;
        String phoneSecondary;
        String email;
        String address;
        String linkedinUrl;

        public ContactBackup(int id, String first, String last, String phone, String secPhone, String mail, String addr, String linkedin) {
            this.contactId = id;
            this.firstName = first;
            this.lastName = last;
            this.phonePrimary = phone;
            this.phoneSecondary = secPhone;
            this.email = mail;
            this.address = addr;
            this.linkedinUrl = linkedin;
        }
    }

    public Junior() {
        super();
        this.setRole("Junior Developer");
    }

    public Junior(int userId, String username, String name, String surname) {
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
                            "6) Undo last update",
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
                case "6" -> undoLastUpdate();
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
     * Supports updating ALL fields including new ones (Secondary Phone, Address, LinkedIn).
     * @author selcukaloba
     * @author dulgerarda
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

        String curName = "", curSurname = "", curPhone = "", curSecPhone = "", curEmail = "", curAddr = "", curLinkedin = "";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {

            ps.setInt(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    curName = rs.getString("first_name");
                    curSurname = rs.getString("last_name");
                    curPhone = rs.getString("phone_primary");
                    curSecPhone = rs.getString("phone_secondary");
                    curEmail = rs.getString("email");
                    curAddr = rs.getString("address");
                    curLinkedin = rs.getString("linkedin_url");
                } else {
                    ConsoleUI.printError("Contact with ID " + contactId + " not found!");
                    return;
                }
            }
        } catch (SQLException e) {
            ConsoleUI.printError("Database error during retrieval: " + e.getMessage());
            return;
        }

        if (curSecPhone == null) curSecPhone = "";
        if (curAddr == null) curAddr = "";
        if (curLinkedin == null) curLinkedin = "";


        ContactBackup backup = new ContactBackup(contactId, curName, curSurname, curPhone, curSecPhone, curEmail, curAddr, curLinkedin);
        undoStack.push(backup);

        System.out.println("\n" + ConsoleUI.CYAN_BOLD + "Enter new values (Press ENTER to keep current value):" + ConsoleUI.RESET);

        System.out.println("First Name (" + curName + "): ");
        String newName = scanner.nextLine().trim();
        if (newName.isEmpty()) newName = curName;

        System.out.println("Last Name (" + curSurname + "): ");
        String newSurname = scanner.nextLine().trim();
        if (newSurname.isEmpty()) newSurname = curSurname;

        System.out.println("Pri. Phone (" + curPhone + "): ");
        String newPhone = scanner.nextLine().trim();
        if (newPhone.isEmpty()) newPhone = curPhone;

        System.out.println("Sec. Phone (" + curSecPhone + "): ");
        String newSecPhone = scanner.nextLine().trim();
        if (newSecPhone.isEmpty()) newSecPhone = curSecPhone;

        System.out.println("Email (" + curEmail + "): ");
        String newEmail = scanner.nextLine().trim();
        if (newEmail.isEmpty()) newEmail = curEmail;

        System.out.println("Address (" + curAddr + "): ");
        String newAddr = scanner.nextLine().trim();
        if (newAddr.isEmpty()) newAddr = curAddr;

        System.out.println("LinkedIn (" + curLinkedin + "): ");
        String newLinkedin = scanner.nextLine().trim();
        if (newLinkedin.isEmpty()) newLinkedin = curLinkedin;

        String updateSql = "UPDATE contacts SET first_name=?, last_name=?, phone_primary=?, phone_secondary=?, email=?, address=?, linkedin_url=?, updated_at=CURRENT_TIMESTAMP WHERE contact_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, newName);
            ps.setString(2, newSurname);
            ps.setString(3, newPhone);
            ps.setString(4, newSecPhone.isEmpty() ? null : newSecPhone);
            ps.setString(5, newEmail.isEmpty() ? null : newEmail);
            ps.setString(6, newAddr.isEmpty() ? null : newAddr);
            ps.setString(7, newLinkedin.isEmpty() ? null : newLinkedin);
            ps.setInt(8, contactId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ConsoleUI.printInfo("Contact updated successfully! (Undo available)");
            } else {
                ConsoleUI.printError("Failed to update contact.");
                undoStack.pop();
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Database Update Error: " + e.getMessage());
            undoStack.pop();
        }
        ConsoleUI.pause();
    }

    /**
     * Reverts the last update operation.
     * @author selcukaloba
     * @author dulgerarda
     */
    public void undoLastUpdate() {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Undo Last Update (Junior) ===" + ConsoleUI.RESET);

        if (undoStack.isEmpty()) {
            ConsoleUI.printInfo("No updates to undo.");
            ConsoleUI.pause();
            return;
        }

        ContactBackup oldData = undoStack.pop();

        System.out.println("Restoring contact ID: " + oldData.contactId);
        System.out.println("Reverting to: " + oldData.firstName + " " + oldData.lastName);

        String sql = "UPDATE contacts SET first_name=?, last_name=?, phone_primary=?, phone_secondary=?, email=?, address=?, linkedin_url=?, updated_at=CURRENT_TIMESTAMP WHERE contact_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, oldData.firstName);
            ps.setString(2, oldData.lastName);
            ps.setString(3, oldData.phonePrimary);
            ps.setString(4, oldData.phoneSecondary);
            ps.setString(5, oldData.email);
            ps.setString(6, oldData.address);
            ps.setString(7, oldData.linkedinUrl);
            ps.setInt(8, oldData.contactId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ConsoleUI.printInfo("Undo successful! Contact restored to previous state.");
            } else {
                ConsoleUI.printError("Undo failed! Contact might have been deleted.");
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Database error during undo: " + e.getMessage());
            undoStack.push(oldData);
        }

        ConsoleUI.pause();
    }
}