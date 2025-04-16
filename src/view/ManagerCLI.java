package view;

import controller.ApplicationController;
import controller.AuthController;
import controller.EnquiryController;
import controller.ManagerController;
import model.*;

import java.util.Scanner;

public class ManagerCLI {
    private final HDBManager manager;
    private final ManagerController managerController;
    private final AuthController authController;
    private final EnquiryController enquiryController;
    private final ApplicationController applicationController;
    private final Scanner scanner;

    public ManagerCLI(HDBManager manager, ManagerController managerController, AuthController authController, EnquiryController enquiryController, ApplicationController applicationController) {
        this.applicationController = applicationController;
        this.manager = manager;
        this.managerController = managerController;
        this.authController = authController;
        this.enquiryController = enquiryController;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice;
        do {
            showMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> {
                    ProjectManagementCLI projectManagementCLI = new ProjectManagementCLI(manager, managerController, scanner);
                    projectManagementCLI.start();
                }
                case 2 -> {
                    ApplicationManagementCLI applicationManagementCLI = new ApplicationManagementCLI(manager, applicationController);
                    applicationManagementCLI.start();
                }
                case 3 -> approveOfficerRegistration();
                // case 4 -> approveWithdrawal();
                // case 5 -> generateReport();
                case 4 -> manageEnquiries();
                case 5 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 5);
    }

    private void showMenu() {
        System.out.println("\n=== HDB Manager Menu ===");
        System.out.println("1. HDB Project Management Hub");
        System.out.println("2. Application Management Hub");
        System.out.println("3. Approve Officer Registration");
        System.out.println("4. View and Reply to Enquiries");
        System.out.println("5. Logout");
        System.out.print("Select an option: ");
    }

    private void approveOfficerRegistration() {
        // managerController.getAllOfficersByStatus();
        while (true) {
            managerController.viewPendingOfficerApplications(manager); // returns pending officer application by the manager project
    
            System.out.print("Enter Officer NRIC to process (or 'back' to return): ");
            String nric = scanner.nextLine().trim().toUpperCase();
    
            if (nric.equalsIgnoreCase("back")) {
                return;
            }
    
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
        EnquiryCLI enquiryCLI = new EnquiryCLI(manager, enquiryController);
        enquiryCLI.start();
    }
}