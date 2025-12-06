import Database.DatabaseConnection;
import models.User;
import util.ConsoleUI;

import java.util.Scanner;

/**
 * Main entry point for the Group 09 Contact Management System application.
 * <p>
 * Handles:
 * <ul>
 *     <li>Startup animations</li>
 *     <li>Login loop</li>
 *     <li>User role dispatch (calling {@code loggedIn.showMenu(scanner)})</li>
 *     <li>Exit animations on shutdown</li>
 * </ul>
 *
 * @author Eren Çakır Bircan
 */
public class Group09 {

    /** Shared scanner instance for reading user input from the console. */
    private static final Scanner scanner = new Scanner(System.in);

    /** ANSI reset color code. */
    private static final String RESET = "\u001B[0m";
    /** ANSI white color code. */
    private static final String WHITE = "\u001B[37m";

    /**
     * ANSI color codes used in the disco ball animation.
     * Red, Yellow, Green, Cyan, Blue, Magenta.
     */
    private static final String[] COLORS = {
            "\u001B[31m",
            "\u001B[33m",
            "\u001B[32m",
            "\u001B[36m",
            "\u001B[34m",
            "\u001B[35m"
    };

    /** Width of the console canvas used by animations. */
    private static final int WIDTH = 100;
    /** Height of the console canvas used by animations. */
    private static final int HEIGHT = 30;

    /**
     * Application entry point.
     * <p>
     * Runs the intro disco-ball animation, shows the boot animation,
     * then enters a login loop:
     * <ul>
     *     <li>Asks for username and password</li>
     *     <li>Authenticates using {@link Database.DatabaseConnection#login}</li>
     *     <li>Dispatches to the logged-in user's menu</li>
     *     <li>On "0" or "exit", plays shutdown and Matrix animations then exits</li>
     * </ul>
     *
     * @param args command-line arguments (not used)
     *
     * @author Eren Çakır Bircan
     */
    public static void main(String[] args) {

        runDiscoBallAnimation();               // 🪩 Start with disco ball
        ConsoleUI.printBootAnimation();        // 🚀 Boot animation

        while (true) {
            ConsoleUI.clearConsole();
            ConsoleUI.printLoginBox();

            System.out.println();
            System.out.print("Username (0 / exit = quit): ");
            String username = scanner.nextLine().trim();

            if ("0".equals(username) || "exit".equalsIgnoreCase(username)) {
                System.out.println("Exiting...");
                ConsoleUI.printShutdownAnimation();

                runMatrixRainIntro();
                try {
                    printMatrixPartyOver();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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

    /**
     * Runs the intro "disco ball" ASCII animation.
     * <p>
     * Uses a radial sine wave to decide where to draw bright and dim
     * characters, cycling through a set of ANSI colors.
     *
     * @author Eren Çakır Bircan
     */
    private static void runDiscoBallAnimation() {
        double t = 0.0;

        for (int frame = 0; frame < 80; frame++) {
            localClearConsole();

            StringBuilder output = new StringBuilder();

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    double dx = x - WIDTH / 2.0;
                    double dy = y - HEIGHT / 2.0;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    double wave = Math.sin(dist / 2.0 - t);
                    if (wave > 0.5) {
                        int colorIndex = (int) ((wave + t) * 10) % COLORS.length;
                        output.append(COLORS[colorIndex]).append("*").append(RESET);
                    } else if (wave > 0.3) {
                        output.append(WHITE).append(".").append(RESET);
                    } else {
                        output.append(" ");
                    }
                }
                output.append("\n");
            }

            System.out.print(output);
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            t += 0.2;
        }
    }

    /**
     * Runs a short "Matrix rain" style animation.
     * <p>
     * Random vertical columns of green characters fall across the screen,
     * using the same {@link #WIDTH} and {@link #HEIGHT} settings.
     *
     * @author Eren Çakır Bircan
     */
    private static void runMatrixRainIntro() {
        String GREEN = "\u001B[32m";
        int width = WIDTH;
        int height = HEIGHT;
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        int[] drops = new int[width];

        for (int i = 0; i < width; i++) {
            drops[i] = (int) (Math.random() * height);
        }

        for (int frame = 0; frame < 60; frame++) {
            localClearConsole();
            StringBuilder screen = new StringBuilder();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (y == drops[x]) {
                        char c = chars[(int) (Math.random() * chars.length)];
                        screen.append(GREEN).append(c).append(RESET);
                    } else {
                        screen.append(" ");
                    }
                }
                screen.append("\n");
            }

            System.out.print(screen);
            for (int i = 0; i < width; i++) {
                drops[i] = (drops[i] + 1) % height;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Prints the "PARTY OVER" Matrix-style ASCII banner with a falling effect.
     * <p>
     * Characters are revealed column by column from bottom to top,
     * creating a vertical drop animation over the banner text.
     *
     * @throws InterruptedException if the sleep between frames is interrupted
     *
     * @author Eren Çakır Bircan
     */
    private static void printMatrixPartyOver() throws InterruptedException {
        String[] lines = {
                "██████╗  █████╗ ██████╗ ████████╗██╗   ██╗     ██╗ ███████╗     ██████╗ ██╗   ██╗███████╗██████╗ ",
                "██╔══██╗██╔══██╗██╔══██╗╚══██╔══╝╚██╗ ██╔╝     ██║ ██╔════╝    ██╔═══██╗██║   ██║██╔════╝██╔══██╗",
                "██████╔╝███████║██████╔╝   ██║    ╚████╔╝      ██║ ███████╗    ██║   ██║██║   ██║█████╗  ██████╔╝",
                "██╔═══╝ ██╔══██║██╔══██╗   ██║     ╚██╔╝       ██║ ╔════██║    ██║   ██║██║   ██║██╔══╝  ██╔══██╗",
                "██║     ██║  ██║██║  ██║   ██║      ██║        ██║ ███████╗    ╚██████╔╝╚██████╔╝███████╗██║  ██║",
                "╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝      ╚═╝        ╚═╝ ╚══════╝     ╚═════╝  ╚═════╝ ╚══════╝ ╚═╝  ╚═╝"
        };

        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";

        char[][] buffer = new char[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            buffer[i] = new char[lines[i].length()];
            for (int j = 0; j < lines[i].length(); j++) {
                buffer[i][j] = ' ';
            }
        }

        for (int col = 0; col < lines[0].length(); col++) {
            for (int row = lines.length - 1; row >= 0; row--) {
                buffer[row][col] = lines[row].charAt(col);
                localClearConsole();
                for (char[] line : buffer) {
                    System.out.println(GREEN + new String(line) + RESET);
                }
                Thread.sleep(2);
            }
            Thread.sleep(5);
        }
    }

    /**
     * Clears the console using ANSI escape codes.
     * <p>
     * This method is local to this class and used by animations so that
     * it does not interfere with {@link ConsoleUI#clearConsole()} logic.
     *
     * @author Eren Çakır Bircan
     */
    private static void localClearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}