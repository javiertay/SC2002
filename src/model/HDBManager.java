package model;

import java.util.ArrayList;
import java.util.List;

/**
* Represents a manager in the Build-To-Order system.
* 
* HDB Managers can create and manage multiple BTO projects,
* but are only actively assigned to one project at a time.
* 
* Inherits common user fields from {@code User}.
* Stores a list of all past or created project names.
* 
* @author Javier
* @version 1.0
*/
public class HDBManager extends User {
    private String assignedProjectName = null;
    private List<String> manageProjectList = new ArrayList<>();

    /**
    * Constructs an HDBManager with the specified user details.
    *
    * @param name The name of the manager.
    * @param nric The NRIC of the manager.
    * @param password The login password.
    * @param age The age of the manager.
    * @param maritalStatus The marital status of the manager.
    */
    public HDBManager(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
    }

    /**
    * Returns the user role.
    *
    * @return "HDBManager"
    */
    @Override
    public String getRole() {
        return "HDBManager";
    }

    /**
    * Checks if the manager is currently assigned to a project.
    *
    * @return True if assigned to a project; false otherwise.
    */
    public boolean isManagingProject() {
        return assignedProjectName != null;
    }

    /**
    * Gets the name of the currently assigned project.
    *
    * @return The assigned project name, or null if not assigned.
    */
    public String getAssignedProject() {
        return assignedProjectName;
    }

    /**
    * Assigns the manager to a new active project.
    *
    * @param projectName The project to assign.
    */
    public void assignToProject(String projectName) {
        this.assignedProjectName = projectName;
    }

    /**
    * Unassigns the manager from their current project.
    */
    public void unassignProject() {
        this.assignedProjectName = null;
    }

    /**
    * Returns a list of all projects managed by this manager.
    *
    * @return A list of managed project names.
    */
    public List<String> getManagedProjects() {
        return new ArrayList<>(manageProjectList);
    }
    
    /**
    * Adds a new project to the manager's managed project list.
    *
    * @param projectName The name of the project to add.
    */
    public void addManagedProject(String projectName) {
        if (!manageProjectList.contains(projectName)) {
            manageProjectList.add(projectName);
        }
    }
}
