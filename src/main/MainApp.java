package main;

import controller.*;
import model.*;
import util.ExcelReader;
import view.*;

import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        String regex_pattern = "^[sStT]\\d{7}[a-zA-Z]$";
        Scanner sc = new Scanner(System.in);
        
        // ===== Initialize Controllers =====
        AuthController authController = new AuthController();
        ApplicationController applicationController = new ApplicationController();
        EnquiryController enquiryController = new EnquiryController();
        OfficerController officerController = new OfficerController();
        ManagerController managerController = new ManagerController(officerController, applicationController);
        ApplicantController applicantController = new ApplicantController(applicationController);
        
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
            System.out.print("Enter NRIC (or 'exit'): ");
            String nric = sc.nextLine().trim();
            if (nric.equalsIgnoreCase("exit")) break;
            if (!nric.matches(regex_pattern)) {
                System.out.println("Invalid NRIC format. Please try again.");
                continue;
            }
            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            User user = authController.login(nric, password);
            if (user == null) continue;

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
