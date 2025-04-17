package view;

import controller.OfficerController;
import controller.EnquiryController;
import controller.ApplicationController;
import model.*;
import model.HDBOfficer.RegistrationStatus;

import java.util.Map;
import java.util.Scanner;

public class OfficerCLI {

    private final HDBOfficer officer;
    private final OfficerController officerController;
    private final EnquiryController enquiryController;
    private final ApplicationController applicationController;
    private final ApplicationCLI applicationCLI;
    private final Scanner scanner;

    public OfficerCLI(HDBOfficer officer, OfficerController officerController, EnquiryController enquiryController, ApplicationCLI applicationCLI, ApplicationController applicationController) {
        this.applicationController = applicationController;
        this.applicationCLI = applicationCLI;
        this.officer = officer;
        this.officerController = officerController;
        this.enquiryController = enquiryController;
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
                            flatSelectionWorkflow();
                        }
                case 6 -> {
                            if (!isActionAllowed()) break;
                            generateReceipt();
                        }
                case 7 -> {
                            if (!isActionAllowed()) break;
                            manageEnquiries();
                        }
                case 8 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 8);
    }

    private void showMenu() {
        System.out.println("\n=== HDB Officer Menu ===");
        System.out.println("1. HDB Application Manager");
        System.out.println("2. Register for a Project");
        System.out.println("3. View Registration Status");

        if (officer.isAssigned()) {
            System.out.println("4. View Assigned Project Details");
            System.out.println("5. Flat Selection (Assign Flat)");
            System.out.println("6. Generate Flat Booking Receipt");
            System.out.println("7. Manage Enquiries");
        } else {
            System.out.println("4. View Assigned Project Details (Unavailable)");
            System.out.println("5. Flat Selection (Unavailable)");
            System.out.println("6. Generate Flat Booking Receipt (Unavailable)");
            System.out.println("7. Manage Enquiries (Unavailable)");
        }

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

    private void flatSelectionWorkflow() {
        if (!officer.isAssigned()) {
            System.out.println("You are not assigned to any project yet.");
            return;
        }

        System.out.print("Enter Applicant NRIC: ");
        String applicantNric = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Flat Type (e.g., 2-Room, 3-Room): ");
        String flatType = scanner.nextLine().trim();

        boolean success = officerController.assignFlat(officer, applicantNric, flatType);
        if (success) {
            Application application = ApplicationRegistry.getApplicationByNRIC(applicantNric);
            officerController.generateReceipt(application);
        }
    }

    private void generateReceipt() {
        System.out.print("Enter Applicant NRIC to generate receipt: ");
        String applicantNric = scanner.nextLine().trim().toUpperCase();

        Application application = ApplicationRegistry.getApplicationByNRIC(applicantNric);
        if (application == null) {
            System.out.println("No application found for this NRIC.");
            return;
        }

        if (!application.getProject().getName().equalsIgnoreCase(officer.getAssignedProject())) {
            System.out.println("This application does not belong to your assigned project.");
            return;
        }

        officerController.generateReceipt(application);
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
