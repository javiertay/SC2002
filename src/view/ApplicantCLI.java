package view;

import controller.AuthController;
import controller.EnquiryController;
import model.Applicant;
import model.Application;
import model.ApplicationRegistry;
import model.Enquiry;
import model.EnquiryRegistry;
import model.ProjectRegistry;
import util.Breadcrumb;
import util.InputUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
* CLI class for Applicants to interact with BTO project application features.
* 
* Handles project applications, enquiry management, password changes, and displays
* a personalized dashboard with application/enquiry summaries.
* 
* @author Javier
* @version 1.0
*/
public class ApplicantCLI {
    private final Applicant applicant;
    private final EnquiryController enquiryController;
    private final AuthController authController;
    private final ApplicationCLI applicationCLI;
    private final Scanner scanner;
    private Breadcrumb breadcrumb;

    /**
    * Constructs the CLI interface for the applicant.
    *
    * @param applicant The logged-in applicant.
    * @param enquiryController Controller handling enquiry-related operations.
    * @param authController Controller for user authentication and password changes.
    * @param applicationCLI CLI for managing BTO applications.
    */
    public ApplicantCLI(Applicant applicant, EnquiryController enquiryController, AuthController authController, ApplicationCLI applicationCLI) {
        this.applicationCLI = applicationCLI;
        this.applicant = applicant;
        this.enquiryController = enquiryController;
        this.authController = authController;
        this.scanner = new Scanner(System.in);
        this.breadcrumb = new Breadcrumb();
    }

    /**
    * Starts the applicant menu loop.
    * Displays dashboard and allows the applicant to navigate available features.
    */
    public void start() {
        breadcrumb.push("Applicant Menu");
        showDashboard();
        
        int choice;
        do {
            showMenu();
            choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> {
                    breadcrumb.push("BTO Project Hub");
                    applicationCLI.setBreadcrumb(breadcrumb);
                    applicationCLI.start(applicant);
                    breadcrumb.pop(); // Return to Applicant Menu after exiting Application Management Hub
                }
                case 2 -> manageEnquiries();
                case 3 -> authController.promptPasswordChange(applicant, scanner);
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 0);
    }

    /**
    * Displays the applicant dashboard including application and enquiry status,
    * and open/upcoming project counts.
    */
    private void showMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. Manage HDB Applications");
        System.out.println("2. Manage Enquiries");
        System.out.println("3. Change Password");
        System.out.println("0. Logout");
    }

    /**
    * Launches the enquiry management submenu.
    * Allows applicants to submit, view, edit, or delete enquiries.
    */
    private void manageEnquiries() {
        breadcrumb.push("Enquiry Management");
        EnquiryCLI enquiryCLI = new EnquiryCLI(applicant, enquiryController, breadcrumb);
        enquiryCLI.start();
        breadcrumb.pop(); // Return to Applicant Menu after exiting Enquiry Management        
    }

    /**
    * Displays a summary dashboard for the applicant.
    * <p>
    * Shows:
    * <ul>
    *     <li>Number of pending applications</li>
    *     <li>Number of replied enquiries</li>
    *     <li>Total number of open and upcoming projects</li>
    * </ul>
    */
    private void showDashboard() {
        System.out.println("\nWelcome back " + applicant.getName() + "!");

        List<Application> applications = ApplicationRegistry.getApplicationByNRIC(applicant.getNric());
        if (applications == null || applications.isEmpty()) {
            System.out.println(" - You have not applied for any BTO projects yet.");
        } else {
            Application appToShow = applications.size() == 1 ? applications.get(0) : applications.get(applications.size() - 1);
            System.out.println(" - Your application for: " + appToShow.getProject().getName() + " is " + appToShow.getStatus());
        }

        List<Enquiry> enquiries = EnquiryRegistry.getEnquiriesByUser(applicant.getNric());
        if (enquiries.isEmpty()){
            System.out.println(" - You have not made any enquiries for any BTO projects.");
        } else {
            long withReplies = enquiries.stream().filter(e -> e.getReply() != null).count();
    
            if (withReplies == 0) {
                System.out.println(" - You have no replies to your enquiries yet.");
            } else {
                System.out.println(" - Your enquiry has been replied to!");
            }
        }

        LocalDate today = LocalDate.now();
        long open = ProjectRegistry.getAllProjects().stream()
            .filter(p -> p.isVisible() &&
                        !p.getOpenDate().isAfter(today) &&
                        !p.getCloseDate().isBefore(today))
            .count();

        long upcoming = ProjectRegistry.getAllProjects().stream()
            .filter(p -> p.isVisible() && p.getOpenDate().isAfter(today))
            .count();

        System.out.println(" - " + open + " Projects currently open. " + upcoming + " more upcoming soon!");
    }
}