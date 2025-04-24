package model;

import java.util.*;

public class ApplicationRegistry {

    private static final Map<String, List<Application>> applicationMap = new HashMap<>();

    public static void loadApplications(List<Application> applications) {
        applicationMap.clear(); // Start fresh
        for (Application application : applications) {
            applicationMap.computeIfAbsent(application.getApplicant().getNric(), k -> new ArrayList<>()).add(application);
        }
    }

    // Add application
    public static void addApplication(String nric, Application application) {
        applicationMap.computeIfAbsent(nric, k -> new ArrayList<>()).add(application);
    }

    // Get application by NRIC
    public static List<Application> getApplicationByNRIC(String nric) {
        return applicationMap.getOrDefault(nric, new ArrayList<>());
    }

    // Get all applications regardless of visibility and status
    public static Map<String, List<Application>> getAllApplications() {
        return applicationMap;
    }

    public static boolean hasActiveApplication(String nric) {
        List<Application> apps = applicationMap.get(nric);
        if (apps == null) return false;
    
        for (Application app : apps) {
            if (app.getStatus() != Application.Status.WITHDRAWN &&
                app.getStatus() != Application.Status.UNSUCCESSFUL) {
                return true; // found an active one
            }
        }
        return false;
    }

    public static Application getApplicationByNricAndProject(String nric, String projectName) {
        List<Application> apps = applicationMap.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(nric)).map(Map.Entry::getValue).findFirst().orElse(null);
        if (apps == null) return null;
    
        for (Application application : apps) {
            if (application.getProject().getName().equalsIgnoreCase(projectName)) {
                return application;
            }
        }
    
        return null;
    }
    
    public static boolean hasUserAppliedForProject(String nric, String projectName) {
        List<Application> applications = applicationMap.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(nric)).map(Map.Entry::getValue).findFirst().orElse(null);
        if (applications == null) return false;
        return applications.stream().anyMatch(app -> 
            app.getProject().getName().equalsIgnoreCase(projectName));
    }

    public static List<Application> getSuccessfulApplicationsByProject(String projectName) {
        return applicationMap.values().stream()
            .flatMap(List::stream)
            .filter(app -> app.getProject().getName().equalsIgnoreCase(projectName))
            .filter(app -> app.getStatus() == Application.Status.SUCCESSFUL)
            .toList();
    }

    public static List<Application> getFlatBookedByProject(String projectName) {
        return applicationMap.values().stream()
            .flatMap(List::stream)
            .filter(app -> app.getProject().getName().equalsIgnoreCase(projectName))
            .filter(app -> app.getStatus() == Application.Status.BOOKED)
            .toList();
    }

    public static List<Application> getApplicationsByProject(String projectName) {
        return applicationMap.values().stream()
            .flatMap(List::stream)
            .filter(app -> app.getProject().getName().equalsIgnoreCase(projectName))
            .toList();
    }
    
}