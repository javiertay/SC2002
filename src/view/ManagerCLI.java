package view;

import controller.AuthController;
import controller.EnquiryController;
import controller.ManagerController;
import model.*;
import util.Breadcrumb;
import util.InputUtil;

import java.util.List;
import java.util.Scanner;

/**
* CLI interface for HDB Managers.
* 
* Allows managers to manage projects, applications, officer registrations,
* and respond to enquiries. Also displays a dashboard with pending items upon login.
* 
* Features:
* - Access to Project and Application Management Hubs
* - Officer registration approval
* - Enquiry viewing and response
* - Password change functionality
* 
* @author Javier
* @version 1.0
*/
public class ManagerCLI {
    private final HDBManager manager;
    private final ManagerController managerController;
    private final AuthController authController;
    private final EnquiryController enquiryController;
    private final ApplicationManagementCLI applicationManagementCLI;
    private final Scanner scanner;
    private Breadcrumb breadcrumb;

    /**
    * Constructs the ManagerCLI with necessary controllers and components.
    *
    * @param manager The logged-in HDB manager.
    * @param managerController Controller for manager-specific operations.
    * @param authController Controller handling authentication and password updates.
    * @param enquiryController Controller for managing enquiries.
    * @param applicationManagementCLI CLI for managing applications.
    */
    public ManagerCLI(HDBManager manager, ManagerController managerController, AuthController authController, EnquiryController enquiryController, ApplicationManagementCLI applicationManagementCLI) {
        this.manager = manager;
        this.managerController = managerController;
        this.authController = authController;
        this.enquiryController = enquiryController;
        this.applicationManagementCLI = applicationManagementCLI;
        this.scanner = new Scanner(System.in);
        this.breadcrumb = new Breadcrumb();
    }

    /**
    * Starts the main manager menu loop.
    * Displays dashboard and handles user selection routing.
    */
    public void start() {
        breadcrumb.push("HDB Manager Menu");
        showDashboard();
        
        int choice;
        do {
            showMenu();
            choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> {
                    breadcrumb.push("Project Management Hub");
                    ProjectManagementCLI projectManagementCLI = new ProjectManagementCLI(manager, managerController, scanner, breadcrumb);
                    projectManagementCLI.start();
                    breadcrumb.pop(); // Return to Manager Menu after exiting Project Management Hub
                }
                case 2 -> {
                    breadcrumb.push("Application Management Hub");
                    applicationManagementCLI.setBreadcrumb(breadcrumb);
                    applicationManagementCLI.start();
                    breadcrumb.pop(); // Return to Manager Menu after exiting Application Management Hub
                }
                case 3 -> approveOfficerRegistration();
                case 4 -> manageEnquiries();
                case 5 -> authController.promptPasswordChange(manager, scanner);
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 0);
    }

    /**
    * Displays the manager's main menu options.
    */
    private void showMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. HDB Project Management Hub");
        System.out.println("2. Application Management Hub");
        System.out.println("3. Approve Officer Registration");
        System.out.println("4. View and Reply to Enquiries");
        System.out.println("5. Change Password");
        System.out.println("0. Logout");
    }

    /**
    * Handles the officer registration approval flow.
    * Allows the manager to approve or reject pending officer applications
    * for their assigned projects.
    */
    private void approveOfficerRegistration() {
        // managerController.getAllOfficersByStatus();
        List<HDBOfficer> pendingOfficers = managerController.getPendingOfficerApplications(manager);

        if (pendingOfficers.isEmpty()) {
            System.out.println("No pending officer applications for your project.");
        } else {

            while (true) {
                managerController.viewPendingOfficerApplications(manager); // returns pending officer application by the manager project
        
                System.out.print("Enter Officer NRIC to process (or 'back' to return): ");
                String nric = scanner.nextLine().trim().toUpperCase();
        
                if (nric.equalsIgnoreCase("back") || nric.isEmpty()) return;
        
                System.out.print("Approve or Reject? (A/R): ");
                String decision = scanner.nextLine().trim().toUpperCase();
        
                if (decision.equals("A")) {
                    managerController.processOfficerApplication(manager, nric, HDBOfficer.RegistrationStatus.APPROVED);
                } else if (decision.equals("R")) {
                    managerController.processOfficerApplication(manager, nric, HDBOfficer.RegistrationStatus.REJECTED);
                } else {
                    System.out.println("Invalid decision. Returning to menu.");
                }
            }
        }
    }

    /**
    * Routes the manager to the enquiry management interface.
    */
    private void manageEnquiries() {
        breadcrumb.push("Manager Enquiry Hub");
        EnquiryCLI enquiryCLI = new EnquiryCLI(manager, enquiryController, breadcrumb);
        enquiryCLI.start();
        breadcrumb.pop(); // Return to Manager Menu after exiting Enquiry Hub
    }

    /**
    * Displays a dashboard summary for the manager upon login.
    * <p>
    * Shows:
    * <ul>
    *     <li>Number of pending applications</li>
    *     <li>Number of withdrawal requests</li>
    *     <li>Number of officer registrations</li>
    *     <li>Number of enquiries (total and replied)</li>
    * </ul>
    * Provides a quick overview of outstanding tasks across all managed projects.
    */
    private void showDashboard() {
        List<String> managedProjects = manager.getManagedProjects();

        List<Application> applications = ApplicationRegistry.getAllApplications()
            .values().stream()
            .flatMap(List::stream)
            .filter(app -> managedProjects.contains(app.getProject().getName()))
            .filter(app -> app.getStatus() == Application.Status.PENDING)
            .toList();

        int pendingAppCount = applications.size();

        List<Application> withdrawalRequests = ApplicationRegistry.getAllApplications()
            .values().stream()
            .flatMap(List::stream)
            .filter(app -> managedProjects.contains(app.getProject().getName()))
            .filter(Application::isWithdrawalRequested)
            .toList();

        int pendingWithdrawals = withdrawalRequests.size();

        List<HDBOfficer> officers = managerController.getPendingOfficerApplications(manager);
        int pendingOfficerApprovalCount = officers.size();

        List<Enquiry> enquiries = managedProjects.stream()
            .flatMap(projectName -> enquiryController.getProjectEnquiries(projectName).stream())
            .toList();
        int totalEnquiries = enquiries.size();

        System.out.println("\nWelcome back " + manager.getName() + "!");
        System.out.println(" - " + pendingAppCount + " application(s) pending approval");
        System.out.println(" - " + pendingWithdrawals + " withdrawal request(s) awaiting your decision");
        System.out.println(" - " + pendingOfficerApprovalCount + " officer(s) pending approval for your project");
        System.out.println(" - " + totalEnquiries + " enquiries for your project");
    }
}