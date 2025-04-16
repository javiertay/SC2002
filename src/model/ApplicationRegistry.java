package model;

import java.util.*;

public class ApplicationRegistry {

    private static final Map<String, Application> applicationMap = new HashMap<>();

    public static void loadApplications(List<Application> applications) {
        applicationMap.clear(); // Start fresh
        for (Application application : applications) {
            applicationMap.put(application.getApplicant().getNric(), application);
        }
    }

    // Add application
    public static void addApplication(String nric, Application application) {
        applicationMap.put(nric, application);
    }

    // Get application by NRIC
    public static Application getApplicationByNRIC(String nric) {
        return applicationMap.get(nric);
    }

    // Remove application
    public static Application removeApplication(String nric) {
        return applicationMap.remove(nric);
    }

    // Get all applications regardless of visibility and status
    public static Map<String, Application> getAllApplications() {
        return applicationMap;
    }

    // Check if application exists
    public static boolean hasApplication(String nric) {
        return applicationMap.containsKey(nric);
    }

    public static Application getApplicationByNricAndProject(String nric, String projectName) {
        for (Application application : applicationMap.values()) {
            if (application.getApplicant().getNric().equalsIgnoreCase(nric) &&
                application.getProject().getName().equalsIgnoreCase(projectName)) {
                return application;
            }
        }
        return null;
    }
    
    public static boolean hasUserAppliedForProject(String nric, String projectName) {
        Application application = applicationMap.get(nric);
        if (application == null) {
            return false;
        }
    
        Project project = application.getProject();
        if (project == null) {
            return false;
        }
    
        return project.getName().equalsIgnoreCase(projectName);
    }

    public static List<Application> getPendingApplicationsByProject(String projectName) {
        return applicationMap.values().stream()
        .filter(app -> app.getProject().getName().equalsIgnoreCase(projectName))
        .filter(app -> app.getStatus() == Application.Status.PENDING)
        .toList();
    }
    
    public static List<Application> getWithdrawalRequestsByProject(String projectName) {
        return applicationMap.values().stream()
            .filter(app -> app.getProject().getName().equalsIgnoreCase(projectName))
            .filter(Application::isWithdrawalRequested)
            .toList();
    }
}