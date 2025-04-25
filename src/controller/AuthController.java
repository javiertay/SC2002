package controller;

import model.User;
import util.ExcelWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
* Controller responsible for user authentication and account management.
* Handles login, password changes, and access to user data.
*
* @author Javier 
* @version 1.0
*/
public class AuthController {
    private final Map<String, User> userStore;

    /**
    * Constructs a new AuthController with an empty user store.
    */
    public AuthController() {
        this.userStore = new HashMap<>();
    }

    /**
    * Adds a new user to the system. Called from MainApp after reading all data
    *
    * @param user The user to be added.
    */
    public void addUser(User user) {
        userStore.put(user.getNric(), user);
    }

    /**
    * Attempts to log in a user based on NRIC and password.
    *
    * @param nric The NRIC entered by the user.
    * @param password The password entered by the user.
    * @return The authenticated user if credentials are valid; null otherwise.
    */
    public User login(String nric, String password) {
        User user = userStore.get(nric);

        if (!user.getPassword().equals(password)) {
            return null;
        }

        System.out.println("Login successful!");
        return user;
    }

    /**
    * Changes the password for a given user.
    *
    * @param user The user requesting the password change.
    * @param newPassword The new password to be set.
    * @return True if the password was successfully changed, false otherwise.
    */
    public boolean changePassword(User user, String newPassword) {
        if (user == null) {
            System.out.println("No user logged in.");
            return false;
        }

        user.changePassword(newPassword);
        ExcelWriter.updateUserPassword(user, newPassword);
        System.out.println("Password changed successfully.");
        return true;
    }

    /**
    * Retrieves a user by their NRIC.
    *
    * @param nric The NRIC of the user to retrieve.
    * @return The User object if found, or null if not found.
    */
    public User getUserByNRIC(String nric) {
        return userStore.get(nric);
    }

    /**
    * Retrieves all registered users.
    *
    * @return Map of NRIC to User objects.
    */
    public Map<String, User> getAllUsers() {
        return userStore;
    }

    /**
    * Prompts a user to change their password through the CLI.
    * Verifies the current password and confirms the new password twice.
    *
    * @param user The user requesting the password change.
    * @param scanner The scanner to read user input.
    * @return True if the password was changed successfully; false otherwise.
    */
    public boolean promptPasswordChange(User user, Scanner scanner) {
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
    
        if (!user.getPassword().equals(currentPassword)) {
            System.out.println("Incorrect current password.");
            return false;
        }
    
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
    
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
    
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return false;
        }
    
        return changePassword(user, newPassword);
    }
    
}
