package models;

import util.ConsoleUI;
import java.util.Scanner;

public class Tester extends User {

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
                case "1" -> changePassword(scanner);
                case "2" -> listAllContacts(scanner);
                case "3" -> searchContacts(scanner);
                case "4" -> sortContacts(scanner);
                case "0" -> running = false;
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    // Junior / Senior da kullanacağı için protected ve imzaları aynı
    protected void changePassword(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Change password (Tester)...");
        ConsoleUI.pause();
    }

    protected void listAllContacts(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("List all contacts (Tester)...");
        ConsoleUI.pause();
    }

    protected void searchContacts(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Search contacts (Tester)...");
        ConsoleUI.pause();
    }

    protected void sortContacts(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Sort contacts (Tester)...");
        ConsoleUI.pause();
    }
}
