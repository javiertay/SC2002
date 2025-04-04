package model;

public class HDBManager extends User {
    private String assignedProjectName = null;

    public HDBManager(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
    }

    @Override
    public String getRole() {
        return "HDBManager";
    }

    public boolean isManagingProject() {
        return assignedProjectName != null;
    }

    public String getAssignedProject() {
        return assignedProjectName;
    }

    public void assignToProject(String projectName) {
        this.assignedProjectName = projectName;
    }

    public void unassignProject() {
        this.assignedProjectName = null;
    }

    // Manager capabilities — logic to be handled in controller/service layer.
}
