package view;

import java.util.Scanner;

import controller.ApplicationController;
import model.*;

public class ApplicationCLI {
    private final ApplicationController applicationController;
    private final Scanner scanner;

    public ApplicationCLI(ApplicationController applicationController) {
        this.applicationController = applicationController;
        this.scanner = new Scanner(System.in);
    }    

    public void start(Applicant applicant) {
        int choice;
        do {
            showMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> viewAllAvailableProject(applicant);
                case 2 -> submitApplication(applicant);
                case 3 -> viewApplicationStatus(applicant);
                case 4 -> withdrawApplication(applicant);
                case 5 -> {
                    System.out.println("Exiting Application Management.");
                    System.out.println();
                    }
                default -> System.out.println("Invalid option. Please try again.");
            }

        } while (choice != 5);
    }

    private void showMenu() {
        System.out.println("\n=== HDB Application Manager ===");
        System.out.println("1. View Available Projects");
        System.out.println("2. Apply for a Project");
        System.out.println("3. View Application Status");
        System.out.println("4. Withdraw Application");
        System.out.println("5. Back");
        System.out.print("Select an option: ");  
    }

    private void viewAllAvailableProject(Applicant applicant){
        applicationController.getAllAvailableProjects(applicant);
    }

    private void submitApplication(Applicant applicant) {
        System.out.print("\nEnter Project Name: ");
        String projectName = scanner.nextLine().trim();
        
        Project project = ProjectRegistry.getProjectByName(projectName);
        String flatType;

        if (applicant.getMaritalStatus().equalsIgnoreCase("single")) {
            flatType = "2-Room";
            System.out.println("As a single applicant, you will automatically be assigned a 2-Room flat.");
        } else if (applicant.getMaritalStatus().equalsIgnoreCase("married")) {
            System.out.println("Available Flat Types:");
            project.getFlatTypes().forEach((type, flatTypeObj) -> {
                int remainingUnits = flatTypeObj.getRemainingUnits();
                System.out.println("- " + type + ": " + remainingUnits + " units left");
            });

            System.out.print("Enter Flat Type (e.g., 2-Room or 3-Room): ");
            flatType = scanner.nextLine().trim();
        } else {
            System.out.println("You are not eligible to apply for a flat.");
            return;
        }

        boolean success = applicationController.submitApplication(applicant, projectName, flatType);
        if (success) {
            System.out.println("Application process completed successfully!");
        } else {
            System.out.println("Application failed. Please try again.");
        }
    }

    private void viewApplicationStatus(Applicant applicant) {
        applicationController.viewApplicationStatus(applicant);
    }

    private void withdrawApplication(Applicant applicant) {
        System.out.print("Are you sure you want to withdraw your application? (Y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("y")) {
            applicationController.reqToWithdrawApp(applicant);
        } else {
            System.out.println("Withdrawal cancelled.");
        }
    }
}
