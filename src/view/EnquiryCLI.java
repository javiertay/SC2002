package view;

import java.util.List;
import java.util.Scanner;

import controller.EnquiryController;

import model.*;

public class EnquiryCLI {
    private final EnquiryController enquiryController;
    private final User user;
    Scanner scanner = new Scanner(System.in);

    public EnquiryCLI(User user, EnquiryController enquiryController) {
        this.user = user;
        this.enquiryController = enquiryController;
    }

    public void start() {
        if (user instanceof Applicant) {
            showApplicantLoop();
        } else if (user instanceof HDBOfficer || user instanceof HDBManager) {
            staffMenuLoop();
        }
    }

    public void showApplicantLoop() {
        int choice;
        do {
            applicantEnquiryMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> submitEnquiry();
                case 2 -> viewMyEnquiries();
                case 3 -> editEnquiry();
                case 4 -> deleteEnquiry();
                case 5 -> {
                        System.out.println("Exiting Enquiry Management.");
                        System.out.println();
                    }
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 5);
    }

    public void applicantEnquiryMenu() {
        System.out.println("\n======Enquiry Management======");
        System.out.println("1. Submit Enquiry");
        System.out.println("2. View My Enquiries");
        System.out.println("3. Edit Enquiry");
        System.out.println("4. Delete Enquiry");
        System.out.println("5. Back");
        System.out.print("Choice: ");
    }

    public void submitEnquiry() {
        System.out.print("Project Name: ");
        String project = scanner.nextLine();
        System.out.print("Your Message: ");
        String msg = scanner.nextLine();
        enquiryController.submitEnquiry(user.getNric(), project, msg);
    }

    public void viewMyEnquiries() {
        List<Enquiry> enquiries = enquiryController.getEnquiriesByUser(user.getNric());
        if (enquiries.isEmpty()) {
            System.out.println("No enquiries found.");
        } else {
            for (Enquiry enquiry : enquiries) {
                System.out.println("\n------------------");
                System.out.println("Enquiry ID: " + enquiry.getEnquiryId());
                System.out.println("Project Name: " + enquiry.getProjectName());
                System.out.println("Message: " + enquiry.getContent());
                System.out.println("Reply: " + (enquiry.getReply() != null ? enquiry.getReply() : "No reply yet"));
                if (enquiry.getReplyBy() != null) {
                    System.out.println("Replied By: " + enquiry.getReplyBy());
                }
            }
        }
    }

    public void editEnquiry() {
        System.out.print("Enquiry ID to edit: ");
        int id = Integer.parseInt(scanner.nextLine());
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

        boolean updated = enquiryController.updateEnquiry(id, newMsg, user.getNric());
        System.out.println(updated ? "Updated" : "Not found or not your enquiry");
    }

    public void deleteEnquiry() {
        System.out.print("Enquiry ID to delete: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.println("\nYou are about to delete Enquiry ID " + id + ".");
        System.out.print("Are you sure you want to proceed? (Y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = enquiryController.deleteEnquiry(id, user.getNric());
        System.out.println(deleted ? "Deleted" : "Not found or not your enquiry");
    }

    private void staffMenuLoop() {
        int choice;
        do {
            showStaffEnquiryMenu();
            choice = Integer.parseInt(scanner.nextLine());
    
            switch (choice) {
                case 1 -> viewEnquiriesForStaff();
                case 2 -> replyToEnquiry();
                case 3 -> {
                        System.out.println("Exiting Enquiry Management.");
                        System.out.println();
                    }
                default -> System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 3);
    }
    
    public void showStaffEnquiryMenu() {
        System.out.println("\n======"+ (user instanceof HDBManager ? "Manager" : "Officer") +" Enquiry Management======");
        System.out.println("1. View Enquiries");
        System.out.println("2. Reply to Enquiry");
        System.out.println("3. Back");
        System.out.print("Choice: ");
    }
    
    private void viewEnquiriesForStaff() {
        List<Enquiry> enquiries;

        if (user instanceof HDBManager) {
            enquiries = EnquiryRegistry.getAllEnquiries();
        } else if (user instanceof HDBOfficer officer) {
            String projectName = officer.getAssignedProject();
            if (projectName == null || projectName.isEmpty()) {
                System.out.println("You are not assigned to any project.");
                return;
            }
            enquiries = enquiryController.getProjectEnquiries(projectName);
        } else {
            System.out.println("Unauthorized access.");
            return;
        }

        if (enquiries.isEmpty()) {
            System.out.println("No enquiries found.");
        } else {
            enquiries.forEach(System.out::println);
        }
    }

    private void replyToEnquiry() {
        System.out.print("Enter Enquiry ID to reply: ");
        int enquiryId = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter your reply: ");
        String reply = scanner.nextLine();

        boolean success = enquiryController.replyToEnquiry(enquiryId, reply, user);
        System.out.println(success ? "Reply sent!" : "Enquiry not found.");
    }
}
