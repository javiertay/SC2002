package view;

import controller.AuthController;
import controller.EnquiryController;
import controller.ManagerController;
import model.*;
import util.Breadcrumb;
import util.InputUtil;

import java.util.List;
import java.util.Scanner;

public class ManagerCLI {
    private final HDBManager manager;
    private final ManagerController managerController;
    private final AuthController authController;
    private final EnquiryController enquiryController;
    private final ApplicationManagementCLI applicationManagementCLI;
    private final Scanner scanner;
    private Breadcrumb breadcrumb;

    public ManagerCLI(HDBManager manager, ManagerController managerController, AuthController authController, EnquiryController enquiryController, ApplicationManagementCLI applicationManagementCLI) {
        this.manager = manager;
        this.managerController = managerController;
        this.authController = authController;
        this.enquiryController = enquiryController;
        this.applicationManagementCLI = applicationManagementCLI;
        this.scanner = new Scanner(System.in);
        this.breadcrumb = new Breadcrumb();
    }

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

    private void showMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. HDB Project Management Hub");
        System.out.println("2. Application Management Hub");
        System.out.println("3. Approve Officer Registration");
        System.out.println("4. View and Reply to Enquiries");
        System.out.println("5. Change Password");
        System.out.println("0. Logout");
    }

    private void approveOfficerRegistration() {
        // managerController.getAllOfficersByStatus();
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

    private void manageEnquiries() {
        breadcrumb.push("Manager Enquiry Hub");
        EnquiryCLI enquiryCLI = new EnquiryCLI(manager, enquiryController, breadcrumb);
        enquiryCLI.start();
        breadcrumb.pop(); // Return to Manager Menu after exiting Enquiry Hub
    }

    private void showDashboard() {
        List<Application> applications = ApplicationRegistry.getPendingApplicationsByProject(manager.getAssignedProject());
        int pendingAppCount = applications.size();

        List<HDBOfficer> officers = managerController.getPendingOfficerApplications(manager);
        int pendingOfficerApprovalCount = officers.size();

        List<Enquiry> enquiries = enquiryController.getProjectEnquiries(manager.getAssignedProject());
        int totalEnquiries = enquiries.size();

        System.out.println("\nWelcome back " + manager.getName() + "!");
        System.out.println(" - " + pendingAppCount + " applications pending for your review.");
        System.out.println(" - " + pendingOfficerApprovalCount + " officer(s) pending approval for your project");
        System.out.println(" - " + totalEnquiries + " enquiries for your project");
    }
}