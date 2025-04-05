package view;

import controller.ApplicantController;
import controller.ApplicationController;
import controller.AuthController;
import controller.EnquiryController;
import model.Applicant;
import model.Project;
import model.ProjectRegistry;

import java.util.Scanner;

public class ApplicantCLI {

    private final Applicant applicant;
    private final ApplicationController applicationController;
    private final EnquiryController enquiryController;
    private final AuthController authController;
    private final ApplicantController applicantController;
    private final Scanner scanner;

    public ApplicantCLI(Applicant applicant, ApplicantController applicantController, ApplicationController applicationController, EnquiryController enquiryController, AuthController authController) {
        this.applicant = applicant;
        this.applicantController = applicantController;
        this.applicationController = applicationController;
        this.enquiryController = enquiryController;
        this.authController = authController;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice;
        do {
            showMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> applicantController.getAvailableProjects(applicant);
                case 2 -> applyForProject();
                case 3 -> applicationController.viewApplicationStatus(applicant);
                case 4 -> applicationController.withdrawApplication(applicant);
                case 5 -> manageEnquiries();
                case 6 -> changePassword();
                case 7 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 7);
    }

    private void showMenu() {
        System.out.println("\n=== Applicant Menu ===");
        System.out.println("1. View Available Projects");
        System.out.println("2. Apply for a Project");
        System.out.println("3. View Application Status");
        System.out.println("4. Withdraw Application");
        System.out.println("5. Manage Enquiries");
        System.out.println("6. Change Password");
        System.out.println("7. Logout");
        System.out.print("Select an option: ");  
    }

    private void applyForProject() {
        System.out.print("\nEnter Project Name: ");
        String projectName = scanner.nextLine();

        Project project = ProjectRegistry.getProjectByName(projectName);
        if (project != null) {
            System.out.println("Available Flat Types:");
            for (String type : project.getFlatTypes().keySet()) {
                System.out.println("- " + type);
            }
            System.out.print("Enter Flat Type (2-Room or 3-Room): ");
            String flatType = scanner.nextLine();
            applicationController.submitApplication(applicant, projectName, flatType);
        }
    }

    private void manageEnquiries() {
        EnquiryCLI enquiryCLI = new EnquiryCLI(applicant, enquiryController);
        enquiryCLI.start();
    }

    private void changePassword() {
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
    
        if (!applicant.getPassword().equals(currentPassword)) {
            System.out.println("Incorrect current password.");
            return;
        }
    
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
    
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
    
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }
    
        boolean success = authController.changePassword(applicant, newPassword);
        if (success) {
            System.out.println("Password successfully changed.");
        } else {
            System.out.println("Password change failed.");
        }
    }
    
}
