package util;

import java.util.Scanner;

public class ConsoleUI {

    // ====== BASIC SETTINGS ======
    public static final int WIDTH = 60;

    // Box drawing characters
    public static final String TOP_LEFT     = "┌";
    public static final String TOP_RIGHT    = "┐";
    public static final String BOTTOM_LEFT  = "└";
    public static final String BOTTOM_RIGHT = "┘";
    public static final String HORIZONTAL   = "─";
    public static final String VERTICAL     = "│";

    // ====== COLORS (ANSI codes) ======
    public static final String RESET       = "\u001B[0m";
    public static final String BLUE_BOLD   = "\u001B[1;34m";
    public static final String CYAN_BOLD   = "\u001B[1;36m";
    public static final String RED_BOLD    = "\u001B[1;31m";
    public static final String GREEN_BOLD  = "\u001B[1;32m";
    public static final String YELLOW_BOLD = "\u001B[1;33m";

    public static final String LIGHT_GRAY  = "\u001B[37m";
    public static final String ITALIC      = "\u001B[3m";

    // ====== CLEAR CONSOLE ======
    public static void clearConsole() {
        try {
            String os = System.getProperty("os.name");
            if (os.startsWith("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls")
                        .inheritIO()
                        .start()
                        .waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    public static void printCenter(String text) {
        int len = text.length();
        int padLeft = Math.max(0, (WIDTH - len) / 2);
        for (int i = 0; i < padLeft; i++) System.out.print(" ");
        System.out.print(text);
    }

    private static void printRow(String text) {
        String cleanText = text.replaceAll("\033\\[[;\\d]*m", "");

        int len = cleanText.length();
        int space = WIDTH - 2;
        int padLeft = (space - len) / 2;
        int padRight = space - len - padLeft;

        System.out.print(VERTICAL);
        for (int i = 0; i < padLeft; i++) System.out.print(" ");
        System.out.print(text);
        for (int i = 0; i < padRight; i++) System.out.print(" ");
        System.out.println(VERTICAL);
    }

    public static void printBoxed(String title, String[] content) {
        System.out.print(TOP_LEFT);
        for (int i = 0; i < WIDTH - 2; i++) System.out.print(HORIZONTAL);
        System.out.println(TOP_RIGHT);

        printRow(title);

        System.out.print("├");
        for (int i = 0; i < WIDTH - 2; i++) System.out.print(HORIZONTAL);
        System.out.println("┤");

        for (String line : content) {
            printRow(line);
        }

        System.out.print(BOTTOM_LEFT);
        for (int i = 0; i < WIDTH - 2; i++) System.out.print(HORIZONTAL);
        System.out.println(BOTTOM_RIGHT);
    }

    public static void pause() {
        System.out.print("Press Enter to continue...");
        try {
            System.in.read();
        } catch (Exception ignored) { }
        System.out.println();
    }

    // ====== GENEL MENÜ ======
    public static String showMenu(String title, String[] optionLines, Scanner scanner) {
        clearConsole();

        String[] content = new String[optionLines.length + 2];
        content[0] = "";
        System.arraycopy(optionLines, 0, content, 1, optionLines.length);
        content[content.length - 1] = "";

        printBoxed(title, content);

        System.out.println();
        printCenter("Your choice: ");
        return scanner.nextLine().trim();
    }

    public static void printInvalidChoice() {
        System.out.println();
        System.out.println(RED_BOLD + "Invalid choice. Please try again." + RESET);
        pause();
    }

    public static void printLoginBox() {
        String[] content = {
                "",
                "Login Screen",
                "",
                "Please enter your username and password.",
                "Type 0 or 'exit' as username to quit.",
                ""
        };

        printBoxed(BLUE_BOLD + "Login" + RESET, content);
    }

    public static void printError(String message) {
        System.out.println();
        System.out.println(RED_BOLD + message + RESET);
        pause();
    }

    public static void printInfo(String message) {
        System.out.println();
        System.out.println(GREEN_BOLD + message + RESET);
        pause();
    }
}
