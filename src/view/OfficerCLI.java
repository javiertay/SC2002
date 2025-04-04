package view;

import controller.ApplicationController;
import controller.OfficerController;
import controller.EnquiryController;
import model.*;

import java.util.Scanner;
import java.util.List;;

public class OfficerCLI {

    private final HDBOfficer officer;
    private final OfficerController officerController;
    private final ApplicationController applicationController;
    private final EnquiryController enquiryController;
    private final Scanner scanner;

    public OfficerCLI(HDBOfficer officer, OfficerController officerController, ApplicationController applicationController, EnquiryController enquiryController) {
        this.officer = officer;
        this.officerController = officerController;
        this.applicationController = applicationController;
        this.enquiryController = enquiryController;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice;
        do {
            showMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> viewAssignedProject();
                case 2 -> registerToProject();
                case 3 -> bookFlatForApplicant();
                case 4 -> generateReceipt();
                case 5 -> viewAndReplyEnquiries();
                case 6 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 5);
    }

    private void showMenu() {
        System.out.println("\n=== HDB Officer Menu ===");
        System.out.println("1. View Assigned Project");
        System.out.println("2. Register to Handle Project");
        System.out.println("3. Book Flat for Applicant");
        System.out.println("4. Generate Receipt");
        System.out.println("5. Logout");
        System.out.print("Select an option: ");
    }

    private void viewAssignedProject() {
        if (!officer.isAssigned()) {
            System.out.println("You are not assigned to any project.");
            return;
        }

        String projectName = officer.getAssignedProject();
        Project project = ProjectRegistry.getProjectByName(projectName);

        if (project == null) {
            System.out.println("Project data not found.");
        } else {
            System.out.println("Assigned Project: " + project.getName() +
                    " in " + project.getNeighborhood());
        }
    }

    private void registerToProject() {
        System.out.print("Enter Project Name to register: ");
        String projectName = scanner.nextLine();
        officerController.registerToProject(officer, projectName);
    }

    private void bookFlatForApplicant() {
        System.out.print("Enter Applicant NRIC: ");
        String nric = scanner.nextLine();

        Application app = applicationController.getApplicationByNRIC(nric);
        if (app == null) {
            System.out.println("Application not found.");
            return;
        }

        officerController.bookFlat(officer, app);
    }

    private void generateReceipt() {
        System.out.print("Enter Applicant NRIC: ");
        String nric = scanner.nextLine();

        Application app = applicationController.getApplicationByNRIC(nric);
        if (app == null) {
            System.out.println("Application not found.");
            return;
        }

        String receipt = officerController.generateReceipt(app);
        System.out.println(receipt);
    }

    private void viewAndReplyEnquiries() {
        if (!officer.isAssigned()) return;
        List<Enquiry> list = enquiryController.getProjectEnquiries(officer.getAssignedProject());
        list.forEach(System.out::println);

        System.out.print("Reply to Enquiry ID (0 to skip): ");
        int id = Integer.parseInt(scanner.nextLine());
        if (id != 0) {
            System.out.print("Reply message: ");
            String reply = scanner.nextLine();
            enquiryController.replyToEnquiry(id, reply);
        }
    }
}
