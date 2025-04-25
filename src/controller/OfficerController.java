package controller;

import java.time.LocalDate;
import java.util.Map;

import model.*;

/**
* Controller for handling actions available to HDB Officers, including project registration,
* registration status checks, project viewing, flat assignment, and receipt generation.
*
* @author Javier 
* @version 1.0
*/
public class OfficerController {
    private final ApplicationController applicationController;

    /**
    * Constructs the OfficerController with access to the application controller.
    *
    * @param applicationController The application controller used to handle assignments and queries.
    */
    public OfficerController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    /**
    * Allows an officer to register to handle a specific project.
    * Officer cannot register if they already have an active registration for an ongoing project,
    * or if they have applied as an applicant for the same project.
    *
    * @param officer The officer requesting to register.
    * @param projectName The name of the project.
    * @return True if registration was successful; false otherwise.
    */
    public boolean reqToHandleProject(HDBOfficer officer, String projectName) {
        projectName = ProjectRegistry.getNormalizedProjectName(projectName);
        // Check if officer has ANY active registration (PENDING or APPROVED)
        for (Map.Entry<String, HDBOfficer.RegistrationStatus> entry : officer.getAllRegistrations().entrySet()) {
            HDBOfficer.RegistrationStatus status = entry.getValue();
            if (status == HDBOfficer.RegistrationStatus.PENDING || status == HDBOfficer.RegistrationStatus.APPROVED) {
                Project registeredProject = ProjectRegistry.getProjectByName(officer.getAssignedProject());
        
                // If the project is still ongoing, block new registration
                if (registeredProject != null && LocalDate.now().isBefore(registeredProject.getCloseDate())) {
                    System.out.println("You have an active officer registration for project: " + officer.getAssignedProject());
                    return false;
                }
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
        if (current == HDBOfficer.RegistrationStatus.PENDING || current == HDBOfficer.RegistrationStatus.APPROVED) {
            System.out.println("You have already applied to this project (status: " + current + ").");
            return false;
        }

        // Set registration status to pending
        officer.setRegistrationStatus(projectName, HDBOfficer.RegistrationStatus.PENDING);
        System.out.println("Registration request submitted. Awaiting Manager approval.");
        return true;
    }

    /**
    * Returns the registration status of the officer for a specific project.
    *
    * @param officer The officer to query.
    * @param projectName The project name to check.
    * @return The registration status for the specified project.
    */
    public HDBOfficer.RegistrationStatus viewRegistrationStatus(HDBOfficer officer, String projectName) {
        return officer.getRegistrationStatus(projectName);
    }

    /**
    * Returns the project object that the officer is currently assigned to.
    *
    * @param officer The officer requesting project details.
    * @return The assigned project, or null if not found.
    */
    public Project viewAssignedProjectDetails(HDBOfficer officer) {
        String projectName = officer.getAssignedProject();
        return ProjectRegistry.getProjectByName(projectName);
    }

    /**
    * Assigns a flat to a successful applicant under the officer's project.
    * Also triggers receipt generation.
    *
    * @param officer The officer performing the assignment.
    * @param applicantNric The NRIC of the applicant.
    */
    public void assignFlatToApplicant(HDBOfficer officer, String applicantNric) {
        if (applicationController.assignFlat(officer, applicantNric)) {
            generateReceipt(officer, applicantNric);
        }
    }

    /**
    * Generates a receipt for a flat booking made by a specified applicant.
    * Confirms the application exists and belongs to the officer’s assigned project.
    *
    * @param officer The officer generating the receipt.
    * @param applicantNric The NRIC of the applicant.
    */
    public void generateReceipt(HDBOfficer officer, String applicantNric) {
        Application application = ApplicationRegistry.getApplicationByNricAndProject(applicantNric, officer.getAssignedProject());
    
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
