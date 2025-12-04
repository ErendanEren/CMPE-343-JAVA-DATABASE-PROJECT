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


public class Senior extends Junior {

    private final Stack<ContactSnapshot> deletedContactsStack = new Stack<>();


    private static class ContactSnapshot {
        int contactId;
        String firstName;
        String lastName;
        String nickname;
        String phonePrimary;
        String email;
        String address;
        java.sql.Date birthdate;
    }

    public Senior() {
        super();
        this.setRole("Senior Developer");
    }

    // İstersen Manager.getUserById için parametreli ctor da ekleyebiliriz
    public Senior(int userId, String username, String name, String surname, Connection connection) {
        super(userId, username, name, surname, connection);
        this.setRole("Senior Developer");
    }

    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Senior Developer Menu" + ConsoleUI.RESET,
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
                case "1" -> changePassword(scanner);     // Tester
                case "2" -> listAllContacts(scanner);    // Tester
                case "3" -> searchContacts(scanner);     // Tester
                case "4" -> sortContacts(scanner);       // Tester
                case "5" -> updateContact(scanner);      // Junior
                case "6" -> addContact(scanner);         // Senior ekstra
                case "7" -> deleteContact(scanner);      // Senior ekstra
                case "8" -> undoLastDelete();            // Senior ekstra
                case "0" -> {
                    running = false;
                    ConsoleUI.printInfo("Logging out...");
                }
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    /**
     * Adds a new contact into the contacts table.
     * Senior ek yetkisi.
     */
    public void addContact(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Add New Contact (Senior) ===" + ConsoleUI.RESET);

        System.out.print("First Name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last Name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Nickname (optional): ");
        String nickname = scanner.nextLine().trim();

        System.out.print("Phone (primary): ");
        String phone = scanner.nextLine().trim();

        System.out.print("Email (optional): ");
        String email = scanner.nextLine().trim();

        System.out.print("Address (optional): ");
        String address = scanner.nextLine().trim();

        System.out.print("Birth date (YYYY-MM-DD, empty = unknown): ");
        String birthInput = scanner.nextLine().trim();

        // Basit validation
        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            ConsoleUI.printError("First name, last name and phone are required!");
            ConsoleUI.pause();
            return;
        }

        java.sql.Date birthDateSql = null;
        if (!birthInput.isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(birthInput); // format: 2020-05-17
                birthDateSql = java.sql.Date.valueOf(ld);
            } catch (DateTimeParseException e) {
                ConsoleUI.printError("Invalid date format! Use YYYY-MM-DD. Contact not added.");
                ConsoleUI.pause();
                return;
            }
        }

        String sql = """
                INSERT INTO contacts
                  (first_name, last_name, nickname, phone_primary, email, address, birthdate)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, nickname.isEmpty() ? null : nickname);
            ps.setString(4, phone);
            ps.setString(5, email.isEmpty() ? null : email);
            ps.setString(6, address.isEmpty() ? null : address);

            if (birthDateSql != null) {
                ps.setDate(7, birthDateSql);
            } else {
                ps.setNull(7, Types.DATE);
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
     * Optional helper for reading a birth date step by step.
     * Şu an kullanılmıyor ama istersen diğer yerlerde kullanabilirsin.
     */
    private java.sql.Date readBirthDateFromUser(Scanner scanner) {
        while (true) {
            System.out.println("Birth date (leave year empty = unknown)");

            System.out.print("  Year (YYYY): ");
            String yearStr = scanner.nextLine().trim();

            if (yearStr.isEmpty()) {
                return null;
            }

            int year;
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid year! Please enter a numeric value like 1999.");
                continue;
            }

            System.out.print("  Month (1-12): ");
            String monthStr = scanner.nextLine().trim();
            int month;
            try {
                month = Integer.parseInt(monthStr);
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid month! Please enter a number between 1 and 12.");
                continue;
            }

            System.out.print("  Day (1-31): ");
            String dayStr = scanner.nextLine().trim();
            int day;
            try {
                day = Integer.parseInt(dayStr);
            } catch (NumberFormatException e) {
                ConsoleUI.printError("Invalid day! Please enter a number between 1 and 31.");
                continue;
            }

            try {
                LocalDate ld = LocalDate.of(year, month, day);
                return java.sql.Date.valueOf(ld);
            } catch (Exception e) {
                ConsoleUI.printError("Invalid date combination! Please try again.");
            }
        }
    }

    /**
     * Deletes a contact and pushes a snapshot to the undo stack.
     */
    public void deleteContact(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println(ConsoleUI.YELLOW_BOLD + "=== Delete Contact (Senior) ===" + ConsoleUI.RESET);

        listAllContacts(scanner);

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

        // Tüm bilgileri alalım ki undo'da geri ekleyelim
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

                // Snapshot oluştur
                ContactSnapshot snap = new ContactSnapshot();
                snap.contactId    = rs.getInt("contact_id");
                snap.firstName    = rs.getString("first_name");
                snap.lastName     = rs.getString("last_name");
                snap.nickname     = rs.getString("nickname");
                snap.phonePrimary = rs.getString("phone_primary");
                snap.email        = rs.getString("email");
                snap.address      = rs.getString("address");
                snap.birthdate    = rs.getDate("birthdate");

                System.out.println("You are about to delete: " + ConsoleUI.RED_BOLD +
                        snap.firstName + " " + snap.lastName + ConsoleUI.RESET);

                System.out.print("Are you sure? (yes/no): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("yes")) {
                    ConsoleUI.printInfo("Delete cancelled.");
                    ConsoleUI.pause();
                    return;
                }

                // Önce stack'e push et
                deletedContactsStack.push(snap);

                try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                    delPs.setInt(1, contactId);
                    int affected = delPs.executeUpdate();
                    if (affected > 0) {
                        ConsoleUI.printInfo("Contact deleted successfully. (You can undo with option 8)");
                    } else {
                        ConsoleUI.printError("Delete failed.");
                        // başarısızsa stack'ten geri al
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
     * Restores the last deleted contact from the stack.
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
                  (contact_id, first_name, last_name, nickname, phone_primary, email, address, birthdate)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, snap.contactId);
            ps.setString(2, snap.firstName);
            ps.setString(3, snap.lastName);
            ps.setString(4, snap.nickname);
            ps.setString(5, snap.phonePrimary);
            ps.setString(6, snap.email);
            ps.setString(7, snap.address);

            if (snap.birthdate != null) {
                ps.setDate(8, snap.birthdate);
            } else {
                ps.setNull(8, Types.DATE);
            }

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ConsoleUI.printInfo("Last deleted contact restored successfully.");
            } else {
                ConsoleUI.printError("Failed to restore contact.");
            }

        } catch (SQLException e) {
            ConsoleUI.printError("Database error while restoring contact: " + e.getMessage());
        }

        ConsoleUI.pause();
    }
}
