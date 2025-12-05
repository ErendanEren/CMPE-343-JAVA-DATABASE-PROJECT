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
    public static final String PURPLE_BOLD = "\u001B[1;35m";
    public static final String WHITE_BOLD  = "\u001B[1;37m";

    public static final String LIGHT_GRAY  = "\u001B[37m";
    public static final String ITALIC      = "\u001B[3m";

    // ====== ANIMASYON AYARLARI ======
    // Animasyonun hızını buradan ayarlayabilirsin (ms cinsinden)
    private static final int ANIMATION_DELAY = 40;

    // ====== BOOT ANIMASYONU (BAŞLANGIÇ) ======
    public static void printBootAnimation() {
        clearConsole();

        // 1. Sistem Yükleme Efekti (Loading Bar)
        System.out.println(CYAN_BOLD + "Initializing System Constraints..." + RESET);
        printProgressBar("Loading Modules", 100);

        System.out.println(CYAN_BOLD + "Connecting to Database Services..." + RESET);
        printProgressBar("Verifying Schema", 100);

        clearConsole();

        // 2. ASCII Logo Gösterimi
        System.out.println(PURPLE_BOLD);
        System.out.println("   ______ ____  ____  _    _ _____   ___   ___  ");
        System.out.println("  / ____|  _ \\|  _ \\| |  | |  __ \\ / _ \\ / _ \\ ");
        System.out.println(" | |  __| |_) | |_) | |  | | |__) | | | | (_) |");
        System.out.println(" | | |_ |  _ <|  _ <| |  | |  ___/| | | |\\__, |");
        System.out.println(" | |__| | |_) | |_) | |__| | |    | |_| |  / / ");
        System.out.println("  \\_____|____/|____/ \\____/|_|     \\___/  /_/  ");
        System.out.println(RESET);

        System.out.println(YELLOW_BOLD + "   CONTACT MANAGEMENT SYSTEM v1.0" + RESET);
        System.out.println(LIGHT_GRAY + "   Created by Group 09 Engineering Team" + RESET);
        System.out.println("\n");

        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    // ====== SHUTDOWN ANIMASYONU (KAPANIŞ) ======
    public static void printShutdownAnimation() {
        System.out.println();
        System.out.println(RED_BOLD + "Termination Signal Received..." + RESET);

        // Tersine sayan bir kapanış efekti
        try {
            System.out.print(YELLOW_BOLD + "Saving Data states... " + RESET);
            Thread.sleep(600);
            System.out.println(GREEN_BOLD + "[OK]" + RESET);

            System.out.print(YELLOW_BOLD + "Closing Database Connection... " + RESET);
            Thread.sleep(600);
            System.out.println(GREEN_BOLD + "[OK]" + RESET);

            System.out.println(RED_BOLD + "System Halted." + RESET);
            System.out.println("Goodbye!");
            Thread.sleep(500);
        } catch (InterruptedException e) { }
    }

    // Yardımcı Metod: Yükleme Çubuğu Çizer
    private static void printProgressBar(String taskName, int total) {
        String anim = "|/-\\";
        for (int i = 0; i <= total; i += 4) { // 4'er 4'er artırarak hızlandırdım
            try {
                Thread.sleep(ANIMATION_DELAY);
                String data = "\r" + BLUE_BOLD + "[" + anim.charAt(i % 4) + "] " + taskName + ": " +
                        "[" + "=".repeat(i / 4) + " ".repeat((100 - i) / 4) + "] " +
                        i + "%" + RESET;
                System.out.print(data);
            } catch (InterruptedException e) {
                break;
            }
        }
        System.out.println("\r" + GREEN_BOLD + "[✓] " + taskName + ": [=========================] 100% - COMPLETED" + RESET);
        try { Thread.sleep(300); } catch (Exception e){}
    }

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
        pause(); // Menüye dönmeden önce okuması için beklet
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