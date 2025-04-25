package model;

import java.util.HashMap;
import java.util.Map;

/**
* Represents an HDB Officer in the Build-To-Order system.
* 
* Inherits from {@code Applicant} and includes additional functionality
* to handle project registration and assignment logic specific to officers.
* 
* Officers can register for multiple projects, but may only be actively assigned to one.
* Each project has a registration status tracked using an internal map.
* 
* Status values include:
* <ul>
*   <li>NOT_REGISTERED</li>
*   <li>PENDING</li>
*   <li>APPROVED</li>
*   <li>REJECTED</li>
* </ul>
* 
* @author Javier
* @version 1.0
*/
public class HDBOfficer extends Applicant{
    private String assignedProjectName = null;
    private final Map<String, RegistrationStatus> registrationStatus; // Project name -> Status ("Pending", "Approved", "Rejected")

    /**
    * Represents the registration status of an HDB officer.
    */
    public enum RegistrationStatus {
        /** The officer is not registered. */
        NOT_REGISTERED,

        /** The officer has applied and is awaiting approval. */
        PENDING,

        /** The officer's registration has been approved. */
        APPROVED,

        /** The officer's registration has been rejected. */
        REJECTED
    }

    /**
    * Constructs a new HDBOfficer with the specified user details.
    *
    * @param name The name of the officer.
    * @param nric The NRIC of the officer.
    * @param password The login password.
    * @param age The age of the officer.
    * @param maritalStatus The marital status of the officer.
    */
    public HDBOfficer(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
        this.assignedProjectName = null;
        this.registrationStatus = new HashMap<>();
    }

    /**
    * Checks if the officer has been assigned to any project.
    *
    * @return True if assigned; false otherwise.
    */
    public boolean isAssigned() {
        return assignedProjectName != null;
    }

    /**
    * Assigns the officer to a specific project.
    *
    * @param projectName The project to assign.
    */
    public void assignToProject(String projectName) {
        this.assignedProjectName = projectName;
    }

    /**
    * Returns the project the officer is currently assigned to.
    *
    * @return The name of the assigned project, or null if unassigned.
    */
    public String getAssignedProject() {
        return assignedProjectName;
    }

    /**
    * Returns the user role.
    *
    * @return "HDBOfficer"
    */
    @Override
    public String getRole() {
        return "HDBOfficer";
    }

    /**
    * Retrieves the registration status for a specific project.
    *
    * @param projectName The project name.
    * @return The current registration status.
    */
    public RegistrationStatus getRegistrationStatus(String projectName) {
        return registrationStatus.getOrDefault(projectName, RegistrationStatus.NOT_REGISTERED);
    }

    /**
    * Sets the registration status for a specific project.
    *
    * @param projectName The project name.
    * @param status The status to set (PENDING, APPROVED, etc.).
    */
    public void setRegistrationStatus(String projectName, RegistrationStatus status) {
        registrationStatus.put(projectName, status);
    }

    /**
    * Checks if the officer is currently registered (pending or approved) for a project.
    *
    * @param projectName The name of the project.
    * @return True if the officer has an active registration.
    */
    public boolean hasActiveRegistration(String projectName) {
        RegistrationStatus status = registrationStatus.get(projectName);
        return status == RegistrationStatus.PENDING || status == RegistrationStatus.APPROVED;
    }

    /**
    * Checks if the officer is approved for any project.
    *
    * @return True if there is at least one approved registration.
    */
    public boolean isRegisteredToAnotherProject() {
        return registrationStatus.containsValue(RegistrationStatus.APPROVED);
    }

    /**
    * Returns a copy of all project registration statuses.
    *
    * @return A map of project names to their registration status.
    */
    public Map<String, RegistrationStatus> getAllRegistrations() {
        return new HashMap<>(registrationStatus);
    }
}
