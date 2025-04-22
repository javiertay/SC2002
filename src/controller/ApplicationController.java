package controller;

import model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import util.*;

public class ApplicationController {

    // View all available projects for the applicant based on age and marital status
    public List<Project> getAllAvailableProjects(Applicant applicant) {
        List<Project> projects = ProjectRegistry.filterByVisibility(true).stream()
            .filter(project -> !LocalDate.now().isAfter(project.getCloseDate()))
            .toList();

        if (applicant.getMaritalStatus().equalsIgnoreCase("single")) {
            projects = projects.stream().filter(project -> {
                FlatType twoRoom = project.getFlatTypes().get("2-Room");
                return twoRoom != null && twoRoom.getRemainingUnits() > 0;
            }).toList();
        }

        return projects;
    }
    
    // Submit application
    public boolean submitApplication(Applicant applicant, String projectName, String flatType) {
        LocalDate today = LocalDate.now();

        if (ApplicationRegistry.hasApplication(applicant.getNric())) {
            System.out.println("You already have an active application.");
            return false;
        }

        Project project = ProjectRegistry.getProjectByName(projectName);

        if (applicant instanceof HDBOfficer officer) {
            String assignedProjectName = officer.getAssignedProject();
            if (projectName.equalsIgnoreCase(assignedProjectName)) {
                System.out.println("You cannot apply for projects you are handling!");
                return false;
            }
        }

        if (project == null) {
            System.out.println("Project not found.");
            return false;
        }

        if (today.isBefore(project.getOpenDate()) || today.isAfter(project.getCloseDate())) {
            System.out.println("This project is not open for applications.");
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

        return true;
    }

    // View current application
    public void viewApplicationStatus(Applicant applicant) {
        Application application = ApplicationRegistry.getApplicationByNRIC(applicant.getNric());
        if (application == null) {
            System.out.println("You have not applied for any project.");
        } else {
            System.out.println(application.toString());
        }
    }

    // Withdraw application
    public boolean reqToWithdrawApp(Applicant applicant) {
        Application application = ApplicationRegistry.getApplicationByNRIC(applicant.getNric());
        if (application == null) {
            System.out.println("No application found to withdraw.");
            return false;
        }
        
        if (application.getStatus() == Application.Status.WITHDRAWN) {
            System.out.println("Application is already withdrawn.");
            return false;
        }
    
        if (application.isWithdrawalRequested()) {
            System.out.println("You have already requested a withdrawal.");
            return false;
        }
    
        application.setWithdrawalRequested(true);
        System.out.println("Withdrawal request submitted. Waiting for manager approval.");
        return true;
    }

    //
    public Application getApplicationByNRIC(String nric) {
        return ApplicationRegistry.getApplicationByNRIC(nric);
    }

    public Map<String, Application> getAllApplications() {
        return ApplicationRegistry.getAllApplications();
    }


    // ====== HDB Manager Functions ======
    public void approveRejectApplication(String nric, String projectName, HDBManager manager, Application.Status status) {
        if (!manager.getAssignedProject().equalsIgnoreCase(projectName)) {
            System.out.println("You are not assigned to this project.");
            return;
        }

        Application application = ApplicationRegistry.getApplicationByNricAndProject(nric, projectName);
        if (application == null) {
            System.out.println("No application found for this NRIC in the specified project.");
            return;
        }

        if (application.getStatus() != Application.Status.PENDING) {
            System.out.println("Only pending applications can be processed");
            return;
        }

        if (status == Application.Status.SUCCESSFUL) {
            String flatType = application.getFlatType();
            FlatType ft = application.getProject().getFlatType(flatType);
            if (ft == null || ft.getRemainingUnits() <= 0) {
                System.out.println("No units left for this flat type.");
                return;
            }
        }

        application.setStatus(status);
        System.out.println("Application for NRIC: " + nric + " in project: " + projectName + " has been " + status);
    }

    public List<Application> getPendingApplicationsByProject(String projectName) {
        return ApplicationRegistry.getPendingApplicationsByProject(projectName);
    }

    public boolean approveWithdrawal(String nric) {
        Application app = ApplicationRegistry.getApplicationByNRIC(nric);

        if (app == null || !app.isWithdrawalRequested()) {
            System.out.println("No withdrawal request found for this applicant.");
            return false;
        }

        // If BOOKED, return the flat unit
        if (app.getStatus() == Application.Status.BOOKED) {
            String flatType = app.getFlatType();
            Project project = app.getProject();
            FlatType ft = project.getFlatTypes().get(flatType);

            if (ft != null) {
                ft.cancelBooking();
            }
        }

        app.setStatus(Application.Status.WITHDRAWN);
        app.setWithdrawalRequested(false); // clear the request
        System.out.println("Application withdrawn successfully.");
        return true;
    }

    public boolean rejectWithdrawal(String nric) {
        Application app = ApplicationRegistry.getApplicationByNRIC(nric);

        if (app == null || !app.isWithdrawalRequested()) {
            System.out.println("No withdrawal request found for this applicant.");
            return false;
        }

        app.setWithdrawalRequested(false); // clear the request
        System.out.println("Withdrawal request rejected.");
        return true;
    }

    public List<Application> getPendingWithdrawal (String projectName) {
        return ApplicationRegistry.getWithdrawalRequestsByProject(projectName);
    }

    public List<Application> getFilteredApplications(Filter filter) {
        return FilterUtil.applyFilter(ApplicationRegistry.getAllApplications().values(), filter);
    }
    
    // ====== HDB Officer Functions ======
    public boolean assignFlat(HDBOfficer officer, String applicantNRIC) {
        Application application = ApplicationRegistry.getApplicationByNricAndProject(applicantNRIC, officer.getAssignedProject());
        if (application == null || application.getStatus() != Application.Status.SUCCESSFUL) {
            System.out.println("No successful application found for this applicant in your project.");
            return false;
        }

        // Update application status to booked
        application.setStatus(Application.Status.BOOKED);

        // Update project flat availability
        Project project = ProjectRegistry.getProjectByName(officer.getAssignedProject());
        project.getFlatType(application.getFlatType()).bookUnit();

        System.out.println("Flat assigned successfully.");
        return true;
    }

    public List<Application> getSuccessfulApplicationsByProject(String projectName) {
        return ApplicationRegistry.getSuccessfulApplicationsByProject(projectName);
    }

    public List<Application> getFlatBookedByProject(String projectName) {
        return ApplicationRegistry.getFlatBookedByProject(projectName);
    }
}