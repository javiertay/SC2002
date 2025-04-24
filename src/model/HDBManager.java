package model;

import java.util.ArrayList;
import java.util.List;

public class HDBManager extends User {
    private String assignedProjectName = null;
    private List<String> manageProjectList = new ArrayList<>();

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

    public List<String> getManagedProjects() {
        return new ArrayList<>(manageProjectList);
    }
    
    public void addManagedProject(String projectName) {
        if (!manageProjectList.contains(projectName)) {
            manageProjectList.add(projectName);
        }
    }
}
