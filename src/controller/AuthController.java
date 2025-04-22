package controller;

import model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AuthController {
    private final Map<String, User> userStore;

    public AuthController() {
        this.userStore = new HashMap<>();
    }

    // Called from MainApp after reading users
    public void addUser(User user) {
        userStore.put(user.getNric(), user);
    }

    // Handles login
    public User login(String nric, String password) {
        User user = userStore.get(nric);

        if (!user.getPassword().equals(password)) {
            return null;
        }

        System.out.println("Login successful!");
        return user;
    }

    // Handles password change
    public boolean changePassword(User user, String newPassword) {
        if (user == null) {
            System.out.println("No user logged in.");
            return false;
        }

        user.changePassword(newPassword);
        System.out.println("Password changed successfully.");
        return true;
    }

    public User getUserByNRIC(String nric) {
        return userStore.get(nric);
    }

    public Map<String, User> getAllUsers() {
        return userStore;
    }

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
