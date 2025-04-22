package controller;

import java.util.Map;

import model.*;

public class OfficerController {
    private final ApplicationController applicationController;

    public OfficerController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    public boolean reqToHandleProject(HDBOfficer officer, String projectName) {
        projectName = ProjectRegistry.getNormalizedProjectName(projectName);
        // Check if officer has ANY active registration (PENDING or APPROVED)
        for (Map.Entry<String, HDBOfficer.RegistrationStatus> entry : officer.getAllRegistrations().entrySet()) {
            HDBOfficer.RegistrationStatus status = entry.getValue();
            if (status == HDBOfficer.RegistrationStatus.PENDING || status == HDBOfficer.RegistrationStatus.APPROVED) {
                System.out.println("You have an active officer registration for project: " + entry.getKey());
                return false;
            }
        }

        // Check if officer applied to the project as an applicant
        if (ApplicationRegistry.hasUserAppliedForProject(officer.getNric(), projectName)) {
            System.out.println("You have already applied for this project as an applicant.");
            return false;
        }

        Project project = ProjectRegistry.getProjectByName(projectName);
        if (project == null) {
            System.out.println("Project not found.");
            return false;
        }

        // Prevent duplicate re-application to same project if not rejected
        HDBOfficer.RegistrationStatus current = officer.getRegistrationStatus(projectName);
        if (current != null && current != HDBOfficer.RegistrationStatus.REJECTED) {
            System.out.println("You have already applied to this project (status: " + current + ").");
            return false;
        }

        // Set registration status to pending
        officer.setRegistrationStatus(projectName, HDBOfficer.RegistrationStatus.PENDING);
        System.out.println("Registration request submitted. Awaiting Manager approval.");
        return true;
    }

    public HDBOfficer.RegistrationStatus viewRegistrationStatus(HDBOfficer officer, String projectName) {
        return officer.getRegistrationStatus(projectName);
    }

    public Project viewAssignedProjectDetails(HDBOfficer officer) {
        String projectName = officer.getAssignedProject();
        return ProjectRegistry.getProjectByName(projectName);
    }

    // Flat selection: update applicant profile and project flat availability
    public void assignFlatToApplicant(HDBOfficer officer, String applicantNric) {
        if (applicationController.assignFlat(officer, applicantNric)) {
            generateReceipt(officer, applicantNric);
        }
    }

    // Generate flat booking receipt
    public void generateReceipt(HDBOfficer officer, String applicantNric) {
        Application application = ApplicationRegistry.getApplicationByNRIC(applicantNric);
    
        if (application == null) {
            System.out.println("No application found for this NRIC.");
            return;
        }

        if (!application.getProject().getName().equalsIgnoreCase(officer.getAssignedProject())) {
            System.out.println("This application does not belong to your assigned project.");
            return;
        }

        Applicant applicant = application.getApplicant();

        System.out.println("\n===== Flat Booking Receipt =====");
        System.out.println("Applicant Name: " + applicant.getName());
        System.out.println("NRIC: " + applicant.getNric());
        System.out.println("Age: " + applicant.getAge());
        System.out.println("Marital Status: " + applicant.getMaritalStatus());
        System.out.println("Flat Type: " + application.getFlatType());
        System.out.println("Project: " + application.getProject().getName() + " (" + application.getProject().getNeighborhood() + ")");
        System.out.println("Flat Type: " + application.getFlatType() + " has been successfully booked.");
        System.out.println("================================\n");
    }
}
