
import Database.DatabaseConnection;
import models.User;
import util.ConsoleUI;
import java.sql.*;

import java.util.Scanner;

public class Group09 {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            ConsoleUI.clearConsole();
            ConsoleUI.printLoginBox();

            System.out.println();
            System.out.print("Username (0 / exit = quit): ");
            String username = scanner.nextLine().trim();

            if ("0".equals(username) || "exit".equalsIgnoreCase(username)) {
                System.out.println("Exiting...");
                break;
            }

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            User loggedIn = DatabaseConnection.login(username, password);

            if (loggedIn == null) {
                System.out.println("Invalid username or password. Press Enter to try again...");
                scanner.nextLine();
                continue;
            }


            ConsoleUI.clearConsole();
            System.out.println("Welcome, " + loggedIn.getName() +
                    " (" + loggedIn.getRole() + ")");


            loggedIn.showMenu(scanner);
        }
    }
}
