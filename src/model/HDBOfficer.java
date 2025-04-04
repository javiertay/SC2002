package model;

public class HDBOfficer extends User{
    private String assignedProjectName = null;

    public HDBOfficer(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
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

    public void unassignProject() {
        this.assignedProjectName = null;
    }

    @Override
    public String getRole() {
        return "HDBOfficer";
    }

    // Officer functionality
    public boolean canBook(Application app) {
        return app != null && app.getStatus() == Application.Status.SUCCESSFUL;
    }

    public void bookFlat(Application app, Project project) {
        if (!canBook(app)) {
            System.out.println("Booking not allowed. Status: " + app.getStatus());
            return;
        }

        FlatType flat = project.getFlatType(app.getFlatType());
        if (flat != null && flat.isAvailable()) {
            flat.bookUnit();
            app.setStatus(Application.Status.BOOKED);
            System.out.println("Flat successfully booked for applicant " + app.getApplicant().getNric());
        } else {
            System.out.println("No units available for the selected flat type.");
        }
    }

    // public String generateReceipt(Application app, Project project) {
    //     return "=== Flat Booking Receipt ===\n" +
    //            "Applicant NRIC: " + app.getApplicant().getNric() + "\n" +
    //            "Age: " + app.getApplicant().getAge() + "\n" +
    //            "Marital Status: " + app.getApplicant().getMaritalStatus() + "\n" +
    //            "Project: " + project.getName() + "\n" +
    //            "Neighborhood: " + project.getNeighborhood() + "\n" +
    //            "Flat Type: " + app.getFlatType() + "\n" +
    //            "Status: " + app.getStatus() + "\n";
    // }
}
