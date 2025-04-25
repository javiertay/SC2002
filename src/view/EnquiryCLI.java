package view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import controller.EnquiryController;

import model.*;
import util.Breadcrumb;
import util.InputUtil;
import util.TableUtil;

/**
 * CLI for managing enquiries submitted by applicants or viewed by HDB staff.
 * 
 * Applicants can submit, view, edit, and delete their enquiries.
 * Officers and managers can view and reply to enquiries for their assigned or handled projects.
 * 
 * @author Javier
 * @version 1.0
 */
public class EnquiryCLI {
    private final EnquiryController enquiryController;
    private final User user;
    Scanner scanner = new Scanner(System.in);
    private Breadcrumb breadcrumb;

    /**
    * Constructs the EnquiryCLI with the current user and required controller.
    *
    * @param user The logged-in user (Applicant, Officer, or Manager).
    * @param enquiryController The controller used to manage enquiry logic.
    * @param breadcrumb Breadcrumb tracker for navigation path display.
    */
    public EnquiryCLI(User user, EnquiryController enquiryController, Breadcrumb breadcrumb) {
        this.breadcrumb = breadcrumb;
        this.user = user;
        this.enquiryController = enquiryController;
    }

    /**
    * Starts the enquiry management interface.
    * Routes based on user role (applicant or staff).
    */
    public void start() {
        if (user instanceof Applicant) {
            showApplicantLoop();
        } else if (user instanceof HDBManager) {
            staffMenuLoop();
        }
    }

    /**
    * Menu loop for applicants to submit or manage their enquiries.
    */
    public void showApplicantLoop() {
        int choice;
        do {
            applicantEnquiryMenu();
            choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> submitEnquiry();
                case 2 -> viewMyEnquiries();
                case 3 -> {
                    if (user instanceof HDBOfficer officer &&
                        officer.getAssignedProject() != null &&
                        !officer.getAssignedProject().isBlank()) {
                        viewEnquiriesForStaff();
                    }
                    else System.out.println("Invalid option. Please try again.");
                }
                case 0 -> {
                        System.out.println("Exiting Enquiry Management.");
                        System.out.println();
                    }
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 0);
    }

    /**
    * Displays the enquiry menu for applicants.
    */
    public void applicantEnquiryMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. Submit Enquiry");
        System.out.println("2. View My Enquiries");
        if (user instanceof HDBOfficer officer &&
            officer.getAssignedProject() != null &&
            !officer.getAssignedProject().isBlank()) {
            System.out.println("3. View Enquiries for " + officer.getAssignedProject());
        }
        System.out.println("0. Back to Previous Menu");
    }

    /**
    * Allows the applicant to submit a new enquiry for an open and visible project.
    */
    public void submitEnquiry() {
        List<Project> visibleProjects = ProjectRegistry.getAllProjects().stream()
                                        .filter(Project::isVisible)
                                        .filter(p -> !p.getCloseDate().isBefore(LocalDate.now()))
                                        .toList();

        if (visibleProjects.isEmpty()) {
            System.out.println("There are currently no open projects available to enquire about.");
            return;
        }

        System.out.println("\nOpen Projects:");
        for (Project p : visibleProjects) {
            System.out.println(" - " + p.getName());
        }

        System.out.print("Project Name (or ENTER to cancel): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return;

        String projectName = ProjectRegistry.getNormalizedProjectName(input);
        if (projectName.isEmpty()) return;

        // Validate against visible projects
        boolean isValid = visibleProjects.stream()
            .anyMatch(p -> p.getName().equalsIgnoreCase(projectName));
        if (!isValid) {
            System.out.println("Invalid project. Please select an open and visible project from the list.");
            return;
        }

        System.out.print("Your Message (or ENTER to cancel): ");
        String msg = scanner.nextLine().trim();
        if (msg.isEmpty()) return;

        enquiryController.submitEnquiry(user.getNric(), projectName, msg);
    }

    /**
    * Displays all enquiries submitted by the applicant and allows edit or delete actions.
    */
    public void viewMyEnquiries() {
        List<Enquiry> enquiries = enquiryController.getEnquiriesByUser(user.getNric());
        TableUtil.printEnquiryTable(enquiries);

        if (!enquiries.isEmpty()) {
            System.out.print("\nDo you want to [E]dit or [D]elete an enquiry? Press Enter to skip: ");
            String action = scanner.nextLine().trim().toLowerCase();
    
            if (action.equalsIgnoreCase("e")) {
                editEnquiry();
            } else if (action.equalsIgnoreCase("d")) {
                deleteEnquiry();
            }
        }
    }

    /**
    * Prompts the applicant to update the content of an existing enquiry.
    */
    public void editEnquiry() {
        System.out.print("Enquiry ID to edit: ");
        String idStr = scanner.nextLine().trim();
        if (idStr.isEmpty()) return;
        int id = Integer.parseInt(idStr);

        System.out.print("New Message: ");
        String newMsg = scanner.nextLine();

        System.out.println("\nYou are about to update Enquiry ID " + id + " with the following message:");
        System.out.println("\"" + newMsg + "\"");
        System.out.print("Are you sure you want to proceed? (Y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (!confirmation.equals("y")) {
            System.out.println("Update cancelled.");
            return;
        }

        enquiryController.updateEnquiry(id, newMsg, user.getNric());;
    }

    /**
    * Prompts the applicant to delete one of their submitted enquiries.
    */
    public void deleteEnquiry() {
        System.out.print("Enquiry ID to delete: ");
        String idStr = scanner.nextLine().trim();
        if (idStr.isEmpty()) return;
        int id = Integer.parseInt(idStr);

        System.out.println("\nYou are about to delete Enquiry ID " + id + ".");
        System.out.print("Are you sure you want to proceed? (Y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equalsIgnoreCase("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = enquiryController.deleteEnquiry(id, user.getNric());
        System.out.println(deleted ? "Deleted" : "Not found or not your enquiry");
    }

    /**
    * Menu loop for managers to view and reply to enquiries.
    */
    private void staffMenuLoop() {
        int choice;
        do {
            managerEnquiryMenu();
            choice = InputUtil.readInt(scanner);
    
            switch (choice) {
                case 1 -> getAllEnquiries();
                case 2 -> viewEnquiriesForStaff();
                case 0 -> {
                        System.out.println("Exiting Enquiry Management.");
                        System.out.println();
                    }
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 0);
    }
    
    /**
    * Displays the manager enquiry menu options.
    */
    public void managerEnquiryMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. View All Enquiries");
        System.out.println("2. View Enquries for My Project");
        System.out.println("0. Back to Previous Menu");
    }

    /**
    * Displays all enquiries in the system (for managers).
    */
    private void getAllEnquiries() {
        List<Enquiry> enquiries = EnquiryRegistry.getAllEnquiries();

        TableUtil.printEnquiryTable(enquiries);
    }
    
    /**
    * Displays enquiries for the officer's assigned project or all projects managed by the manager.
    * Allows reply to individual enquiries.
    */
    private void viewEnquiriesForStaff() {
        if (user instanceof HDBOfficer officer) {
            String assignedProject = officer.getAssignedProject();
            if (assignedProject == null || assignedProject.isBlank()) {
                System.out.println("You are not assigned to any project.");
                return;
            }
    
            List<Enquiry> enquiries = enquiryController.getProjectEnquiries(assignedProject);
            TableUtil.printEnquiryTable(enquiries);
            replyToEnquiry();
            return;
        }

        if (user instanceof HDBManager manager) {
            List<String> managedProjects = manager.getManagedProjects();

            if (managedProjects == null || managedProjects.isEmpty()) {
                System.out.println("You have not managed any projects.");
                return;
            }

            List<Enquiry> allEnquiries = new ArrayList<>();
            for (String project : managedProjects) {
                allEnquiries.addAll(enquiryController.getProjectEnquiries(project));
            }

            TableUtil.printEnquiryTable(allEnquiries);
            replyToEnquiry();
            return;
        }
    }

    /**
    * Prompts the staff user to reply to a selected enquiry.
    */
    private void replyToEnquiry() {
        System.out.print("\nEnter Enquiry ID to reply to (or press Enter to skip): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return; // user chose to skip
        }

        try {
            int enquiryId = Integer.parseInt(input);
            System.out.print("Enter your reply: ");
            String reply = scanner.nextLine().trim();
            if (reply.isEmpty()) return;

            Enquiry enquiry = EnquiryRegistry.getById(enquiryId);

            if (enquiry == null) {
                System.out.println("Enquiry not found.");
                return;
            }

            if (enquiry.getSenderNRIC().equals(user.getNric())) {
                System.out.println("You cannot reply to your own enquiry.");
                return;
            }

            boolean success = enquiryController.replyToEnquiry(enquiryId, reply, user);
            System.out.println(success ? "Reply sent!" : "Enquiry not found or cannot be replied to.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid enquiry ID or press Enter to skip.");
        }
    }
}
