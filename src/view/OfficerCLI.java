package view;

import controller.OfficerController;
import controller.EnquiryController;
import controller.ApplicationController;
import controller.AuthController;
import model.*;
import model.HDBOfficer.RegistrationStatus;

import java.util.Map;
import java.util.Scanner;

public class OfficerCLI {
    private final HDBOfficer officer;
    private final AuthController authController;
    private final OfficerController officerController;
    private final EnquiryController enquiryController;
    private final ApplicationController applicationController;
    private final ApplicationCLI applicationCLI;
    private final Scanner scanner;

    public OfficerCLI(HDBOfficer officer, OfficerController officerController, AuthController authController, EnquiryController enquiryController, ApplicationCLI applicationCLI, ApplicationController applicationController) {
        this.officer = officer;
        this.officerController = officerController;
        this.authController = authController;
        this.enquiryController = enquiryController;
        this.applicationController = applicationController;
        this.applicationCLI = applicationCLI;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice;
        do {
            showMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> applicationCLI.start(officer);    
                case 2 -> registerForProject();
                case 3 -> viewRegistrationStatus();
                case 4 -> {
                            if (!isActionAllowed()) break;
                            viewAssignedProjectDetails();
                        }
                case 5 -> {
                            if (!isActionAllowed()) break;
                            ApplicationManagementCLI applicationManagementCLI = new ApplicationManagementCLI(officer, applicationController, officerController);
                            applicationManagementCLI.start();
                        }
                case 6 -> {
                            if (!isActionAllowed()) break;
                            manageEnquiries();
                        }
                case 7 -> authController.promptPasswordChange(officer, scanner);
                case 8 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 8);
    }

    private void showMenu() {
        System.out.println("\n=== HDB Officer Menu ===");
        System.out.println("1. Manage HDB Applications");
        System.out.println("2. Register for a Project");
        System.out.println("3. View Registration Status");

        if (officer.isAssigned()) {
            System.out.println("4. View Assigned Project Details");
            System.out.println("5. Flat Selection (Assign Flat)");
            System.out.println("6. Manage Enquiries");
        } else {
            System.out.println("4. View Assigned Project Details (Unavailable)");
            System.out.println("5. Flat Selection (Unavailable)");
            System.out.println("6. Manage Enquiries (Unavailable)");
        }

        System.out.println("7. Change Password");
        System.out.println("8. Logout");
        System.out.print("Select an option: ");
    }

    private void registerForProject() {
        System.out.print("Enter Project Name to register: ");
        String projectName = scanner.nextLine().trim();
        officerController.reqToHandleProject(officer, projectName);
    }

    private void viewRegistrationStatus() {
        Map<String, RegistrationStatus> registrations = officer.getAllRegistrations();
        if (registrations.isEmpty()) {
            System.out.println("No registrations found.");
            return;
        }

        System.out.println("\nYour Registration Status:");
        registrations.forEach((project, status) ->
                System.out.println("- Project: " + project + " | Status: " + status));
    }

    private void viewAssignedProjectDetails() {
        if (!officer.isAssigned()) {
            System.out.println("You are not assigned to any project yet.");
            return;
        }

        Project project = officerController.viewAssignedProjectDetails(officer);
        if (project == null) {
            System.out.println("Assigned project details not found.");
            return;
        }

        System.out.println("\n=== Project Details ===");
        System.out.println("Project Name: " + project.getName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Application Period: " + project.getOpenDate() + " to " + project.getCloseDate());
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("Flat Types:");
        project.getFlatTypes().forEach((type, flat) ->
                System.out.println("- " + type + ": " + flat.getRemainingUnits() + " units available"));
    }

    private void manageEnquiries() {
        EnquiryCLI enquiryCLI = new EnquiryCLI(officer, enquiryController);
        enquiryCLI.start();
    }

    private boolean isActionAllowed() {
        if (!officer.isAssigned()) {
            System.out.println("You are not assigned to any project yet.");
            return false;
        }
        return true;
    }
}
