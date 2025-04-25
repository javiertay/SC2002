package main;

import controller.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import model.*;
import util.*;
import view.*;

/**
* Entry point of the BTO application system.
* <p>
* This class is responsible for:
* <ul>
*   <li>Loading data from Excel into in-memory registries</li>
*   <li>Instantiating controllers and launching role-specific CLI interfaces</li>
*   <li>Persisting data on shutdown using ExcelWriter</li>
* </ul>
* 
* Roles supported:
* <ul>
*   <li>Applicant</li>
*   <li>HDB Officer</li>
*   <li>HDB Manager</li>
* </ul>
* 
* @author Javier 
* @version 1.0
*/
public class MainApp {
    /**
    * Initializes the system, loads data, and routes users to their respective role-based CLIs.
    *
    * @param args Command-line arguments (not used).
    */ 
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ExcelWriter.saveData();
        }));

        Map<String, Filter> userfilters = new HashMap<>();
        Scanner sc = new Scanner(System.in);
        
        // ===== Initialize Controllers =====
        AuthController authController = new AuthController();
        ApplicationController applicationController = new ApplicationController();
        EnquiryController enquiryController = new EnquiryController();
        OfficerController officerController = new OfficerController(applicationController);
        ManagerController managerController = new ManagerController(authController);
        
        LoginCLI loginCLI = new LoginCLI(authController, sc);

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
            String nric = user.getNric();

            // ===== Route to CLI Based on Role =====
            switch (user.getRole()) {
                case "Applicant" -> {
                    ApplicationCLI applicationCLI = new ApplicationCLI(applicationController, nric, userfilters);
                    ApplicantCLI cli = new ApplicantCLI((Applicant) user, enquiryController, authController, applicationCLI);
                    cli.start();
                }
                case "HDBOfficer" -> {
                    ApplicationCLI applicationCLI = new ApplicationCLI(applicationController, nric, userfilters);
                    OfficerCLI cli = new OfficerCLI((HDBOfficer) user, officerController, authController, enquiryController, applicationCLI, applicationController);
                    cli.start();
                }
                case "HDBManager" -> {
                    ApplicationManagementCLI applicationManagementCLI = new ApplicationManagementCLI((HDBManager) user, applicationController, userfilters);
                    ManagerCLI cli = new ManagerCLI((HDBManager) user, managerController, authController, enquiryController, applicationManagementCLI);
                    cli.start();
                }
                default -> System.out.println("Unknown role.");
            }
        }

        System.out.println("Thank you for using the system!");
        sc.close();
    }

    /**
    * Loads users, projects, enquiries, and applications from an Excel file and assigns them
    * to the relevant registries and user roles.
    * <p>
    * Also handles automatic project assignment and visibility updates based on project status.
    *
    * @param authController The authentication controller to register users with.
    * @param path           The path to the Excel file.
    * @return True if data loading was successful; false otherwise.
    */
    private static boolean loadData(AuthController authController, String path) {
        ExcelReader.ExcelData data = ExcelReader.loadAllData(path);
        if (data == null) {
            System.out.println("Failed to load data from Excel file. Exiting...");
            return false;
        }

        // Load into registries
        ProjectRegistry.loadProjects(data.projects);
        EnquiryRegistry.loadEnquiries(data.enquiries);
        ApplicationRegistry.loadApplications(data.applications);

        // Assign managers to projects
        for (HDBManager manager : data.managers) {
            authController.addUser(manager);
            Project selectedProject = null;
            LocalDate today = LocalDate.now();
            LocalDate earliestFutureDate = LocalDate.MAX;

            for (Project project : data.projects) {
                if (!project.getManagerName().equalsIgnoreCase(manager.getName())) continue; // Only consider projects assigned to this manager
                manager.addManagedProject(project.getName());

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

        // auto set project visibility to off if passed closed date
        for (Project project : data.projects) {
            LocalDate today = LocalDate.now();

            if (project.getCloseDate().isBefore(today)) {
                project.setVisibility(false); // Auto hide projects that are closed
            }

            for (HDBOfficer officer : data.officers) {
                authController.addUser(officer);
                if (project.getOfficerList().contains(officer.getName())) {
                    officer.assignToProject(project.getName());
                    officer.setRegistrationStatus(project.getName(), HDBOfficer.RegistrationStatus.APPROVED);
                }
            }
        }

        // Register users
        for (Applicant a : data.applicants) authController.addUser(a);

        return true;
    }
}
