package controller;

import model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import util.*;

public class ApplicationController {

    // View all available projects for the applicant based on age and marital status
    public List<Project> getAllAvailableProjects(Applicant applicant) {
        List<Project> projects = ProjectRegistry.filterByVisibility(true).stream()
            .filter(project -> !LocalDate.now().isAfter(project.getCloseDate()))
            .toList();

        if (applicant.getMaritalStatus().equalsIgnoreCase("single")) {
            if (applicant.getAge() < 35) {
                // Below 35 and single — no eligible projects
                return Collections.emptyList();
            }
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

        if (ApplicationRegistry.hasActiveApplication(applicant.getNric())) {
            System.out.println("You already have an active application. Withdraw or wait for rejection to reapply.");
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
        List<Application> applications = ApplicationRegistry.getApplicationByNRIC(applicant.getNric());
        if (applications == null || applications.isEmpty()) {
            System.out.println("You have not applied for any project.");
        } else {
            List<String> headers = List.of("Name", "Location", "Open Date", "Close Date","Flat Type", "Project Status", "Booking Status");

            List<List<String>> rows = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (Application application : applications) {
                Project project = application.getProject();

                String status = (today.isAfter(project.getCloseDate()) || !project.isVisible()) 
                                ? "Closed" 
                                : "Open";

                rows.add(List.of(
                    project.getName(),
                    project.getNeighborhood(),
                    project.getOpenDate().format(DateTimeFormatter.ofPattern("d/M/yyyy")),
                    project.getCloseDate().format(DateTimeFormatter.ofPattern("d/M/yyyy")),
                    application.getFlatType(),
                    status,
                    application.getStatus().toString()
                ));
            }
            rows.sort(Comparator.comparing(row -> row.get(0)));
            TableUtil.printTable(headers, rows);
        }
    }

    // Withdraw application
    public boolean reqToWithdrawApp(Applicant applicant) {
        List<Application> applications = ApplicationRegistry.getApplicationByNRIC(applicant.getNric());
    
        if (applications == null || applications.isEmpty()) {
            System.out.println("No application found to withdraw.");
            return false;
        }
    
        // Find the latest non-withdrawn application
        Application application = null;
        for (int i = applications.size() - 1; i >= 0; i--) {
            Application app = applications.get(i);
            if (app.getStatus() != Application.Status.WITHDRAWN) {
                application = app;
                break;
            }
        }
    
        if (application == null) {
            System.out.println("You have no active application to withdraw.");
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
    public List<Application> getApplicationByNRIC(String nric) {
        return ApplicationRegistry.getApplicationByNRIC(nric);
    }

    public Map<String, List<Application>> getAllApplications() {
        return ApplicationRegistry.getAllApplications();
    }


    // ====== HDB Manager Functions ======
    public void approveRejectApplication(String nric, String projectName, HDBManager manager, Application.Status status) {
        boolean authorized = manager.getManagedProjects().stream().anyMatch(p -> p.equalsIgnoreCase(projectName));
        if (!authorized) {
            System.out.println("You are not the manager for this project. You can only process applications for projects you manage!");
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
                application.setStatus(Application.Status.UNSUCCESSFUL); // auto set to reject if no more units left
                return;
            }
        }

        application.setStatus(status);
        System.out.println("Application for NRIC: " + nric + " in project: " + projectName + " has been " + status);
    } 

    public boolean approveWithdrawal(HDBManager manager, String nric) {    
        List<Application> apps = ApplicationRegistry.getApplicationByNRIC(nric);
        if (apps == null || apps.isEmpty()) {
            System.out.println("No applications found for this applicant.");
            return false;
        }
    
        // Find the relevant withdrawal request for manager's assigned project
        Application app = apps.stream()
            .filter(a -> manager.getManagedProjects().stream()
                        .anyMatch(p -> p.equalsIgnoreCase(a.getProject().getName())))
            .filter(Application::isWithdrawalRequested)
            .findFirst()
            .orElse(null);
    
        if (app == null) {
            System.out.println("No withdrawal request found for this applicant under your project.");
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
    

    public boolean rejectWithdrawal(HDBManager manager, String nric) {    
        List<Application> apps = ApplicationRegistry.getApplicationByNRIC(nric);
        if (apps == null || apps.isEmpty()) {
            System.out.println("No applications found for this applicant.");
            return false;
        }
    
        // Look for a withdrawal request for the manager's assigned project
        Application app = apps.stream()
            .filter(a -> manager.getManagedProjects().stream()
                        .anyMatch(p -> p.equalsIgnoreCase(a.getProject().getName())))
            .filter(Application::isWithdrawalRequested)
            .findFirst()
            .orElse(null);
    
        if (app == null) {
            System.out.println("No withdrawal request found for this applicant under your project.");
            return false;
        }
    
        app.setWithdrawalRequested(false); // clear the request
        System.out.println("Withdrawal request rejected.");
        return true;
    }

    public List<Application> getFilteredApplications(Filter filter) {
        List<Application> allApps = ApplicationRegistry.getAllApplications()
        .values().stream()
        .flatMap(List::stream)
        .toList();

        return FilterUtil.applyFilter(allApps, filter);
    }

    public List<Application> getApplicationsByManager(HDBManager manager) {
        return manager.getManagedProjects().stream()
            .map(ProjectRegistry::getProjectByName)
            .filter(Objects::nonNull)
            .flatMap(project -> ApplicationRegistry.getApplicationsByProject(project.getName()).stream())
            .toList();
    }

    public List<Application> getPendingApplicationsByManager(HDBManager manager) {
        return getApplicationsByManager(manager).stream()
            .filter(app -> app.getStatus() == Application.Status.PENDING)
            .toList();
    }

    public List<Application> getWithdrawalRequestsByManager(HDBManager manager) {
        return getApplicationsByManager(manager).stream()
            .filter(app -> app.isWithdrawalRequested())
            .toList();
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