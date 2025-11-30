package models;

import util.ConsoleUI;

import java.util.Scanner;

public class Senior extends Junior {

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
                case "6" -> addContact(scanner);
                case "7" -> deleteContact(scanner);
                case "0" -> running = false;
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    public void addContact(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Add new contact (Senior)...");
        ConsoleUI.pause();
    }

    public void deleteContact(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Delete contact (Senior)...");
        ConsoleUI.pause();
    }
}
