package controller;

import model.*;

import java.time.LocalDate;
import java.util.*;

public class ManagerController {
    private final ApplicationController applicationController;
    private final AuthController authController;

    public ManagerController(ApplicationController applicationController, AuthController authController) {
        this.authController = authController;
        this.applicationController = applicationController;
    }

    // Create a new project
    public void createProject(Project project, HDBManager manager) {
        if (ProjectRegistry.exists(project.getName().trim())) {
            System.out.println("Project with this name already exists.");
            return;
        }

        String managerProject = manager.getAssignedProject();

        if (managerProject != null) {
            Project existingProject = ProjectRegistry.getProjectByName(managerProject);

            if (existingProject != null) {
                LocalDate existingStart = existingProject.getOpenDate();
                LocalDate existingEnd = existingProject.getCloseDate();
                LocalDate newStart = project.getOpenDate();
                LocalDate newEnd = project.getCloseDate();

                boolean isOverlap = (newStart.isBefore(existingEnd) || newStart.isEqual(existingEnd)) &&
                                    (newEnd.isAfter(existingStart) || newEnd.isEqual(existingStart));

                if (isOverlap) {
                    System.out.println("You are already managing a project ("+existingProject.getName()+") that overlaps with these dates.");
                    return;
                }
            }
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
    public boolean processOfficerApplication(HDBManager manager, String officerNric, HDBOfficer.RegistrationStatus targetStatus) {
        String projectName = manager.getAssignedProject();
        Project project = ProjectRegistry.getProjectByName(projectName);
    
        if (project == null) {
            System.out.println("No active project assigned to manager.");
            return false;
        }
    
        User user = authController.getUserByNRIC(officerNric);
        if (!(user instanceof HDBOfficer officer)) {
            System.out.println("Officer with NRIC " + officerNric + " not found.");
            return false;
        }
    
        HDBOfficer.RegistrationStatus currentStatus = officer.getRegistrationStatus(projectName);
        if (currentStatus != HDBOfficer.RegistrationStatus.PENDING) {
            System.out.println("Officer did not apply or has already been processed.");
            return false;
        }
    
        // If approving, check slots
        if (targetStatus == HDBOfficer.RegistrationStatus.APPROVED) {
            if (!project.hasAvailableOfficerSlot()) {
                System.out.println("No more officer slots available for this project.");
                return false;
            }
            // Assign officer to project
            project.addOfficer(officer.getName());
            officer.assignToProject(projectName);
        }
    
        // Update officer status
        officer.setRegistrationStatus(projectName, targetStatus);
    
        String statusMessage = (targetStatus == HDBOfficer.RegistrationStatus.APPROVED) ? "approved" : "rejected";
        System.out.println("Officer " + officer.getName() + " application has been " + statusMessage + ".");
    
        return true;
    }
    

    /* Officer Managements: View, Approve, Reject */
    public void getAllOfficersByStatus() {
        List<HDBOfficer> allOfficers = new ArrayList<>();

        for (User user : authController.getAllUsers().values()) {
            if (user instanceof HDBOfficer officer) {
                allOfficers.add(officer);
            }
        }

        // === Pending Officers ===
        System.out.println("\n=== Pending Officers ===");
        boolean hasPending = false;

        for (HDBOfficer officer : allOfficers) {
            for (Map.Entry<String, HDBOfficer.RegistrationStatus> entry : officer.getAllRegistrations().entrySet()) {
                if (entry.getValue() == HDBOfficer.RegistrationStatus.PENDING) {
                    System.out.printf("- %s (NRIC: %s) | Project: %s | Status: %s\n",
                        officer.getName(),
                        officer.getNric(),
                        entry.getKey(),
                        entry.getValue());
                    hasPending = true;
                }
            }
        }

        if (!hasPending) {
            System.out.println("No pending officers found.");
        }

        // === Approved Officers ===
        System.out.println("\n=== Approved Officers ===");
        boolean hasApproved = false;

        for (HDBOfficer officer : allOfficers) {
            for (Map.Entry<String, HDBOfficer.RegistrationStatus> entry : officer.getAllRegistrations().entrySet()) {
                if (entry.getValue() == HDBOfficer.RegistrationStatus.APPROVED) {
                    System.out.printf("- %s (NRIC: %s) | Project: %s | Status: %s\n",
                        officer.getName(),
                        officer.getNric(),
                        entry.getKey(),
                        entry.getValue());
                    hasApproved = true;
                }
            }
        }

        if (!hasApproved) {
            System.out.println("No approved officers found.");
        }

        System.out.println();
    }

    public void viewPendingOfficerApplications(HDBManager manager) {
        String projectName = manager.getAssignedProject();
        List<HDBOfficer> pendingOfficers = new ArrayList<>();

        for (User user : authController.getAllUsers().values()) {
            if (user instanceof HDBOfficer officer) {
                HDBOfficer.RegistrationStatus status = officer.getRegistrationStatus(projectName);
                if (status == HDBOfficer.RegistrationStatus.PENDING) {
                    pendingOfficers.add(officer);
                }
            }
        }

        if (pendingOfficers.isEmpty()) {
            System.out.println("No pending officer applications for your project.");
        } else {
            System.out.println("\n=== Pending Officer Applications ===");
            for (HDBOfficer officer : pendingOfficers) {
                System.out.println("- Name: " + officer.getName() + " | NRIC: " + officer.getNric());
            }
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
