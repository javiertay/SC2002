package view;

import controller.OfficerController;
import controller.EnquiryController;
import controller.ApplicationController;
import controller.AuthController;
import model.*;
import model.HDBOfficer.RegistrationStatus;
import util.Breadcrumb;
import util.InputUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
* CLI interface for HDB Officers in the Build-To-Order Management System.
* 
* Provides functionality for:
* <ul>
*   <li>Viewing and applying to manage BTO projects</li>
*   <li>Managing assigned project details</li>
*   <li>Assigning flats and generating receipts</li>
*   <li>Managing enquiries and changing passwords</li>
* </ul>
* Displays a personalized dashboard upon login showing project and task summary.
* 
* @author Javier
* @version 1.0
*/
public class OfficerCLI {
    private final HDBOfficer officer;
    private final AuthController authController;
    private final OfficerController officerController;
    private final EnquiryController enquiryController;
    private final ApplicationController applicationController;
    private final ApplicationCLI applicationCLI;
    private final Scanner scanner;
    private Breadcrumb breadcrumb;

    /**
    * Constructs the CLI for the logged-in officer and sets up required controllers.
    *
    * @param officer The logged-in HDB officer.
    * @param officerController Controller for officer-specific operations.
    * @param authController Authentication controller for password management.
    * @param enquiryController Controller for managing enquiries.
    * @param applicationCLI CLI for managing applications.
    * @param applicationController Controller for application logic.
    */
    public OfficerCLI(HDBOfficer officer, OfficerController officerController, AuthController authController, EnquiryController enquiryController, ApplicationCLI applicationCLI, ApplicationController applicationController) {
        this.officer = officer;
        this.officerController = officerController;
        this.authController = authController;
        this.enquiryController = enquiryController;
        this.applicationController = applicationController;
        this.applicationCLI = applicationCLI;
        this.scanner = new Scanner(System.in);
        this.breadcrumb = new Breadcrumb();
    }

    /**
    * Starts the CLI menu for the officer.
    * Displays the dashboard and routes to available features based on role and project status.
    */
    public void start() {
        breadcrumb.push("HDB Officer Menu");
        showDashboard();

        int choice;
        do {
            showMenu();
            choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> {
                    breadcrumb.push("BTO Project Hub");
                    applicationCLI.setBreadcrumb(breadcrumb);
                    applicationCLI.start(officer);
                    breadcrumb.pop(); // Return to Applicant Menu after exiting Application Management Hub
                }   
                case 2 -> registerForProject();
                case 3 -> viewRegistrationStatus();
                case 4 -> {
                            if (!isActionAllowed()) break;
                            viewAssignedProjectDetails();
                        }
                case 5 -> {
                            if (!isActionAllowed()) break;
                            breadcrumb.push("Flat Selection Hub");
                            ApplicationManagementCLI applicationManagementCLI = new ApplicationManagementCLI(officer, applicationController, officerController);
                            applicationManagementCLI.setBreadcrumb(breadcrumb);
                            applicationManagementCLI.start();
                            breadcrumb.pop(); // Return to Officer Menu after exiting Application Management Hub
                        }
                case 6 -> manageEnquiries();
                case 7 -> authController.promptPasswordChange(officer, scanner);
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 0);
    }

    /**
    * Displays the main officer menu with options for registration, flat assignment,
    * enquiry handling, password change, and logout.
    */
    private void showMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. Manage HDB Applications");
        System.out.println("2. Register for a Project");
        System.out.println("3. View Registration Status");

        if (officer.isAssigned()) {
            System.out.println("4. View Assigned Project Details");
            System.out.println("5. Flat Selection (Assign Flat)");
        } else {
            System.out.println("4. View Assigned Project Details (Unavailable)");
            System.out.println("5. Flat Selection (Unavailable)");
        }
        
        System.out.println("6. Manage Enquiries");
        System.out.println("7. Change Password");
        System.out.println("0. Logout");
    }

    /**
    * Prompts the officer to register for a specific project by name.
    */
    private void registerForProject() {
        System.out.print("Enter Project Name to register: ");
        String projectName = scanner.nextLine().trim();
        officerController.reqToHandleProject(officer, projectName);
    }

    /**
    * Displays all officer registration statuses for projects they've applied to.
    */
    private void viewRegistrationStatus() {
        Map<String, RegistrationStatus> registrations = officer.getAllRegistrations();
        if (registrations.isEmpty()) {
            System.out.println("No registrations found.");
            return;
        }

        System.out.println("\nYour Registration Status:");
        registrations.forEach((project, status) ->
                System.out.println("- Project: " + project + " | Status: " + status));
    }

    /**
    * Displays details of the project currently assigned to the officer.
    */
    private void viewAssignedProjectDetails() {
        if (!officer.isAssigned()) {
            System.out.println("You are not assigned to any project yet.");
            return;
        }

        Project project = officerController.viewAssignedProjectDetails(officer);
        if (project == null) {
            System.out.println("Assigned project details not found.");
            return;
        }

        System.out.println("\n=== Project Details ===");
        System.out.println("Project Name: " + project.getName());
        System.out.println("Neighborhood: " + project.getNeighborhood());
        System.out.println("Application Period: " + project.getOpenDate() + " to " + project.getCloseDate());
        System.out.println("Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("Flat Types:");
        project.getFlatTypes().forEach((type, flat) ->
                System.out.println("- " + type + " ($"+flat.getPrice()+"): " + flat.getRemainingUnits() + " units available"));
    }

    /**
    * Opens the enquiry management interface for the officer.
    */
    private void manageEnquiries() {
        breadcrumb.push("Officer Enquiry Hub");
        EnquiryCLI enquiryCLI = new EnquiryCLI(officer, enquiryController, breadcrumb);
        enquiryCLI.start();
        breadcrumb.pop(); // Return to Officer Menu after exiting Enquiry Management Hub
    }

    /**
    * Validates if the officer has been assigned to a project before allowing certain actions.
    *
    * @return True if assigned, false otherwise.
    */
    private boolean isActionAllowed() {
        if (!officer.isAssigned()) {
            System.out.println("You are not assigned to any project yet.");
            return false;
        }
        return true;
    }

    /**
    * Displays a personalized dashboard summary for the logged-in officer.
    * <p>
    * Shows:
    * <ul>
    *   <li>Most recent application and its status (if any)</li>
    *   <li>Enquiry reply status (if any)</li>
    *   <li>Number of open and upcoming projects</li>
    *   <li>Assigned project name (if assigned)</li>
    *   <li>Number of applications pending booking</li>
    *   <li>Number of enquiries awaiting reply</li>
    * </ul>
    */
    private void showDashboard() {
        System.out.println("\nWelcome back " + officer.getName() + "!");

        List<Application> app = ApplicationRegistry.getApplicationByNRIC(officer.getNric());
        if (app == null || app.isEmpty()) {
            System.out.println(" - You have not applied for any BTO projects yet.");
        } else {
            Application appToShow = app.size() == 1 ? app.get(0) : app.get(app.size() - 1);
            System.out.println(" - Your application for: " + appToShow.getProject().getName() + " is " + appToShow.getStatus());
        }

        List<Enquiry> enq = EnquiryRegistry.getEnquiriesByUser(officer.getNric());
        if (enq.isEmpty()){
            System.out.println(" - You have not made any enquiries for any BTO projects.");
        } else {
            long withReplies = enq.stream().filter(e -> e.getReply() != null).count();
    
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

        // ====== officer dashboard=====
        System.out.println();    
        String assignedProject = officer.getAssignedProject();
        if (assignedProject == null) {
            System.out.println(" - You are not currently assigned to any project.");
            return;
        }
        
        System.out.println(" - Your Assigned Project: " + assignedProject);
    
        List<Application> applications = ApplicationRegistry.getSuccessfulApplicationsByProject(assignedProject);
        int pendingBookings = applications.size();
    
        System.out.println(" - " + pendingBookings + " Applications pending booking");
    
        List<Enquiry> enquiries = EnquiryRegistry.getEnquiriesByProject(assignedProject);
        long pendingReplies = enquiries.stream()
            .filter(e -> e.getReply() == null)
            .count();
    
        System.out.println(" - " + pendingReplies + " Enquiries awaiting reply");
    }    
}
