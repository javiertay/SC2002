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
        
        if (applicant instanceof HDBOfficer officer) {
            String assignedProjectName = officer.getAssignedProject();
            if (projectName.equalsIgnoreCase(assignedProjectName)){
                System.out.println("You cannot apply to projects you are handling!");
                return;
            }
        }

        Project project = ProjectRegistry.getProjectByName(projectName);
        if (project == null) {
            System.out.println("Project not found.");
            return;
        }

        System.out.println("Available Flat Types:");
        for (String type : project.getFlatTypes().keySet()) {
            System.out.println("- " + type);
        }

        System.out.print("Enter Flat Type (e.g., 2-Room or 3-Room): ");
        String flatType = scanner.nextLine().trim();

        applicationController.submitApplication(applicant, projectName, flatType);
    }

    private void viewApplicationStatus(Applicant applicant) {
        applicationController.viewApplicationStatus(applicant);
    }

    private void withdrawApplication(Applicant applicant) {
        System.out.print("Are you sure you want to withdraw your application? (Y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("y")) {
            applicationController.withdrawApplication(applicant);
        } else {
            System.out.println("Withdrawal cancelled.");
        }
    }
}
