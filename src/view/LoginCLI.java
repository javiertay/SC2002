package view;

import controller.AuthController;

import java.io.Console;
import java.util.Scanner;
import model.User;

/**
* Handles the login interface for users in the Build-To-Order Management System.
* Prompts users for their NRIC and password, validates credentials,
* and handles authentication logic including retry attempts.
* 
* Also provides a welcome screen on program startup.
* 
* @author Javier
* @author Qi Ming
* @version 1.0
*/
public class LoginCLI {
    private final AuthController authController;
    private final Scanner scanner;
    private final String NRIC_REGEX = "^[ST]\\d{7}[A-Z]$";

    /**
    * Constructs a LoginCLI with the specified authentication controller and scanner.
    *
    * @param authController The authentication controller to validate users.
    * @param scanner The scanner to read user input from console.
    */
    public LoginCLI(AuthController authController, Scanner scanner) {
        this.authController = authController;
        this.scanner = scanner;
    }

    /**
    * Prompts the user to log in with NRIC and password.
    * Validates NRIC format and checks credentials using the AuthController.
    * Allows up to 3 password attempts before aborting login.
    *
    * @return The authenticated {@code User}, or {@code null} if login fails or is exited.
    */
    public User promptLogin() {
        System.out.print("Enter NRIC (or 'exit'): ");
        String nric = scanner.nextLine().trim().toUpperCase();
        if (nric.equalsIgnoreCase("exit")) return null;
    
        if (!nric.matches(NRIC_REGEX)) {
            System.out.println("Invalid NRIC format. Please try again.");
            return promptLogin();
        }
    
        User user = authController.getUserByNRIC(nric);
        if (user == null) {
            System.out.println("No account found with this NRIC.");
            return promptLogin();
        }
    
        int attempts = 0;
        final int maxAttempts = 3;
    
        while (attempts < maxAttempts) {
            String password;
            Console console = System.console();
            if (console == null) {
                System.out.print("Enter Password: ");
                password = scanner.nextLine();
            } else {
                char[] passwordChars = console.readPassword("Enter Password: ");
                password = new String(passwordChars);
            }
    
            User authenticatedUser = authController.login(nric, password);
            if (authenticatedUser != null) {
                return authenticatedUser;
            }
    
            attempts++;
            int attemptsLeft = maxAttempts - attempts;
            if (attemptsLeft > 0) {
                System.out.println("Incorrect password. Attempts left: " + attemptsLeft);
            }
        }
    
        System.out.println("Too many failed attempts. Returning to main menu.");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return promptLogin();
    }
    
    /**
    * Displays the welcome banner for the Build-To-Order Management System.
    */
    public void welcomeScreen(){
        System.out.println("╔═════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                          ███████╗  ████████╗  ██████╗  ███╗   ███╗ ███████╗                         ║");
        System.out.println("║                          ██╔═══██╗ ╚══██╔══╝ ██╔═══██╗ ████╗ ████║ ██╔════╝                         ║");
        System.out.println("║                          ███████═╝    ██║    ██║   ██║ ██╔████╔██║ ███████╗                         ║");
        System.out.println("║                          ██╔═══██╗    ██║    ██║   ██║ ██║╚██╔╝██║ ╔════██╝                         ║");
        System.out.println("║                          ███████╔╝    ██║    ╚██████╔╝ ██║ ╚═╝ ██║ ███████╗                         ║");
        System.out.println("║                          ╚══════╝     ╚═╝     ╚═════╝  ╚═╝     ╚═╝ ╚══════╝                         ║");
        System.out.println("║                                                                                                     ║");
        System.out.println("║                         Welcome to Build-To-Order Management System (BTOMS)                         ║");
        System.out.println("║                                                                                                     ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println( );
        System.out.println("Login to proceed");
    }
}
