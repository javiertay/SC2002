package controller;

import model.*;
import util.TableUtil;

import java.time.LocalDate;
import java.util.*;

public class ManagerController {
    private final AuthController authController;

    public ManagerController(AuthController authController) {
        this.authController = authController;
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

        if (manager.getAssignedProject() == null) {
            manager.assignToProject(project.getName());
        }
    }

    public void viewAllProject() {
        Collection<Project> projects = ProjectRegistry.getAllProjects();

        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }

        List<String> headers = List.of("Name", "Neighborhood", "Open Date", "Close Date", "Manager", "Visibility", "Flat Type", "Units Left", "Price", "Officer Slots", "Officers");
        List<List<String>> rows = new ArrayList<>();

        for (Project p : projects) {
            String visibility = p.isVisible() ? "Yes" : "No";
            String officerSlots = p.getCurrentOfficerSlots() + "/" + p.getMaxOfficerSlots();
            String officers = p.getOfficerList().isEmpty() ? "No officer assigned" : String.join(", ", p.getOfficerList());

            List<FlatType> flatTypes = new ArrayList<>(p.getFlatTypes().values());
            for (int i = 0; i < flatTypes.size(); i++) {
                FlatType ft = flatTypes.get(i);

                List<String> row = List.of(
                    i == 0 ? p.getName() : "",
                    i == 0 ? p.getNeighborhood() : "",
                    i == 0 ? p.getOpenDate().toString() : "",
                    i == 0 ? p.getCloseDate().toString() : "",
                    i == 0 ? p.getManagerName() : "",
                    i == 0 ? visibility : "",
                    ft.getType(),
                    String.valueOf(ft.getRemainingUnits()),
                    "$" + ft.getPrice(),
                    i == 0 ? officerSlots : "",
                    i == 0 ? officers : ""
                );

                rows.add(row);
            }
        }

        TableUtil.printTable(headers, rows);
    }

    public List<Project> getProjectsCreatedByManager(HDBManager manager) {
        return ProjectRegistry.getAllProjects().stream()
            .filter(p -> p.getManagerName().equalsIgnoreCase(manager.getName()))
            .toList();
    } 

    // Edit visibility
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

    public boolean updateNeighborhood(HDBManager manager, String newNeighborhood) {
        Project project = getAssignedProject(manager);
        if (project == null) return false;
    
        project.setNeighborhood(newNeighborhood);
        return true;
    }
    
    public boolean updateOpenDate(HDBManager manager, LocalDate openDate) {
        Project project = getAssignedProject(manager);
        if (project == null) return false;
    
        project.setOpenDate(openDate);
        return true;
    }
    
    public boolean updateCloseDate(HDBManager manager, LocalDate closeDate) {
        Project project = getAssignedProject(manager);
        if (project == null) return false;
    
        project.setCloseDate(closeDate);
        return true;
    }
    
    public boolean updateFlatUnits(HDBManager manager, String flatType, int units, int price) {
        Project project = getAssignedProject(manager);
        if (project == null) return false;

        int noOfBookedUnits = project.getFlatTypes().get(flatType).getTotalUnits() - project.getFlatTypes().get(flatType).getRemainingUnits();

        if (!project.getFlatTypes().containsKey(flatType)) return false;
        project.getFlatTypes().get(flatType).setTotalUnits(units);
        project.getFlatTypes().get(flatType).setRemainingUnits(units - noOfBookedUnits);
        project.getFlatTypes().get(flatType).setPrice(price);
        return true;
    }
    
    public boolean updateOfficerSlots(HDBManager manager, int slots) {
        Project project = getAssignedProject(manager);
        if (project == null) return false;
    
        project.setMaxOfficerSlots(slots);
        return true;
    }
        
    // Utility
    private Project getAssignedProject(HDBManager manager) {
        String name = manager.getAssignedProject();
        return (name == null) ? null : ProjectRegistry.getProjectByName(name);
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
    
    public void viewPendingOfficerApplications(HDBManager manager) {
        List<HDBOfficer> pendingOfficers = getPendingOfficerApplications(manager);

        if (pendingOfficers.isEmpty()) {
            System.out.println("No pending officer applications for your project.");
        } 

        List<String> headers = List.of("Name", "NRIC", "Project Name", "Status");

        List<List<String>> rows = pendingOfficers.stream().map(officer -> List.of(
            officer.getName(),
            officer.getNric(),
            officer.getAssignedProject() != null ? manager.getAssignedProject() : "-",
            "Pending"
        )).toList();

        TableUtil.printTable(headers, rows);
    }
}