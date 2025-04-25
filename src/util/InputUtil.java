package util;

import java.util.Scanner;

/**
* Utility for safely reading typed input from the console, such as integers.
* 
* @author Javier
* @version 1.0
*/
public class InputUtil {
    /**
    * Reads a user input and converts it to an integer.
    *
    * @param scanner The Scanner object for reading.
    * @return The integer input or -1 if invalid.
    */
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
