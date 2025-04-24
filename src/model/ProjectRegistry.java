package model;

import java.util.*;

public class ProjectRegistry {
    private static final Map<String, Project> projectMap = new HashMap<>();

    // Load once at startup
    public static void loadProjects(List<Project> projects) {
        projectMap.clear();
        for (Project p : projects) {
            projectMap.put(p.getName(), p);
        }
    }

    public static Collection<Project> getAllProjects() {
        return projectMap.values();
    }

    public static Project getProjectByName(String name) {
        for (Project p : projectMap.values()) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                return p;
            }
        }
        return null;
    }

    public static List<Project> filterByVisibility(boolean visibleOnly) {
        List<Project> result = new ArrayList<>();
        for (Project project : projectMap.values()) {
            if (!visibleOnly || project.isVisible()) {
                result.add(project);
            }
        }
        return result;
    }

    public static void addProject(Project project) {
        projectMap.put(project.getName(), project);
    }

    public static void removeProject(String projectName) {
        projectMap.remove(projectName);
    }

    public static boolean exists(String projectName) {
        return projectMap.containsKey(projectName);
    }

    public static String getNormalizedProjectName(String inputName) {
        for (Project p : projectMap.values()) {
            if (p.getName().equalsIgnoreCase(inputName.trim())) {
                return p.getName(); // returns the correct canonical name (e.g., "Acacia Breeze")
            }
        }
        return inputName; // fallback
    }
}
