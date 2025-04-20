package view;

import model.*;
import util.Filter;
import controller.ApplicationController;
import controller.OfficerController;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ApplicationManagementCLI {
    private final Filter filter;
    private final ApplicationController applicationController;
    private final OfficerController officerController;
    private final HDBManager manager;
    private final HDBOfficer officer;
    private final String projectName;
    private final Scanner scanner = new Scanner(System.in);

    public ApplicationManagementCLI(HDBManager manager, ApplicationController applicationController, Map<String, Filter> userFilters) {
        this.manager = manager;
        this.officer = null;
        this.projectName = manager.getAssignedProject();
        this.applicationController = applicationController;
        this.officerController = null;
        this.filter = userFilters.computeIfAbsent(manager.getNric(), k -> new Filter());
    }

    public ApplicationManagementCLI(HDBOfficer officer, ApplicationController applicationController, OfficerController officerController) {
        this.officer = officer;
        this.manager = null;
        this.projectName = officer.getAssignedProject();
        this.applicationController = applicationController;
        this.officerController = officerController;
        this.filter = null;
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

            switch (choice) {
                case 1 -> flatSelectionWorkflow();
                case 2 -> generateReceipt();
                case 3 -> System.out.println("Exiting Application Management.");
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 3);
    }

    private void officerMenu() {
        System.out.println("\n======Application Management======");
        System.out.println("1. Flat Selection");
        System.out.println("2. Generate Booking Receipt");
        System.out.println("3. Back");
        System.out.print("Choice: ");
    }

    private void flatSelectionWorkflow() {
        List<Application> pending = applicationController.getPendingApplicationsByProject(projectName);

        if (pending.isEmpty()) {
            System.out.println("No successful applications pending for flat booking.");
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
        
        System.out.print("Enter Applicant NRIC: ");
        String applicantNric = scanner.nextLine().trim().toUpperCase();

        officerController.assignFlatToApplicant(officer, applicantNric);
    }

    private void generateReceipt() {
        List<Application> successful = applicationController.getFlatBookedByProject(projectName);

        if (successful.isEmpty()) {
            System.out.println("No successful applications found for flat booking.");
            return;
        }      

        System.out.print("Enter Applicant NRIC to generate receipt: (or exit)");
        String applicantNric = scanner.nextLine().trim().toUpperCase();

        if (!applicantNric.equalsIgnoreCase("exit")) {
            officerController.generateReceipt(officer, applicantNric);
        }

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
        collectApplicationFilterInput(); 
        List<Application> result = applicationController.getFilteredApplications(filter);

        if (result.isEmpty()) {
            System.out.println("No matching applications found.");
            return;
        }

        printReport(result);
    }

    private void collectApplicationFilterInput() {
        System.out.println("\n=== Filter Aplications by: ===");

        System.out.print("Filter by Marital Status (e.g., Married, Single leave blank to skip): ");
        String maritalStatus = scanner.nextLine().trim();
        filter.setMaritalStatus(maritalStatus.isEmpty() ? null : maritalStatus);

        System.out.print("Filter by Flat Type (e.g., 2-Room, 3-Room leave blank to skip): ");
        String flatType = scanner.nextLine().trim();
        filter.setFlatType(flatType.isEmpty() ? null : flatType);

        System.out.print("Filter by Project Name (leave blank to skip): ");
        String projectName = scanner.nextLine().trim();
        filter.setProjectName(projectName.isEmpty() ? null : projectName);

        System.out.print("Filter by Minimum Age (leave blank to skip): ");
        String minAgeStr = scanner.nextLine().trim();
        if (!minAgeStr.isEmpty()) {
            try {
                filter.setMinAge(Integer.parseInt(minAgeStr));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for minimum age. Skipping.");
            }
        }

        System.out.print("Filter by Maximum Age (leave blank to skip): ");
        String maxAgeStr = scanner.nextLine().trim();
        if (!maxAgeStr.isEmpty()) {
            try {
                filter.setMaxAge(Integer.parseInt(maxAgeStr));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for maximum age. Skipping.");
            }
        }

        System.out.println("Statuses: PENDING, SUCCESSFUL, BOOKED, UNSUCCESSFUL, WITHDRAWN");
        System.out.print("Filter by Application Status (leave blank to skip): ");
        String statusInput = scanner.nextLine().trim().toUpperCase();
        try {
            filter.setStatus(statusInput.isEmpty() ? null : Application.Status.valueOf(statusInput));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status entered. Skipping.");
            filter.setStatus(null);
        }

        System.out.println("Application filters updated.");
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
