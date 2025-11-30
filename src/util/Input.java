package util;

import java.util.Scanner;

public class Input {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static String getStringInput() {
        return SCANNER.nextLine().trim();
    }

    public static int getIntInput() {
        while (true) {
            String input = getStringInput();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid integer: ");
            }
        }
    }

    public static int getIntInputInRange(int min, int max) {
        while (true) {
            int value = getIntInput();
            if (value < min || value > max) {
                System.out.print("Please enter a number between " + min + " and " + max + ": ");
            } else {
                return value;
            }
        }
    }
}
