package controller;

import model.*;

import java.util.*;

public class OfficerController {
    private final Set<String> officerProjectRequests = new HashSet<>(); // Tracks registration NRIC+ProjectName
    private final Set<String> approvedOfficers = new HashSet<>(); // Tracks approved officers by NRIC+ProjectName

    // Officer requests to register to handle a project
    public boolean registerToProject(HDBOfficer officer, String projectName) {
        if (officer.isAssigned()) {
            System.out.println("❌ Officer is already assigned to a project.");
            return false;
        }

        Project project = ProjectRegistry.getProjectByName(projectName);
        if (project == null) {
            System.out.println("❌ Project not found.");
            return false;
        }

        String key = officer.getNric() + "@" + projectName;
        if (officerProjectRequests.contains(key)) {
            System.out.println("⚠️ Already registered for this project.");
            return false;
        }

        officerProjectRequests.add(key);
        System.out.println("📨 Officer registration request submitted. Awaiting Manager approval.");
        return true;
    }

    // To be called by ManagerController when approved
    public void approveOfficer(HDBOfficer officer, String projectName) {
        String key = officer.getNric() + "@" + projectName;
        if (officerProjectRequests.contains(key)) {
            officer.assignToProject(projectName);
            approvedOfficers.add(key);
            System.out.println("✅ Officer approved for project: " + projectName);
        }
    }

    // Officer books flat for applicant (if successful)
    public boolean bookFlat(HDBOfficer officer, Application app) {
        if (!officer.isAssigned() || !officer.getAssignedProject().equals(app.getProject().getName())) {
            System.out.println("❌ Officer not assigned to this project.");
            return false;
        }

        if (!app.isBookable()) {
            System.out.println("❌ Applicant is not in 'SUCCESSFUL' state.");
            return false;
        }

        FlatType flat = app.getProject().getFlatType(app.getFlatType());
        if (flat == null || !flat.isAvailable()) {
            System.out.println("❌ Flat type unavailable.");
            return false;
        }

        flat.bookUnit();
        app.setStatus(Application.Status.BOOKED);
        System.out.println("🏠 Flat successfully booked.");
        return true;
    }

    // Generate receipt
    public String generateReceipt(Application app) {
        if (!app.isBooked()) {
            return "❌ No booked flat for applicant " + app.getApplicant().getNric();
        }

        return "\n==== Booking Receipt ====\n" +
                "Applicant: " + app.getApplicant().getNric() +
                "\nAge: " + app.getApplicant().getAge() +
                "\nMarital Status: " + app.getApplicant().getMaritalStatus() +
                "\nProject: " + app.getProject().getName() +
                "\nNeighborhood: " + app.getProject().getNeighborhood() +
                "\nFlat Type: " + app.getFlatType() +
                "\nStatus: " + app.getStatus();
    }

    public boolean isOfficerApproved(String nric, String projectName) {
        return approvedOfficers.contains(nric + "@" + projectName);
    }

    public Set<String> getOfficerRequests() {
        return officerProjectRequests;
    }

    public Set<String> getApprovedOfficers() {
        return approvedOfficers;
    }
}
