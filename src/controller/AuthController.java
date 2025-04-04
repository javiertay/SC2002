package controller;

import model.User;

import java.util.HashMap;
import java.util.Map;

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

        if (user == null) {
            System.out.println("NRIC not found.");
            return null;
        }

        if (!user.getPassword().equals(password)) {
            System.out.println("Incorrect password.");
            return null;
        }

        System.out.println("Login successful! Welcome, " + user.getName() + ".");
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
}
