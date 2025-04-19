package view;

import controller.AuthController;
import controller.EnquiryController;
import model.Applicant;
import java.util.Scanner;

public class ApplicantCLI {
    private final Applicant applicant;
    private final EnquiryController enquiryController;
    private final AuthController authController;
    private final ApplicationCLI applicationCLI;
    private final Scanner scanner;

    public ApplicantCLI(Applicant applicant, EnquiryController enquiryController, AuthController authController, ApplicationCLI applicationCLI) {
        this.applicationCLI = applicationCLI;
        this.applicant = applicant;
        this.enquiryController = enquiryController;
        this.authController = authController;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice;
        do {
            showMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> applicationCLI.start(applicant);
                case 2 -> manageEnquiries();
                case 3 -> authController.promptPasswordChange(applicant, scanner);
                case 4 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 4);
    }

    private void showMenu() {
        System.out.println("\n=== Applicant Menu ===");
        System.out.println("1. Manage HDB Applications");
        System.out.println("2. Manage Enquiries");
        System.out.println("3. Change Password");
        System.out.println("4. Logout");
        System.out.print("Select an option: ");  
    }

    private void manageEnquiries() {
        EnquiryCLI enquiryCLI = new EnquiryCLI(applicant, enquiryController);
        enquiryCLI.start();
    }
}