package main;

import controller.*;

import java.time.LocalDate;
import java.util.Scanner;
import model.*;
import util.ExcelReader;
import view.*;

public class MainApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        // ===== Initialize Controllers =====
        AuthController authController = new AuthController();
        ApplicationController applicationController = new ApplicationController();
        EnquiryController enquiryController = new EnquiryController();
        OfficerController officerController = new OfficerController(applicationController);
        ManagerController managerController = new ManagerController(authController);
        
        LoginCLI loginCLI = new LoginCLI(authController, sc);
        ApplicationCLI applicationCLI = new ApplicationCLI(applicationController);

        // ===== Load All Data =====
        if (!loadData(authController, "src/data/CombinedExcel.xlsx")) {
            sc.close();
            return;
        }

        // ===== Login Loop =====
        while (true) {
            loginCLI.welcomeScreen();
            User user = loginCLI.promptLogin();
            if (user == null) break;

            // ===== Route to CLI Based on Role =====
            switch (user.getRole()) {
                case "Applicant" -> {
                    ApplicantCLI cli = new ApplicantCLI((Applicant) user, enquiryController, authController, applicationCLI);
                    cli.start();
                }
                case "HDBOfficer" -> {
                    OfficerCLI cli = new OfficerCLI((HDBOfficer) user, officerController, authController, enquiryController, applicationCLI, applicationController);
                    cli.start();
                }
                case "HDBManager" -> {
                    ManagerCLI cli = new ManagerCLI((HDBManager) user, managerController, authController, enquiryController, applicationController);
                    cli.start();
                }
                default -> System.out.println("Unknown role.");
            }
        }

        System.out.println("Thank you for using the system!");
        sc.close();
    }

    private static boolean loadData(AuthController authController, String path) {
        ExcelReader.ExcelData data = ExcelReader.loadAllData(path, authController);
        if (data == null) {
            System.out.println("Failed to load data from Excel file. Exiting...");
            return false;
        }

        // Load into registries
        ProjectRegistry.loadProjects(data.projects);
        EnquiryRegistry.loadEnquiries(data.enquiries);
        ApplicationRegistry.loadApplications(data.applications);

        // Assign officers to projects
        for (HDBOfficer officer : data.officers) {
            for (Project project : data.projects) {
                if (project.getOfficerList().contains(officer.getName())) {
                    officer.assignToProject(project.getName());
                    officer.setRegistrationStatus(project.getName(), HDBOfficer.RegistrationStatus.APPROVED);
                }
            }
        }

        // Assign managers to projects
        for (HDBManager manager : data.managers) {
            Project selectedProject = null;
            LocalDate today = LocalDate.now();
            LocalDate earliestFutureDate = LocalDate.MAX;

            for (Project project : data.projects) {
                if (!project.getManagerName().equalsIgnoreCase(manager.getName())) continue; // Only consider projects assigned to this manager

                LocalDate openDate = project.getOpenDate();
                LocalDate closeDate = project.getCloseDate();

                // Check if the project is currently open
                if ((openDate.isBefore(today) || openDate.isEqual(today))
                        && (closeDate.isAfter(today) || closeDate.isEqual(today))) {
                    selectedProject = project;
                    break;
                }

                // If no current project, find the earliest future project
                if (openDate.isAfter(today) && openDate.isBefore(earliestFutureDate)) {
                    selectedProject = project;
                    earliestFutureDate = openDate;
                }
            }

            if (selectedProject != null) {
                manager.assignToProject(selectedProject.getName());
            }
        }

        // Register users
        for (Applicant a : data.applicants) authController.addUser(a);
        for (HDBOfficer o : data.officers) authController.addUser(o);
        for (HDBManager m : data.managers) authController.addUser(m);

        return true;
    }
}
