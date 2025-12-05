package util;

import models.Contact;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    // ====== BASIC SETTINGS ======
    public static final int WIDTH = 60;
    public static final int CARD_WIDTH = 80;

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

        System.out.println(YELLOW_BOLD);
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

    public static void printShutdownAnimation() {
        System.out.println();
        System.out.println(RED_BOLD + "Termination Signal Received..." + RESET);

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

    // =========================================================
    // PAGINATED CARD VIEW (CONTACT)
    // =========================================================

    public static void showPaginatedContactView(List<Contact> contacts, Scanner scanner, String title) {
        if (contacts == null || contacts.isEmpty()) {
            printError("No contacts to display.");
            return;
        }

        int currentIndex = 0;
        boolean viewing = true;

        while (viewing) {
            clearConsole();

            Contact current = contacts.get(currentIndex);

            // Header Box
            System.out.println(BLUE_BOLD + "╔" + repeat("═", CARD_WIDTH - 2) + "╗" + RESET);

            String titleLine = "║  " + YELLOW_BOLD + title + BLUE_BOLD;
            int titlePadding = CARD_WIDTH - 1 - stripAnsi(titleLine).length();
            if (titlePadding < 0) titlePadding = 0;
            System.out.println(titleLine + repeat(" ", titlePadding) + "║" + RESET);

            System.out.println(BLUE_BOLD + "╠" + repeat("═", CARD_WIDTH - 2) + "╣" + RESET);

            String countLine = "║  " + RESET + "Contact " + CYAN_BOLD + (currentIndex + 1) +
                    RESET + " of " + CYAN_BOLD + contacts.size() + BLUE_BOLD;
            int countPadding = CARD_WIDTH - 1 - stripAnsi(countLine).length();
            if (countPadding < 0) countPadding = 0;
            System.out.println(countLine + repeat(" ", countPadding) + "║" + RESET);

            System.out.println(BLUE_BOLD + "╚" + repeat("═", CARD_WIDTH - 2) + "╝" + RESET);
            System.out.println();

            // Display Contact Card
            displayContactCard(current);

            // Navigation Instructions
            System.out.println();
            System.out.println(LIGHT_GRAY + repeat("━", CARD_WIDTH) + RESET);
            System.out.println();

            if (currentIndex == 0 && contacts.size() == 1) {
                System.out.println(YELLOW_BOLD + "  ⚠  This is the only contact" + RESET);
            } else if (currentIndex == 0) {
                System.out.println(GREEN_BOLD + "  →  [D] Next Contact" + RESET);
            } else if (currentIndex == contacts.size() - 1) {
                System.out.println(GREEN_BOLD + "  ←  [A] Previous Contact" + RESET);
            } else {
                System.out.println(GREEN_BOLD + "  ←  [A] Previous  |  [D] Next  →" + RESET);
            }

            System.out.println(RED_BOLD + "  ✕  [EXIT] Return to Menu" + RESET);
            System.out.println();
            System.out.print(CYAN_BOLD + "▶ " + RESET + "Your choice: ");

            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "a" -> {
                    if (currentIndex > 0) {
                        currentIndex--;
                    } else {
                        System.out.println(YELLOW_BOLD + "\n⚠  You are at the first contact!" + RESET);
                        sleep(1200);
                    }
                }
                case "d" -> {
                    if (currentIndex < contacts.size() - 1) {
                        currentIndex++;
                    } else {
                        System.out.println(YELLOW_BOLD + "\n⚠  You are at the last contact!" + RESET);
                        sleep(1200);
                    }
                }
                case "exit" -> viewing = false;
                default -> {
                    System.out.println(RED_BOLD + "\n❌ Invalid input! Use A/D/EXIT" + RESET);
                    sleep(1200);
                }
            }
        }
    }

    private static void displayContactCard(Contact c) {
        String firstName = (c.getName() != null) ? c.getName() : "N/A";
        String lastName = (c.getSurname() != null) ? c.getSurname() : "N/A";
        String fullName = firstName + " " + lastName;

        System.out.println(CYAN_BOLD + "┌" + repeat("─", CARD_WIDTH - 2) + "┐" + RESET);

        String nameLine = "│  " + GREEN_BOLD + "👤 " + fullName.toUpperCase() + CYAN_BOLD;
        int namePadding = CARD_WIDTH - 1 - stripAnsi(nameLine).length();
        if (namePadding < 0) namePadding = 0;
        System.out.println(nameLine + repeat(" ", namePadding) + "│" + RESET);

        System.out.println(CYAN_BOLD + "├" + repeat("─", CARD_WIDTH - 2) + "┤" + RESET);

        printCardField("ID", String.valueOf(c.getContactId()), LIGHT_GRAY);
        printCardField("First Name", c.getName(), YELLOW_BOLD);
        printCardField("Last Name", c.getSurname(), YELLOW_BOLD);

        System.out.println(CYAN_BOLD + "├" + repeat("─", CARD_WIDTH - 2) + "┤" + RESET);

        printCardField("📞 Primary Phone", c.getPrimaryPhone(), GREEN_BOLD);
        printCardField("📞 Secondary Phone", c.getSecondaryPhone(), GREEN_BOLD);

        System.out.println(CYAN_BOLD + "├" + repeat("─", CARD_WIDTH - 2) + "┤" + RESET);

        String birthStr = (c.getBirthdate() != null) ? c.getBirthdate().toString() : "N/A";
        printCardField("🎂 Birth Date", birthStr, BLUE_BOLD);

        System.out.println(CYAN_BOLD + "├" + repeat("─", CARD_WIDTH - 2) + "┤" + RESET);

        printCardField("📧 Email", c.getEmail(), CYAN_BOLD);

        String linkedIn = (c.getLinkedinUrl() != null && !c.getLinkedinUrl().isEmpty())
                ? (c.getLinkedinUrl().length() > 50 ? c.getLinkedinUrl().substring(0, 47) + "..." : c.getLinkedinUrl())
                : "N/A";
        printCardField("🔗 LinkedIn", linkedIn, BLUE_BOLD);

        System.out.println(CYAN_BOLD + "├" + repeat("─", CARD_WIDTH - 2) + "┤" + RESET);

        String address = (c.getAddress() != null && !c.getAddress().isEmpty()) ? c.getAddress() : "N/A";
        printCardField("📍 Address", address, YELLOW_BOLD);

        System.out.println(CYAN_BOLD + "└" + repeat("─", CARD_WIDTH - 2) + "┘" + RESET);
    }

    private static void printCardField(String label, String value, String color) {
        if (value == null || value.trim().isEmpty()) {
            value = "N/A";
        }

        String displayValue = value;
        if (displayValue.length() > 55) {
            displayValue = displayValue.substring(0, 52) + "...";
        }

        String visibleLine = "│  " +
                String.format("%-20s", label) + ": " +
                displayValue;

        int padding = CARD_WIDTH - 1 - visibleLine.length();
        if (padding < 0) padding = 0;

        System.out.println(
                CYAN_BOLD + "│  " + RESET +
                        String.format("%-20s", label) + ": " +
                        color + displayValue + RESET +
                        repeat(" ", padding) +
                        CYAN_BOLD + "│" + RESET
        );
    }

    // (Tablo görünümü istersen başka yerlerde kullanmak için kalsın)
    public static void printSearchResultsTable(List<Contact> contacts) {
        String line = repeat("━", 130);

        System.out.println(LIGHT_GRAY + line + RESET);
        System.out.printf(
                YELLOW_BOLD +
                        "%-4s %-15s %-15s %-15s %-12s %-25s %-25s%n" +
                        RESET,
                "ID", "First Name", "Last Name", "Phone", "Birthdate", "Address", "Email"
        );
        System.out.println(LIGHT_GRAY + line + RESET);

        if (contacts.isEmpty()) {
            System.out.println(RED_BOLD + "No records found." + RESET);
        } else {
            for (Contact c : contacts) {
                String birthStr = (c.getBirthdate() == null) ? "-" : c.getBirthdate().toString();

                System.out.printf(
                        "%-4d %-15s %-15s %-15s %-12s %-25s %-25s%n",
                        c.getContactId(),
                        truncate(c.getName(), 15),
                        truncate(c.getSurname(), 15),
                        truncate(c.getPrimaryPhone(), 15),
                        birthStr,
                        truncate(c.getAddress(), 25),
                        truncate(c.getEmail(), 25)
                );
            }
        }

        System.out.println(LIGHT_GRAY + line + RESET);
    }

    // =========================================================
    // SECTION HEADERS / SECTIONED MENUS (SEARCH vs)
    // =========================================================

    public static void printSectionHeader(String title) {
        clearConsole();
        System.out.println(YELLOW_BOLD + "╔" + repeat("═", 40) + "╗" + RESET);

        String titleLine = "║  " + title;
        int padding = 38 - stripAnsi(title).length();
        if (padding < 0) padding = 0;
        System.out.println(YELLOW_BOLD + titleLine + repeat(" ", padding) + "║" + RESET);

        System.out.println(YELLOW_BOLD + "╚" + repeat("═", 40) + "╝" + RESET);
        System.out.println();
    }

    public static void printSectionedMenu(String title, String[][] sections, String[] sectionTitles) {
        clearConsole();

        System.out.println(YELLOW_BOLD + "╔" + repeat("═", 40) + "╗" + RESET);

        String titleLine = "║  " + title;
        int titlePadding = 38 - stripAnsi(title).length();
        if (titlePadding < 0) titlePadding = 0;
        System.out.println(YELLOW_BOLD + titleLine + repeat(" ", titlePadding) + "║" + RESET);

        System.out.println(YELLOW_BOLD + "╚" + repeat("═", 40) + "╝" + RESET);
        System.out.println();

        for (int i = 0; i < sections.length; i++) {
            if (sectionTitles != null && i < sectionTitles.length && !sectionTitles[i].isEmpty()) {
                System.out.println(CYAN_BOLD + "┌─ " + sectionTitles[i] +
                        repeat("─", 37 - sectionTitles[i].length()) + "┐" + RESET);
            }

            for (String line : sections[i]) {
                System.out.println("  " + line);
            }

            if (sectionTitles != null && i < sectionTitles.length && !sectionTitles[i].isEmpty()) {
                System.out.println(CYAN_BOLD + "└" + repeat("─", 40) + "┘" + RESET);
            }
            System.out.println();
        }
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private static String repeat(String str, int times) {
        if (times <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private static String stripAnsi(String str) {
        if (str == null) return "";
        return str.replaceAll("\033\\[[;\\d]*m", "");
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }
}
