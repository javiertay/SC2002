package controller;

import model.*;
import java.util.Map;

public class ApplicationController {
    // Submit application
    public boolean submitApplication(Applicant applicant, String projectName, String flatType) {
        if (ApplicationRegistry.hasApplication(applicant.getNric())) {
            System.out.println("You already have an active application.");
            return false;
        }

        if (applicant.hasApplied()) {
            System.out.println("You have already applied for a project.");
            return false;
        }

        Project project = ProjectRegistry.getProjectByName(projectName);
        if (project == null) {
            System.out.println("Project not found.");
            return false;
        }

        if (!project.isVisible()) {
            System.out.println("This project is no longer visible to applicants.");
            return false;
        }

        if (!applicant.canApply(flatType)) {
            System.out.println("You are not eligible to apply for this flat type.");
            return false;
        }

        FlatType flat = project.getFlatType(flatType);
        if (flat == null) {
            System.out.println("Flat type does not exist in this project.");
            return false;
        }

        if (flat.getRemainingUnits() <= 0) {
            System.out.println("No units available for this flat type.");
            return false;
        }

        // need to update this portion
        Application application = new Application(applicant, project, flatType);
        ApplicationRegistry.addApplication(applicant.getNric(), application);
        applicant.setHasApplied(true);

        System.out.println("Application submitted successfully.");
        return true;
    }

    // View current application
    public void viewApplicationStatus(Applicant applicant) {
        Application application = ApplicationRegistry.getApplicationByNRIC(applicant.getNric());
        if (application == null) {
            System.out.println("ℹYou have not applied for any project.");
        } else {
            System.out.println(application.toString());
        }
    }

    // Withdraw application
    public boolean withdrawApplication(Applicant applicant) {
        Application application = ApplicationRegistry.removeApplication(applicant.getNric());
        if (application == null) {
            System.out.println("No application found to withdraw.");
            return false;
        }

        applicant.setHasApplied(false);
        System.out.println("Application withdrawn successfully.");
        return true;
    }

    // Utility: Get application for Officer to book flat
    public Application getApplicationByNRIC(String nric) {
        return ApplicationRegistry.getApplicationByNRIC(nric);
    }

    public Map<String, Application> getAllApplications() {
        return ApplicationRegistry.getAllApplications();
    }
}
