package model;

import java.util.*;

public class ApplicationRegistry {

    private static final Map<String, Application> applicationMap = new HashMap<>();
    // Key = Applicant NRIC → Application

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

    // Get all applications
    public static Map<String, Application> getAllApplications() {
        return applicationMap;
    }

    // Check if application exists
    public static boolean hasApplication(String nric) {
        return applicationMap.containsKey(nric);
    }
}
