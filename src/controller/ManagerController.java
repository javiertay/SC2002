package controller;

import model.*;

import java.util.*;

public class ManagerController {

    private final OfficerController officerController;
    private final ApplicationController applicationController;

    public ManagerController(OfficerController officerController, ApplicationController applicationController) {
        this.officerController = officerController;
        this.applicationController = applicationController;
    }

    // Create a new project
    public void createProject(Project project) {
        if (ProjectRegistry.exists(project.getName())) {
            System.out.println("Project with this name already exists.");
            return;
        }
        ProjectRegistry.addProject(project);
        System.out.println("Project created successfully.");
    }

    public void viewAllProject() {
        Collection<Project> projects = ProjectRegistry.getAllProjects();

        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }

        System.out.println("=== All Projects ===");
        for (Project p : projects) {
            System.out.println("  Project Name: " + p.getName());
            System.out.println("  Neighborhood: " + p.getNeighborhood());
            System.out.println("  Flat Types:");
            for (FlatType ft : p.getFlatTypes().values()) {
                System.out.println("    - " + ft.getType() + ": " + ft.getRemainingUnits() + " units left");
            }
            System.out.println("  Open Date: " + p.getOpenDate());
            System.out.println("  Close Date: " + p.getCloseDate());
            System.out.println("  Visible: " + (p.isVisible() ? "Yes" : "No"));
            System.out.println("  Manager Name: " + p.getManagerName());
            System.out.println("  Officer Slots Used: " + p.getCurrentOfficerSlots() + "/" + p.getMaxOfficerSlots());
            System.out.println("  Officer(s): " + (p.getOfficerList().isEmpty() ? "No officer assigned" : String.join(", ", p.getOfficerList())));

            System.out.println("----------------------------------");
        }
    }

    // Edit visibility
    public void toggleProjectVisibility(String projectName) {
        if (!ProjectRegistry.exists(projectName)) {
            System.out.println("Project not found.");
            return;
        }
        ProjectRegistry.toggleVisibility(projectName);
        System.out.println("Project visibility toggled.");
    }

    // Delete a project
    public void deleteProject(String projectName) {
        if (!ProjectRegistry.exists(projectName)) {
            System.out.println("Project not found.");
            return;
        }
        ProjectRegistry.removeProject(projectName);
        System.out.println("Project deleted.");
    }
 
    // Approve officer registration
    public void approveOfficerRegistration(HDBOfficer officer, String projectName) {
        Project project = ProjectRegistry.getProjectByName(projectName);
        if (project == null) {
            System.out.println("Project not found.");
            return;
        }
    
        if (!project.hasAvailableOfficerSlot()) {
            System.out.println("No available officer slots in this project.");
            return;
        }

        // officerController.approveOfficer(officer, projectName);
        project.incrementOfficerSlot();
    }

    public void processOfficerRegistrationDecision(HDBOfficer officer, String projectName, boolean approved) {
        if (approved) {
            // Update officer profile
            officer.setRegistrationStatus(projectName, HDBOfficer.RegistrationStatus.APPROVED);
            officer.assignToProject(projectName);
            // Update the corresponding project object by adding the officer
            Project project = ProjectRegistry.getProjectByName(projectName);
            if (project != null) {
                project.addOfficer(officer.getName());
                project.incrementOfficerSlot();
                System.out.println("Officer added to project: " + projectName);
            }
            System.out.println("Officer registration approved for project: " + projectName);
        } else {
            officer.setRegistrationStatus(projectName, HDBOfficer.RegistrationStatus.REJECTED);
            System.out.println("Officer registration rejected for project: " + projectName);
        }
    }
    

    // Approve/reject application based on availability
    public void approveApplication(String applicantNRIC, boolean approved) {
        Application app = applicationController.getApplicationByNRIC(applicantNRIC);

        if (app == null) {
            System.out.println("Application not found.");
            return;
        }

        if (approved) {
            FlatType flat = app.getProject().getFlatType(app.getFlatType());
            if (flat == null || flat.getRemainingUnits() <= 0) {
                System.out.println("Not enough units to approve application.");
                return;
            }
            app.setStatus(Application.Status.SUCCESSFUL);
            flat.bookUnit();
            System.out.println("Application approved.");
        } else {
            app.setStatus(Application.Status.UNSUCCESSFUL);
            System.out.println("Application rejected.");
        }
    }

    // Approve withdrawal
    public void approveWithdrawal(String applicantNRIC) {
        boolean success = applicationController.withdrawApplication(new Applicant("", applicantNRIC, "", 0, ""));
        if (success) {
            System.out.println("Withdrawal approved and processed.");
        }
    }

    // Generate a simple report (you can expand filters later)
    public void generateReport(String maritalFilter) {
        Map<String, Application> apps = applicationController.getAllApplications();
        System.out.println("==== BTO Report: Filter = " + maritalFilter + " ====");

        for (Application app : apps.values()) {
            if (app.getStatus() == Application.Status.BOOKED &&
                (maritalFilter.equalsIgnoreCase("all") ||
                 app.getApplicant().getMaritalStatus().equalsIgnoreCase(maritalFilter))) {

                System.out.println("- " + app.getApplicant().getNric()
                        + " | Age: " + app.getApplicant().getAge()
                        + " | Status: " + app.getApplicant().getMaritalStatus()
                        + " | Project: " + app.getProject().getName()
                        + " | Flat: " + app.getFlatType());
            }
        }
    }
}
