package model;

import java.util.*;

/**
* Central registry for storing and retrieving BTO applications by applicants.
* 
* Uses a static map to associate applicant NRICs with their list of applications.
* Provides filtering utilities based on applicant, project, and application status.
* 
* This registry supports multiple applications per applicant.
* 
* @author Javier
* @version 1.0
*/
public class ApplicationRegistry {

    private static final Map<String, List<Application>> applicationMap = new HashMap<>();

    /**
    * Loads a list of applications into the registry.
    * Clears existing data before reloading.
    *
    * @param applications List of applications to be loaded.
    */
    public static void loadApplications(List<Application> applications) {
        applicationMap.clear(); // Start fresh
        for (Application application : applications) {
            applicationMap.computeIfAbsent(application.getApplicant().getNric(), k -> new ArrayList<>()).add(application);
        }
    }

    /**
    * Adds a new application under the specified NRIC.
    *
    * @param nric NRIC of the applicant.
    * @param application The application to add.
    */
    public static void addApplication(String nric, Application application) {
        applicationMap.computeIfAbsent(nric, k -> new ArrayList<>()).add(application);
    }

    /**
    * Retrieves all applications submitted by a given NRIC.
    *
    * @param nric The NRIC of the applicant.
    * @return List of applications or an empty list if none found.
    */
    public static List<Application> getApplicationByNRIC(String nric) {
        return applicationMap.getOrDefault(nric, new ArrayList<>());
    }

    /**
    * Retrieves all applications stored in the registry.
    *
    * @return Map of NRICs to their list of applications.
    */
    public static Map<String, List<Application>> getAllApplications() {
        return applicationMap;
    }

    /**
    * Checks whether an applicant has any active application (not withdrawn or unsuccessful).
    *
    * @param nric NRIC of the applicant.
    * @return True if an active application exists; false otherwise.
    */
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

    /**
    * Retrieves a specific application for a given applicant and project.
    *
    * @param nric The NRIC of the applicant.
    * @param projectName The name of the project.
    * @return The matching application, or null if none found.
    */
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
    
    /**
    * Checks if an applicant has applied for a specific project.
    *
    * @param nric The NRIC of the applicant.
    * @param projectName The name of the project.
    * @return True if a matching application exists; false otherwise.
    */
    public static boolean hasUserAppliedForProject(String nric, String projectName) {
        List<Application> applications = applicationMap.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(nric)).map(Map.Entry::getValue).findFirst().orElse(null);
        if (applications == null) return false;
        return applications.stream().anyMatch(app -> 
            app.getProject().getName().equalsIgnoreCase(projectName));
    }

    /**
    * Retrieves all successful applications for a specific project.
    *
    * @param projectName The name of the project.
    * @return List of successful applications.
    */
    public static List<Application> getSuccessfulApplicationsByProject(String projectName) {
        return applicationMap.values().stream()
            .flatMap(List::stream)
            .filter(app -> app.getProject().getName().equalsIgnoreCase(projectName))
            .filter(app -> app.getStatus() == Application.Status.SUCCESSFUL)
            .toList();
    }

    /**
    * Retrieves all applications with a BOOKED status for a specific project.
    *
    * @param projectName The name of the project.
    * @return List of booked applications.
    */
    public static List<Application> getFlatBookedByProject(String projectName) {
        return applicationMap.values().stream()
            .flatMap(List::stream)
            .filter(app -> app.getProject().getName().equalsIgnoreCase(projectName))
            .filter(app -> app.getStatus() == Application.Status.BOOKED)
            .toList();
    }

    /**
    * Retrieves all applications submitted for a specific project.
    *
    * @param projectName The name of the project.
    * @return List of applications related to the project.
    */
    public static List<Application> getApplicationsByProject(String projectName) {
        return applicationMap.values().stream()
            .flatMap(List::stream)
            .filter(app -> app.getProject().getName().equalsIgnoreCase(projectName))
            .toList();
    }
    
}