package model;

import java.util.*;

/**
* Registry for storing and retrieving BTO projects by name.
* 
* Provides static methods to manage the project collection in memory, including
* loading, filtering, retrieval, and validation utilities.
* 
* This registry is used globally by managers, officers, and applicants to access projects.
* 
* @author Javier
* @version 1.0
*/
public class ProjectRegistry {
    private static final Map<String, Project> projectMap = new HashMap<>();

    /**
    * Loads the given list of projects into the registry.
    * Replaces any previously stored data.
    *
    * @param projects The list of projects to load.
    */
    public static void loadProjects(List<Project> projects) {
        projectMap.clear();
        for (Project p : projects) {
            projectMap.put(p.getName(), p);
        }
    }

    /**
    * Retrieves all projects currently stored in the registry.
    *
    * @return A collection of all projects.
    */
    public static Collection<Project> getAllProjects() {
        return projectMap.values();
    }

    /**
    * Retrieves a project by name (case-insensitive).
    *
    * @param name The name of the project.
    * @return The matching project, or null if not found.
    */
    public static Project getProjectByName(String name) {
        for (Project p : projectMap.values()) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                return p;
            }
        }
        return null;
    }

    /**
    * Returns a list of projects filtered by visibility.
    *
    * @param visibleOnly If true, returns only visible projects; otherwise returns all.
    * @return A list of filtered projects.
    */
    public static List<Project> filterByVisibility(boolean visibleOnly) {
        List<Project> result = new ArrayList<>();
        for (Project project : projectMap.values()) {
            if (!visibleOnly || project.isVisible()) {
                result.add(project);
            }
        }
        return result;
    }

    /**
    * Adds a new project to the registry.
    *
    * @param project The project to add.
    */
    public static void addProject(Project project) {
        projectMap.put(project.getName(), project);
    }

    /**
    * Removes a project from the registry by name.
    *
    * @param projectName The name of the project to remove.
    */
    public static void removeProject(String projectName) {
        projectMap.remove(projectName);
    }

    /**
    * Checks if a project exists in the registry by name.
    *
    * @param projectName The name to check.
    * @return True if the project exists; false otherwise.
    */
    public static boolean exists(String projectName) {
        return projectMap.containsKey(projectName);
    }

    /**
    * Returns the canonical name of a project if it exists, ignoring case.
    *
    * @param inputName The project name entered by the user.
    * @return The normalized project name if found; otherwise returns the input name.
    */
    public static String getNormalizedProjectName(String inputName) {
        for (Project p : projectMap.values()) {
            if (p.getName().equalsIgnoreCase(inputName.trim())) {
                return p.getName(); // returns the correct canonical name (e.g., "Acacia Breeze")
            }
        }
        return inputName; // fallback
    }
}
