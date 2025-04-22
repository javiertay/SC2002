package util;

import java.util.Scanner;

public class InputUtil {
    public static int readInt(Scanner scanner) {
        while (true) {
            try {
                System.out.print("Select an option: ");
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
