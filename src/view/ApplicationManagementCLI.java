package view;

import model.*;
import controller.ApplicationController;

import java.util.List;
import java.util.Scanner;

public class ApplicationManagementCLI {
    private final ApplicationController applicationController;
    private final HDBManager manager;
    private final HDBOfficer officer;
    private final String projectName;
    private final Scanner scanner = new Scanner(System.in);

    public ApplicationManagementCLI(HDBManager manager, ApplicationController applicationController) {
        this.manager = manager;
        this.officer = null;
        this.projectName = manager.getAssignedProject();
        this.applicationController = applicationController;
    }

    public ApplicationManagementCLI(HDBOfficer officer, ApplicationController applicationController) {
        this.officer = officer;
        this.manager = null;
        this.projectName = officer.getAssignedProject();
        this.applicationController = applicationController;
    }

    public void start() {
        if (officer != null) {
            officerManagement();
        } else if (manager != null) {
            managerManagement();
        }
    }

    private void officerManagement() {
        int choice;
        do {
            officerMenu();
            choice = Integer.parseInt(scanner.nextLine());

            // switch (choice) {
            //     case 1 -> 
            //     case 2 -> 
            //     case 3 -> System.out.println("Exiting Application Management.");
            //     default -> System.out.println("Invalid option. Please try again.");
            // }
        } while (choice != 3);
    }

    private void officerMenu() {
        System.out.println("\n======Application Management======");
        System.out.println("1. Flat Selection");
        System.out.println("2. Generate Booking Receipt");
        System.out.println("3. Back");
        System.out.print("Choice: ");
    }

    private void managerManagement() {
        int choice; 
        do {
            printManagerMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> processApplication();
                case 2 -> approveWithdrawal();
                case 3 -> generateReport();
                case 4 -> System.out.println("Exiting Application Management.");
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 4);
    }

    private void printManagerMenu() {
        System.out.println("\n======Application Management======");
        System.out.println("1. Approve/Reject BTO Application");
        System.out.println("2. Withdrawal Approval");
        System.out.println("3. Generate Booking Report");
        System.out.println("4. Back");
        System.out.print("Choice: ");
    }

    private void processApplication() {
        List<Application> pending = applicationController.getPendingApplicationsByProject(projectName);

        if (pending.isEmpty()) {
            System.out.println("No pending applications for this project.");
            return;
        }

        System.out.println("\n=== Pending Applications ===");
        for (Application app : pending) {
            System.out.println("- NRIC: " + app.getApplicant().getNric() + ", Flat Type: " + app.getFlatType());
        }

        System.out.print("\nProcess Application? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("Enter applicant NRIC: ");
        String nric = scanner.nextLine();
        System.out.print("Approve or Reject (A/R): ");
        String decision = scanner.nextLine().trim().toUpperCase();

        Application.Status status = decision.equals("A") ? Application.Status.SUCCESSFUL : Application.Status.UNSUCCESSFUL;
        applicationController.approveRejectApplication(nric, projectName, manager, status);
    }

    private void approveWithdrawal() {
        List<Application> requests = applicationController.getPendingWithdrawal(projectName);

        if (requests.isEmpty()) {
            System.out.println("No withdrawal requests found.");
            return;
        }

        System.out.println("\n=== Withdrawal Requests ===");
        for (Application app : requests) {
            System.out.println("- NRIC: " + app.getApplicant().getNric() + ", Status: " + app.getStatus());
        }

        System.out.print("\nProcess Withdrawal? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (!confirm.equals("Y")) {
            System.out.println("Returning to menu...");
            return;
        }

        System.out.print("Enter applicant NRIC for withdrawal: ");
        String nric = scanner.nextLine().toUpperCase().trim();
        System.out.print("Approve or Reject (A/R): ");
        String decision = scanner.nextLine().trim().toUpperCase();

        if (decision.equals("A")) {
            applicationController.approveWithdrawal(nric);
        } else if (decision.equals("R")) {
            applicationController.rejectWithdrawal(nric);
        } else {
            System.out.println("Invalid decision. Returning to menu.");
        }
    }

    private void generateReport() {
        BookingFilter filter = collectFilterInput();

        List<Application> result = applicationController.getFilteredApplications(filter);

        if (result.isEmpty()) {
            System.out.println("No matching applications found.");
            return;
        }

        printReport(result);
    }

    private BookingFilter collectFilterInput() {
        BookingFilter filter = new BookingFilter();
        boolean addingFilters = true;
    
        while (addingFilters) {
            System.out.println("\n=== Filter By: ===");
            System.out.println("1. Marital Status");
            System.out.println("2. Flat Type");
            System.out.println("3. Age Range");
            System.out.println("4. Project Name");
            System.out.println("5. Application Status");
            System.out.println("6. Done");
            System.out.print("Choice: ");
    
            int choice = Integer.parseInt(scanner.nextLine());
    
            switch (choice) {
                case 1 -> {
                    System.out.print("Enter marital status (e.g., Married, Single): ");
                    filter.setMaritalStatus(scanner.nextLine().trim());
                }
                case 2 -> {
                    System.out.print("Enter flat type (e.g., 2-Room, 3-Room): ");
                    filter.setFlatType(scanner.nextLine().trim());
                }
                case 3 -> {
                    System.out.print("Enter minimum age: ");
                    filter.setMinAge(Integer.parseInt(scanner.nextLine()));
                    System.out.print("Enter maximum age: ");
                    filter.setMaxAge(Integer.parseInt(scanner.nextLine()));
                }
                case 4 -> {
                    System.out.print("Enter project name: ");
                    filter.setProjectName(scanner.nextLine().trim());
                }
                case 5 -> {
                    System.out.println("Available statuses: PENDING, SUCCESSFUL, BOOKED, UNSUCCESSFUL, WITHDRAWN");
                    System.out.print("Enter status: ");
                    String statusInput = scanner.nextLine().trim().toUpperCase();
                    try {
                        filter.setStatus(Application.Status.valueOf(statusInput));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid status entered.");
                    }
                }
                case 6 -> addingFilters = false;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    
        return filter;
    }

    private void printReport(List<Application> result) {
        System.out.println("\n=== Filtered Application Report ===");
        for (Application app : result) {
            Applicant a = app.getApplicant();
            System.out.println("- Name: " + a.getName()
                    + ", NRIC: " + a.getNric()
                    + ", Age: " + a.getAge()
                    + ", Marital Status: " + a.getMaritalStatus()
                    + ", Flat Type: " + app.getFlatType()
                    + ", Project: " + app.getProject().getName()
                    + ", Status: " + app.getStatus());
        }
    }
}
