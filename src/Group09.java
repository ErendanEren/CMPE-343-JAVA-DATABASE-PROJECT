import Database.DatabaseConnection;
import models.User;
import util.ConsoleUI;
import java.sql.*;

import java.util.Scanner;

public class Group09 {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // 1. PROGRAM AÇILIŞINDA ANİMASYON (Hocanın isteği) 
        ConsoleUI.printBootAnimation();

        while (true) {
            ConsoleUI.clearConsole();
            ConsoleUI.printLoginBox();

            System.out.println();
            System.out.print("Username (0 / exit = quit): ");
            String username = scanner.nextLine().trim();

            if ("0".equals(username) || "exit".equalsIgnoreCase(username)) {
                // 2. PROGRAM KAPANIŞINDA ANİMASYON
                ConsoleUI.printShutdownAnimation();
                break;
            }

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            User loggedIn = DatabaseConnection.login(username, password);

            if (loggedIn == null) {
                System.out.println(ConsoleUI.RED_BOLD + "\nAccess Denied: Invalid credentials." + ConsoleUI.RESET);
                ConsoleUI.pause();
                continue;
            }

            // Successful login
            ConsoleUI.clearConsole();
            System.out.println("Welcome, " + loggedIn.getName() +
                    " (" + loggedIn.getRole() + ")");

            // Each subclass overrides showMenu(scanner)
            loggedIn.showMenu(scanner);
        }
    }
}