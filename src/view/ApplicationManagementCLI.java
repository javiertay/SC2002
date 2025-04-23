package view;

import model.*;
import util.Breadcrumb;
import util.Filter;
import util.InputUtil;
import util.TableUtil;
import controller.ApplicationController;
import controller.OfficerController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationManagementCLI {
    private final Filter filter;
    private final ApplicationController applicationController;
    private final OfficerController officerController;
    private final HDBManager manager;
    private final HDBOfficer officer;
    private final String projectName;
    private final Scanner scanner = new Scanner(System.in);
    private Breadcrumb breadcrumb;

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

    public void setBreadcrumb(Breadcrumb breadcrumb) {
        this.breadcrumb = breadcrumb;
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
            choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> flatSelectionWorkflow();
                case 2 -> generateReceipt();
                case 0 -> System.out.println("Exiting Application Management.");
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 0);
    }

    private void officerMenu() {
        System.out.println("\n======Application Management======");
        System.out.println("1. Flat Selection");
        System.out.println("2. Generate Booking Receipt");
        System.out.println("0. Back to Previous Menu");
    }

    private void flatSelectionWorkflow() {
        List<Application> pending = applicationController.getSuccessfulApplicationsByProject(projectName);

        if (pending.isEmpty()) {
            System.out.println("No successful applications pending for flat booking.");
            return;
        }

        System.out.println("\n=== Pending Applications ===");
        for (Application app : pending) {
            System.out.println("- Name: "+ app.getApplicant().getName() + ", NRIC: " + app.getApplicant().getNric() + ", Flat Type: " + app.getFlatType());
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

        System.out.print("Enter Applicant NRIC to generate receipt (or exit): ");
        String applicantNric = scanner.nextLine().trim().toUpperCase();

        if (!applicantNric.equalsIgnoreCase("exit")) {
            officerController.generateReceipt(officer, applicantNric);
        }

    }

    private void managerManagement() {
        int choice; 
        do {
            printManagerMenu();
            choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> viewAllApplicationsForProject();
                case 2 -> processApplication();
                case 3 -> approveWithdrawal();
                case 4 -> generateReport();
                case 0 -> System.out.println("Exiting Application Management.");
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 0);
    }

    private void printManagerMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. View all Applications for your Project");
        System.out.println("2. Approve/Reject BTO Application");
        System.out.println("3. Withdrawal Approval");
        System.out.println("4. Generate Booking Report");
        System.out.println("0. Back to Previous Menu");
    }

    private void viewAllApplicationsForProject() {
        String projectName = manager.getAssignedProject();
        List<Application> apps = applicationController.getApplicationsByProject(projectName);
    
        if (apps.isEmpty()) {
            System.out.println("No applications found for your project.");
            return;
        }
    
        List<String> headers = List.of("Applicant", "NRIC", "Age", "Project", "Flat Type", "Status");
        List<List<String>> rows = new ArrayList<>(apps.stream().map(app -> {
            Applicant a = app.getApplicant();
            return List.of(
                a.getName(),
                a.getNric(),
                String.valueOf(a.getAge()),
                app.getProject().getName(),
                app.getFlatType(),
                app.getStatus().toString()
            );
        }).toList());
        rows.sort(Comparator.comparing((List<String> row) -> row.get(3)).thenComparing(row -> row.get(0)));
        TableUtil.printTable(headers, rows);
    }    

    private void processApplication() {   
        List<Application> pending = applicationController.getPendingApplicationsByProject(projectName);

        if (pending.isEmpty()) {
            System.out.println("No pending applications for this project.");
            return;
        }

        System.out.println("\n=== Pending Applications ===");
        for (Application app : pending) {
            System.out.println("- Name: "+ app.getApplicant().getName() + ", NRIC: " + app.getApplicant().getNric() + ", Flat Type: " + app.getFlatType());
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
            System.out.println("- Name: "+ app.getApplicant().getName() + ", NRIC: " + app.getApplicant().getNric() + ", Status: " + app.getStatus());
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
            applicationController.approveWithdrawal(manager, nric);
        } else if (decision.equals("R")) {
            applicationController.rejectWithdrawal(manager, nric);
        } else {
            System.out.println("Invalid decision. Returning to menu.");
        }
    }

    private void generateReport() {
        System.out.println("\n-------------------------");
        System.out.println("Current Application Filters:");
        System.out.println(" - Marital Status: " + (filter.getMaritalStatus() != null ? filter.getMaritalStatus() : "-"));
        System.out.println(" - Flat Type: " + (filter.getFlatType() != null ? filter.getFlatType() : "-"));
        System.out.println(" - Project Name: " + (filter.getProjectName() != null ? filter.getProjectName() : "-"));
        System.out.println(" - Age Range: " +
            (filter.getMinAge() != null ? filter.getMinAge() : "-") + " to " +
            (filter.getMaxAge() != null ? filter.getMaxAge() : "-"));
        System.out.println(" - Status: " + (filter.getStatus() != null ? filter.getStatus() : "-"));
        System.out.println();

        if (!filter.isEmpty()) {
            clearFilters();
        } else {
            System.out.println("No filters applied. Would you like to add filters? (Y/N): ");
            String response = scanner.nextLine().trim();

            if (response.equalsIgnoreCase("y")) {
                collectApplicationFilterInput();
            }
        }

        List<Application> result = applicationController.getFilteredApplications(filter);

        if (result.isEmpty()) {
            if (filter.isEmpty()) {
                System.out.println("No applications found.");
                return;
            }

            System.out.print("No matching applications found. Clear filters and try again? (Y/N): ");
            String retry = scanner.nextLine().trim().toLowerCase();
            if (retry.equals("y")) {
                filter.clear();
                result = applicationController.getFilteredApplications(filter);
            } else {
                return;
            }
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

        // System.out.print("Filter by Project Name (leave blank to skip): ");
        // String projectName = scanner.nextLine().trim();
        // filter.setProjectName(projectName.isEmpty() ? null : projectName);
        System.out.print("Filter by Project Name (comma-separated, leave blank to skip): ");
        String projectName = scanner.nextLine().trim();
        if (!projectName.isEmpty()) {
            Set<String> projectNames = Arrays.stream(projectName.split(","))
                .map(String::trim)
                .map(String::toLowerCase) // normalize
                .collect(Collectors.toSet());
            filter.setProjectName(projectNames);
        } else {
            filter.setProjectName(null);
        }

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
        // try {
        //     filter.setStatus(statusInput.isEmpty() ? null : Application.Status.valueOf(statusInput));
        // } catch (IllegalArgumentException e) {
        //     System.out.println("Invalid status entered. Skipping.");
        //     filter.setStatus(null);
        // }
        if (!statusInput.isEmpty()) {
            try {
                Set<Application.Status> statuses = Arrays.stream(statusInput.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(Application.Status::valueOf)
                    .collect(Collectors.toSet());
                filter.setStatus(statuses);
            } catch (IllegalArgumentException e) {
                System.out.println("One or more invalid statuses entered. Skipping status filter.");
                filter.setStatus(null);
            }
        }

        System.out.println("Application filters updated.");
    }

    private void printReport(List<Application> result) {
       System.out.println("\n=== Filtered Application Report ===");

        List<String> headers = List.of("Name", "NRIC", "Age", "Marital Status", "Flat Type", "Project", "Status");
        List<List<String>> rows = new ArrayList<>();

        for (Application app : result) {
            Applicant a = app.getApplicant();
            rows.add(List.of(
                a.getName(),
                a.getNric(),
                String.valueOf(a.getAge()),
                a.getMaritalStatus(),
                app.getFlatType(),
                app.getProject().getName(),
                app.getStatus().toString()
            ));
        }
        rows.sort(Comparator.comparing(row -> row.get(0)));
        TableUtil.printTable(headers, rows);
    }

    private void clearFilters() {
        if (!filter.isEmpty()) {
            System.out.print("Do you want to reset all filters? (Y/N): ");
            String confirmation = scanner.nextLine().trim();
            
            if (confirmation.equalsIgnoreCase("y")) {
                filter.clear();
                System.out.println("Project filter cleared.");
            }
        }
    }
}