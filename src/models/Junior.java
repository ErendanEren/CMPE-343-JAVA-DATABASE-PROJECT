package models;

import util.ConsoleUI;

import java.util.Scanner;

public class Junior extends Tester {

    @Override
    public void showMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Junior Developer Menu" + ConsoleUI.RESET,
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
                case "1" -> changePassword(scanner);     // Tester'dan
                case "2" -> listAllContacts(scanner);    // Tester'dan
                case "3" -> searchContacts(scanner);     // Tester'dan
                case "4" -> sortContacts(scanner);       // Tester'dan
                case "5" -> updateContact(scanner);      // Junior
                case "0" -> running = false;
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    public void updateContact(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Update existing contact (Junior)...");
        ConsoleUI.pause();
    }
}
