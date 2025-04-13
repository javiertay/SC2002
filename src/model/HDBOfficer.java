package model;

import java.util.HashMap;
import java.util.Map;

public class HDBOfficer extends Applicant{
    private String assignedProjectName = null;
    private final Map<String, RegistrationStatus> registrationStatus; // Project name -> Status ("Pending", "Approved", "Rejected")

    public enum RegistrationStatus {
        NOT_REGISTERED,
        PENDING,
        APPROVED,
        REJECTED
    }

    public HDBOfficer(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
        this.assignedProjectName = null;
        this.registrationStatus = new HashMap<>();
    }

    public boolean isAssigned() {
        return assignedProjectName != null;
    }

    public void assignToProject(String projectName) {
        this.assignedProjectName = projectName;
    }

    public String getAssignedProject() {
        return assignedProjectName;
    }

    @Override
    public String getRole() {
        return "HDBOfficer";
    }

    public RegistrationStatus getRegistrationStatus(String projectName) {
        return registrationStatus.getOrDefault(projectName, RegistrationStatus.NOT_REGISTERED);
    }

    public void setRegistrationStatus(String projectName, RegistrationStatus status) {
        registrationStatus.put(projectName, status);
    }

    public boolean hasActiveRegistration(String projectName) {
        RegistrationStatus status = registrationStatus.get(projectName);
        return status == RegistrationStatus.PENDING || status == RegistrationStatus.APPROVED;
    }

    public boolean isRegisteredToAnotherProject() {
        return registrationStatus.containsValue(RegistrationStatus.APPROVED);
    }

    public Map<String, RegistrationStatus> getAllRegistrations() {
        return new HashMap<>(registrationStatus);
    }
}
