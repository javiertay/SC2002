package controller;

import model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ApplicationController {

    // View all available projects for the applicant based on age and marital status
    public void getAllAvailableProjects(Applicant applicant) {
        List<Project> projects = ProjectRegistry.filterByVisibility(true).stream()
                .filter(project -> !LocalDate.now().isAfter(project.getCloseDate()))
                .toList();
        
        if (applicant.getMaritalStatus().equalsIgnoreCase("single")) {
            projects = projects.stream().filter(project -> {
                FlatType twoRoom = project.getFlatTypes().get("2-Room");
                return twoRoom != null && twoRoom.getRemainingUnits() > 0;
            }).toList();
        }        

        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }
        
        System.out.println("\nAvailable Projects for You:");
        boolean isEligible = false;
        for (Project project : projects) {
            boolean show2Room = false;
            boolean show3Room = false;

            if (applicant.getMaritalStatus().equalsIgnoreCase("single") && applicant.getAge() >= 35) {
                show2Room = true;
            } else if (applicant.getMaritalStatus().equalsIgnoreCase("married") && applicant.getAge() >= 21) {
                show2Room = true;
                show3Room = true;
            } else {
                continue;
            }

            isEligible = true; // Check if the applicant is eligible for any project

            System.out.println("  Project Name: " + project.getName());
            System.out.println("  Neighborhood: " + project.getNeighborhood());
            System.out.println("  Flat Types:");
            for (FlatType ft : project.getFlatTypes().values()) {
                String type = ft.getType();
                if ((type.equalsIgnoreCase("2-Room") && show2Room) ||
                    (type.equalsIgnoreCase("3-Room") && show3Room)) {
                    System.out.println("    - " + type + ": " + ft.getRemainingUnits() + " units left");
                }
            }
            System.out.println("  Project Open Date: " + project.getOpenDate());
            System.out.println("  Project Close Date: " + project.getCloseDate());

            System.out.println("----------------------------------");
        }

        if (!isEligible) {
            System.out.println("You are not eligible for any projects.");
        }
    }

    // Submit application
    public boolean submitApplication(Applicant applicant, String projectName, String flatType) {
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
