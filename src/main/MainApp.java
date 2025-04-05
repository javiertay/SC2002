package main;

import controller.*;
import model.*;
import util.ExcelReader;
import view.*;

import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        // ===== Initialize Controllers =====
        AuthController authController = new AuthController();
        ApplicationController applicationController = new ApplicationController();
        EnquiryController enquiryController = new EnquiryController();
        OfficerController officerController = new OfficerController();
        ManagerController managerController = new ManagerController(officerController, applicationController);
        ApplicantController applicantController = new ApplicantController(applicationController);
        
        LoginCLI loginCLI = new LoginCLI(authController, sc);

        // ===== Load Data =====
        ExcelReader.ExcelData data = ExcelReader.loadAllData("src/data/CombinedExcel.xlsx", authController);
        if (data == null) {
            System.out.println("Failed to load data from Excel file. Exiting...");
            sc.close();
            return;
        }
        
        // ===== Load Data into System State Management =====
        for (Applicant a : data.applicants) authController.addUser(a);
        for (HDBOfficer o : data.officers) authController.addUser(o);
        for (HDBManager m : data.managers) authController.addUser(m);
        ProjectRegistry.loadProjects(data.projects);
        EnquiryRegistry.loadEnquiries(data.enquiries);
        ApplicationRegistry.loadApplications(data.applications);

        // ===== Login Loop =====
        while (true) {
            System.out.println("\n=== Welcome to the BTO Management System ===");
            User user = loginCLI.promptLogin();
            if (user == null) break;

            // ===== Route to CLI Based on Role =====
            switch (user.getRole()) {
                case "Applicant" -> {
                    ApplicantCLI cli = new ApplicantCLI((Applicant) user, applicantController, applicationController, enquiryController, authController);
                    cli.start();
                }
                case "HDBOfficer" -> {
                    OfficerCLI cli = new OfficerCLI((HDBOfficer) user, officerController, applicationController, enquiryController);
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
