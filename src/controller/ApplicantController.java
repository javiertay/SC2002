package controller;

import model.*;

import java.util.List;

public class ApplicantController {
    private final ApplicationController applicationController;
    private final EnquiryController enquiryController;

    public ApplicantController(ApplicationController ac, EnquiryController ec) {
        this.applicationController = ac;
        this.enquiryController = ec;
    }

    public void getAvailableProjects(Applicant app) {
        List<Project> projects = ProjectRegistry.filterByVisibility(true);
        
        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }
        
        System.out.println("\nAvailable Projects for You:");
        boolean isEligible = false;
        for (Project project : projects) {
            boolean show2Room = false;
            boolean show3Room = false;

            if (app.getMaritalStatus().equalsIgnoreCase("single") && app.getAge() >= 35) {
                show2Room = true;
            } else if (app.getMaritalStatus().equalsIgnoreCase("married") && app.getAge() >= 21) {
                show2Room = true;
                show3Room = true;
            } else {
                continue;
            }

            isEligible = true; // Check if the applicant is eligible for any project

            System.out.println("  Project Name: " + project.getName());
            System.out.println("  Neighborhood: " + project.getNeighborhood());
            System.out.println("  Flat Types:");
            for (FlatType ft : project.getFlatTypes().values()) {
                String type = ft.getType();
                if ((type.equalsIgnoreCase("2-Room") && show2Room) ||
                    (type.equalsIgnoreCase("3-Room") && show3Room)) {
                    System.out.println("    - " + type + ": " + ft.getRemainingUnits() + " units left");
                }
            }
            System.out.println("  Project Open Date: " + project.getOpenDate());
            System.out.println("  Project Close Date: " + project.getCloseDate());

            System.out.println("----------------------------------");
        }

        if (!isEligible) {
            System.out.println("You are not eligible for any projects.");
        }
    }

    public boolean applyForProject(Applicant applicant, String projectName, String flatType) {
        return applicationController.submitApplication(applicant, projectName, flatType);
    }

    public void withdrawApplication(Applicant applicant) {
        applicationController.withdrawApplication(applicant);
    }

    public void viewApplicationStatus(Applicant applicant) {
        applicationController.viewApplicationStatus(applicant);
    }

    public void submitEnquiry(String nric, String project, String message) {
        enquiryController.submitEnquiry(nric, project, message);
    }

    public List<Enquiry> getUserEnquiries(String nric) {
        return enquiryController.getUserEnquiries(nric);
    }

    public boolean deleteEnquiry(int id, String nric) {
        return enquiryController.deleteEnquiry(id, nric);
    }
}

