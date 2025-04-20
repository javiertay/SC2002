package controller;

import model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import util.*;

public class ApplicationController {

    // View all available projects for the applicant based on age and marital status
    public void getAllAvailableProjects(Applicant applicant, Filter filter) {
        List<Project> projects = ProjectRegistry.filterByVisibility(true).stream()
                .filter(project -> !LocalDate.now().isAfter(project.getCloseDate()))
                .toList();
        
        if (applicant.getMaritalStatus().equalsIgnoreCase("single")) {
            projects = projects.stream().filter(project -> {
                FlatType twoRoom = project.getFlatTypes().get("2-Room");
                return twoRoom != null && twoRoom.getRemainingUnits() > 0;
            }).toList();
        }
        
        List<Project> filtered = FilterUtil.applyFilter(projects, filter);

        if (filtered.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }
        
        System.out.println("\nAvailable Projects for You:");
        boolean isEligible = false;
        for (Project project : filtered) {
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

        // List<Project> allProjects = ProjectRegistry.filterByVisibility(true);

        // // Filter the list using shared filtering logic
        // List<Project> filtered = FilterUtil.applyFilter(allProjects, filter);

        // if (filtered.isEmpty()) {
        //     System.out.println("No matching projects found.");
        //     return;
        // }

        // System.out.println("\nAvailable Projects for You:");
        // boolean isEligible = false;

        // for (Project project : filtered) {
        //     boolean show2Room = false;
        //     boolean show3Room = false;

        //     if (applicant.getMaritalStatus().equalsIgnoreCase("single") && applicant.getAge() >= 35) {
        //         show2Room = project.getFlatTypes().containsKey("2-Room") &&
        //                     project.getFlatTypes().get("2-Room").getRemainingUnits() > 0;
        //     } else if (applicant.getMaritalStatus().equalsIgnoreCase("married") && applicant.getAge() >= 21) {
        //         show2Room = project.getFlatTypes().containsKey("2-Room");
        //         show3Room = project.getFlatTypes().containsKey("3-Room");
        //     }

        //     if (!show2Room && !show3Room) continue;
        //     isEligible = true;

        //     System.out.println("  Project Name: " + project.getName());
        //     System.out.println("  Neighborhood: " + project.getNeighborhood());
        //     System.out.println("  Flat Types:");
        //     for (FlatType ft : project.getFlatTypes().values()) {
        //         String type = ft.getType();
        //         if ((type.equalsIgnoreCase("2-Room") && show2Room) ||
        //             (type.equalsIgnoreCase("3-Room") && show3Room)) {
        //             System.out.println("    - " + type + ": " + ft.getRemainingUnits() + " units left");
        //         }
        //     }
        //     System.out.println("  Project Open Date: " + project.getOpenDate());
        //     System.out.println("  Project Close Date: " + project.getCloseDate());
        //     System.out.println("----------------------------------");
        // }

        // if (!isEligible) {
        //     System.out.println("You are not eligible for any projects.");
        // }
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

        // Project project = ProjectRegistry.getProjectByName(projectName);
        // FlatType flat = project.getFlatType(flatType);

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

        // return ApplicationRegistry.getAllApplications().values().stream()
        //     .filter(app -> filter.getProjectName() == null || app.getProject().getName().equalsIgnoreCase(filter.getProjectName()))
        //     .filter(app -> filter.getFlatType() == null || app.getFlatType().equalsIgnoreCase(filter.getFlatType()))
        //     .filter(app -> filter.getMaritalStatus() == null || app.getApplicant().getMaritalStatus().equalsIgnoreCase(filter.getMaritalStatus()))
        //     .filter(app -> filter.getMinAge() == null || app.getApplicant().getAge() >= filter.getMinAge())
        //     .filter(app -> filter.getMaxAge() == null || app.getApplicant().getAge() <= filter.getMaxAge())
        //     .filter(app -> filter.getStatus() == null || app.getStatus() == filter.getStatus())
        //     .toList();

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
