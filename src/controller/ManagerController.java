package controller;

import model.*;
import util.Filter;
import util.FilterUtil;
import util.TableUtil;

import java.time.LocalDate;
import java.util.*;

/**
* Controller responsible for all project-related operations by HDB managers,
* including project creation, configuration, officer approvals, and more.
*
* @author Javier 
* @version 1.0
*/
public class ManagerController {
    private final AuthController authController;

    /**
    * Constructs a ManagerController with access to the AuthController.
    *
    * @param authController The authentication controller for user access.
    */
    public ManagerController(AuthController authController) {
        this.authController = authController;
    }

    /**
    * Creates a new project and assigns it to the manager if eligible.
    *
    * @param project The new project object to create.
    * @param manager The HDB manager creating the project.
    */
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

        if (manager.getAssignedProject() == null) {
            manager.assignToProject(project.getName());
        }
    }

    /**
    * Displays all projects (filtered) in tabular format.
    *
    * @param filter The filter criteria to apply to projects.
    */
    public void viewAllProject(Filter filter) {
        List<Project> projects = FilterUtil.applyFilter(ProjectRegistry.getAllProjects().stream().sorted(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER)).toList(), filter);

        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }
        TableUtil.printProjectTable(projects, filter);
    }

    /**
    * Toggles a project's visibility status between visible and hidden to applicants.
    *
    * @param projectName The name of the project to toggle.
    */
    public void toggleProjectVisibility(String projectName) {
        if (!ProjectRegistry.exists(projectName)) {
            System.out.println("Project not found.");
            return;
        }
        Project project = ProjectRegistry.getProjectByName(projectName);
        project.setVisibility(!project.isVisible());
        String status = project.isVisible() ? "visible" : "hidden";
        System.out.println("Project " + projectName + " is now " + status + " to applicant.");
    }

    /**
    * Updates the neighborhood name of a manager’s managed project.
    *
    * @param manager The manager performing the update.
    * @param projectName The project to update.
    * @param newNeighborhood The new neighborhood name.
    * @return True if updated successfully; false otherwise.
    */
    public boolean updateNeighborhood(HDBManager manager, String projectName, String newNeighborhood) {
        Project project = getManagedProject(manager, projectName);
        if (project == null) return false;
    
        project.setNeighborhood(newNeighborhood);
        return true;
    }
    
    /**
    * Updates the open date of a project.
    *
    * @param manager The manager performing the update.
    * @param projectName The name of the project.
    * @param openDate The new open date.
    * @return True if successful; false otherwise.
    */
    public boolean updateOpenDate(HDBManager manager, String projectName, LocalDate openDate) {
        Project project = getManagedProject(manager, projectName);
        if (project == null) return false;
    
        project.setOpenDate(openDate);
        return true;
    }
    
    /**
    * Updates the close date of a project.
    *
    * @param manager The manager performing the update.
    * @param projectName The name of the project.
    * @param closeDate The new close date.
    * @return True if successful; false otherwise.
    */
    public boolean updateCloseDate(HDBManager manager, String projectName, LocalDate closeDate) {
        Project project = getManagedProject(manager, projectName);
        if (project == null) return false;
    
        project.setCloseDate(closeDate);
        return true;
    }
    
    /**
    * Updates the number of flat units and price for a specific flat type in a project.
    *
    * @param manager The manager updating the units.
    * @param projectName The name of the project.
    * @param flatType The flat type to update.
    * @param units The new number of total units.
    * @param price The updated price.
    * @return True if updated; false otherwise.
    */
    public boolean updateFlatUnits(HDBManager manager, String projectName, String flatType, int units, int price) {
        Project project = getManagedProject(manager, projectName);
        if (project == null) return false;

        int noOfBookedUnits = project.getFlatTypes().get(flatType).getTotalUnits() - project.getFlatTypes().get(flatType).getRemainingUnits();

        if (!project.getFlatTypes().containsKey(flatType)) return false;
        project.getFlatTypes().get(flatType).setTotalUnits(units);
        project.getFlatTypes().get(flatType).setRemainingUnits(units - noOfBookedUnits);
        project.getFlatTypes().get(flatType).setPrice(price);
        return true;
    }
    
    /**
    * Updates the number of available officer slots in a project.
    *
    * @param manager The manager performing the update.
    * @param projectName The name of the project.
    * @param slots The new number of officer slots (1–10).
    * @return True if updated; false otherwise.
    */
    public boolean updateOfficerSlots(HDBManager manager, String projectName, int slots) {
        if (slots < 1 || slots > 10) {
            System.out.println("Officer slots must be between 1 and 10.");
            return false;
        }
        
        Project project = getManagedProject(manager, projectName);
        if (project == null) return false;
    
        project.setMaxOfficerSlots(slots);
        return true;
    }
        
    /**
    * Returns a specific project if the manager is authorized to manage it.
    * Utility method to prevent code repeation.
    *
    * @param manager The manager making the request.
    * @param projectName The project name to retrieve.
    * @return The project if accessible, or null otherwise.
    */
    private Project getManagedProject(HDBManager manager, String projectName) {
        if (projectName == null || !manager.getManagedProjects().contains(projectName)) {
            System.out.println("You are not authorized to edit this project.");
            return null;
        }
    
        return ProjectRegistry.getProjectByName(projectName);
    }
    
    /**
    * Deletes a project from the system if it is managed by the manager.
    *
    * @param manager The manager requesting deletion.
    * @param projectName The project to delete.
    */
    public void deleteProject(HDBManager manager, String projectName) {
        boolean authorized = manager.getManagedProjects().stream().anyMatch(p -> p.equalsIgnoreCase(projectName));

        if (!authorized) {
            System.out.println("You cannot delete a project that you are not managing.");
            return;
        }

        if (!ProjectRegistry.exists(projectName)) {
            System.out.println("Project not found.");
            return;
        }
        
        ProjectRegistry.removeProject(projectName);
        manager.getManagedProjects().removeIf(p -> p.equalsIgnoreCase(projectName));
        if (manager.getAssignedProject() != null &&
            manager.getAssignedProject().equalsIgnoreCase(projectName)) {
            manager.unassignProject();;
        }
        System.out.println("Project deleted.");
    }

    // ============ OFFICER RELATED METHODS (VIEW AND PROCESS) ============
    /**
    * Processes an officer's application to a project, approving or rejecting it.
    *
    * @param manager The HDB manager approving/rejecting the application.
    * @param officerNric The NRIC of the officer.
    * @param targetStatus The target status to set (APPROVED or REJECTED).
    * @return True if the application was processed successfully; false otherwise.
    */
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

    /**
    * Displays all officers grouped by their registration status (PENDING and APPROVED).
    */
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

    /**
    * Retrieves all officers who have pending applications to the manager’s assigned project.
    *
    * @param manager The manager whose project is used to filter applications.
    * @return A list of pending officer applications.
    */
    public List<HDBOfficer> getPendingOfficerApplications(HDBManager manager) {
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
    
        return pendingOfficers;
    }

    /**
    * Displays a formatted list of pending officer applications for the manager's project.
    *
    * @param manager The HDB manager reviewing pending officer registrations.
    */
    public void viewPendingOfficerApplications(HDBManager manager) {
        List<HDBOfficer> pendingOfficers = getPendingOfficerApplications(manager);

        List<String> headers = List.of("Name", "NRIC", "Project Name", "Status");
    
        List<List<String>> rows = new ArrayList<>(pendingOfficers.stream().map(officer -> List.of(
            officer.getName(),
            officer.getNric(),
            officer.getAssignedProject() == null ? manager.getAssignedProject() : "-",
            "Pending"
        )).toList());
        rows.sort(Comparator.comparing(row -> row.get(0)));
        TableUtil.printTable(headers, rows);
    }
}