package controller;

import model.*;

public class OfficerController {
    
    public boolean reqToHandleProject(HDBOfficer officer, String projectName) {
        // Check if officer already applied for the project
        if (officer.hasActiveRegistration(projectName)) {
            System.out.println("You already have an active registration for this project.");
            return false;
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
    public boolean assignFlat(HDBOfficer officer, String applicantNric, String flatType) {
        // Retrieve applicant's application
        Application application = ApplicationRegistry.getApplicationByNricAndProject(applicantNric, officer.getAssignedProject());
        if (application == null || application.getStatus() != Application.Status.SUCCESSFUL) {
            System.out.println("No successful application found for this applicant in your project.");
            return false;
        }

        // Update application status to booked
        application.setStatus(Application.Status.BOOKED);

        // Update project flat availability
        Project project = ProjectRegistry.getProjectByName(officer.getAssignedProject());
        project.getFlatType(flatType).bookUnit();

        System.out.println("Flat assigned successfully.");
        return true;
    }

    // Generate flat booking receipt
    public void generateReceipt(Application application) {
        Applicant applicant = application.getApplicant();
        Project project = application.getProject();

        System.out.println("\n===== Flat Booking Receipt =====");
        System.out.println("Applicant Name: " + applicant.getName());
        System.out.println("NRIC: " + applicant.getNric());
        System.out.println("Age: " + applicant.getAge());
        System.out.println("Marital Status: " + applicant.getMaritalStatus());
        System.out.println("Flat Type: " + application.getFlatType());
        System.out.println("Project: " + project.getName() + " (" + project.getNeighborhood() + ")");
        System.out.println("================================\n");
    }
}
