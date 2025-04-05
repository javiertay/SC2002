package view;

import controller.AuthController;
import controller.EnquiryController;
import controller.ManagerController;
import model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ManagerCLI {
    private final HDBManager manager;
    private final ManagerController managerController;
    private final AuthController authController;
    private final EnquiryController enquiryController;
    private final Scanner scanner;

    public ManagerCLI(HDBManager manager, ManagerController managerController, AuthController authController, EnquiryController enquiryController) {
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
                case 1 -> managerController.viewAllProject();
                case 2 -> createProject();
                case 3 -> deleteProject();
                case 4 -> toggleVisibility();
                case 5 -> approveOfficer();
                case 6 -> approveApplication();
                case 7 -> approveWithdrawal();
                case 8 -> generateReport();
                case 9 -> manageEnquiries();
                case 10 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 10);
    }

    private void showMenu() {
        System.out.println("\n=== HDB Manager Menu ===");
        System.out.println("1. View All Projects");
        System.out.println("2. Create Project");
        System.out.println("3. Delete Project");
        System.out.println("4. Toggle Project Visibility");
        System.out.println("5. Approve Officer Registration");
        System.out.println("6. Approve/Reject BTO Application");
        System.out.println("7. Approve Withdrawal");
        System.out.println("8. Generate Booking Report");
        System.out.println("9. View and Reply to Enquiries");
        System.out.println("10. Logout");
        System.out.print("Select an option: ");
    }

    private void createProject() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate today = LocalDate.now();

        System.out.print("Project Name: ");
        String name = scanner.nextLine();
        System.out.print("Neighborhood: ");
        String neighbourhood = scanner.nextLine();
        System.out.print("2-Room Units: ");
        int twoRoom = Integer.parseInt(scanner.nextLine());
        System.out.print("3-Room Units: ");
        int threeRoom = Integer.parseInt(scanner.nextLine());
        System.out.print("Max Officer Slots: ");
        int maxSlots = Integer.parseInt(scanner.nextLine());

        LocalDate openDate = null;
        while (openDate == null) {
            System.out.print("Enter Project Open Date (dd/MM/yyyy): ");
            String openDateStr = scanner.nextLine();
            try {
                openDate = LocalDate.parse(openDateStr, formatter);
                if (openDate.isBefore(today)) {
                    System.out.println("Open date cannot be in the past. Please try again.");
                    openDate = null;
                }
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }

        LocalDate closeDate = null;
        while (closeDate == null) {
            System.out.print("Enter Project Close Date (dd/MM/yyyy): ");
            String closeDateStr = scanner.nextLine();
            try {
                if (closeDateStr.matches("\\d+")) {
                    int days = Integer.parseInt(closeDateStr);
                    closeDate = openDate.plusDays(days);
                } else {
                    closeDate = LocalDate.parse(closeDateStr, formatter);
                }
                // closeDate = LocalDate.parse(closeDateStr, formatter);
                if (closeDate.isBefore(openDate)) {
                    System.out.println("Close date cannot be before open date. Please try again.");
                    closeDate = null;
                }
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }

        Project p = new Project(name, neighbourhood, openDate, closeDate, true, maxSlots, manager.getName());
        p.addFlatType("2-Room", twoRoom);
        p.addFlatType("3-Room", threeRoom);
        managerController.createProject(p);
    }

    private void deleteProject() {
        System.out.print("Enter Project Name to delete: ");
        String name = scanner.nextLine();

        System.out.print("Are you sure you want to delete this project? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (confirm.equals("Y")) {
            managerController.deleteProject(name);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private void toggleVisibility() {
        System.out.print("Enter Project Name to toggle visibility: ");
        String name = scanner.nextLine();
        managerController.toggleProjectVisibility(name);
    }

    private void approveOfficer() {
        System.out.print("Officer NRIC: ");
        String nric = scanner.nextLine();
        System.out.print("Project Name: ");
        String project = scanner.nextLine();

        User user = authController.getUserByNRIC(nric);
        if (user instanceof HDBOfficer officer) {
            managerController.approveOfficerRegistration(officer, project);
        } else {
            System.out.println("Invalid officer.");
        }
    }

    private void approveApplication() {
        System.out.print("Applicant NRIC: ");
        String nric = scanner.nextLine();
        System.out.print("Approve (Y/N)? ");
        String input = scanner.nextLine().trim().toUpperCase();
        boolean approve = input.equals("Y");
        managerController.approveApplication(nric, approve);
    }

    private void approveWithdrawal() {
        System.out.print("Applicant NRIC to withdraw: ");
        String nric = scanner.nextLine();
        managerController.approveWithdrawal(nric);
    }

    private void manageEnquiries() {
        EnquiryCLI enquiryCLI = new EnquiryCLI(manager, enquiryController);
        enquiryCLI.start();
    }

    private void generateReport() {
        System.out.print("Filter by Marital Status (Married/Single/All): ");
        String filter = scanner.nextLine();
        managerController.generateReport(filter);
    }
}
