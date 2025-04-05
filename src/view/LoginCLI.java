package view;

import controller.AuthController;
import model.User;

import java.util.Scanner;

public class LoginCLI {
    private final AuthController authController;
    private final Scanner scanner;
    private final String NRIC_REGEX = "^[ST]\\d{7}[A-Z]$";

    public LoginCLI(AuthController authController, Scanner scanner) {
        this.authController = authController;
        this.scanner = scanner;
    }

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
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
    
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
    
}
