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
        OfficerController officerController = new OfficerController();
        ManagerController managerController = new ManagerController(applicationController, authController);
        
        LoginCLI loginCLI = new LoginCLI(authController, sc);
        ApplicationCLI applicationCLI = new ApplicationCLI(applicationController);

        // ===== Load Data =====
        ExcelReader.ExcelData data = ExcelReader.loadAllData("src/data/CombinedExcel.xlsx", authController);
        if (data == null) {
            System.out.println("Failed to load data from Excel file. Exiting...");
            sc.close();
            return;
        }
        
        // ===== Load Data into System State Management =====
        ProjectRegistry.loadProjects(data.projects);
        EnquiryRegistry.loadEnquiries(data.enquiries);
        ApplicationRegistry.loadApplications(data.applications);

        for (HDBOfficer officer : data.officers) {
            for (Project project : data.projects) {
                if (project.getOfficerList().contains(officer.getName())) {
                    officer.assignToProject(project.getName());
                    officer.setRegistrationStatus(project.getName(), HDBOfficer.RegistrationStatus.APPROVED);
                }
            }
        }
        for (HDBManager manager : data.managers) {
            Project selectedProject = null;
            LocalDate today = LocalDate.now();
            LocalDate earliestFutureDate = LocalDate.MAX;

            for (Project project : data.projects) {
                if (!project.getManagerName().equalsIgnoreCase(manager.getName())) {
                    continue; // Not this manager's project
                }

                LocalDate openDate = project.getOpenDate();
                LocalDate closeDate = project.getCloseDate();

                // If project is currently open
                if ((openDate.isBefore(today) || openDate.isEqual(today))
                        && (closeDate.isAfter(today) || closeDate.isEqual(today))) {
                    selectedProject = project;
                    break; // Prefer open project, stop searching
                }

                // If project is upcoming and earlier than current candidate
                if (openDate.isAfter(today) && openDate.isBefore(earliestFutureDate)) {
                    selectedProject = project;
                    earliestFutureDate = openDate;
                }
            }

            if (selectedProject != null) {
                manager.assignToProject(selectedProject.getName());
            }
        }
        for (Applicant a : data.applicants) authController.addUser(a);
        for (HDBOfficer o : data.officers) authController.addUser(o);
        for (HDBManager m : data.managers) authController.addUser(m);

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
                    OfficerCLI cli = new OfficerCLI((HDBOfficer) user, officerController, enquiryController, applicationCLI);
                    cli.start();
                }
                case "HDBManager" -> {
                    ManagerCLI cli = new ManagerCLI((HDBManager) user, managerController, authController, enquiryController);
                    cli.start();
                }
                default -> System.out.println("Unknown role.");
            }
        }

        System.out.println("Thank you for using the system!");
        sc.close();
    }
}
