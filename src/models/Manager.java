package models;

import util.ConsoleUI;

import java.util.Scanner;

public class Manager extends User {

    private static final Scanner scanner = new Scanner(System.in);

    @Override
    public void showMenu() {
        boolean running = true;

        while (running) {
            String choice = ConsoleUI.showMenu(
                    ConsoleUI.BLUE_BOLD + "Manager Menu" + ConsoleUI.RESET,
                    new String[]{
                            "1) Change password",
                            "2) Show contacts statistics",
                            "3) List all users",
                            "4) Add new user",
                            "5) Update existing user",
                            "6) Delete user",
                            "",
                            "0) Logout"
                    },
                    scanner
            );

            switch (choice) {
                case "1" -> changePassword(scanner);
                case "2" -> showContactStats(scanner);
                case "3" -> listAllUsers(scanner);
                case "4" -> addUser(scanner);
                case "5" -> updateUser(scanner);
                case "6" -> deleteUser(scanner);
                case "0" -> running = false;
                default -> ConsoleUI.printInvalidChoice();
            }
        }
    }

    public void changePassword(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Change password for: " + getUsername());

        ConsoleUI.pause();
    }

    public void showContactStats(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Contact statistics (Manager)...");

        ConsoleUI.pause();
    }

    public void listAllUsers(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Listing all users...");
        // TODO: SELECT * FROM users and print
        ConsoleUI.pause();
    }

    public void addUser(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Add new user...");

        ConsoleUI.pause();
    }

    public void updateUser(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Update existing user...");

        ConsoleUI.pause();
    }

    public void deleteUser(Scanner scanner) {
        ConsoleUI.clearConsole();
        System.out.println("Delete user...");

        ConsoleUI.pause();
    }
}
