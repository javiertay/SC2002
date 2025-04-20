package util;

import model.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import controller.AuthController;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ExcelReader {

    public static class ExcelData {
        public final List<Applicant> applicants;
        public final List<HDBOfficer> officers;
        public final List<HDBManager> managers;
        public final List<Project> projects;
        public final List<Application> applications;
        public final List<Enquiry> enquiries;


        public ExcelData(List<Applicant> applicants, List<HDBOfficer> officers,
                         List<HDBManager> managers, List<Project> projects,
                         List<Application> applications, List<Enquiry> enquiries) {
            this.applicants = applicants;
            this.officers = officers;
            this.managers = managers;
            this.projects = projects;
            this.applications = applications;
            this.enquiries = enquiries;
        }
    }

    public static ExcelData loadAllData(String filePath, AuthController authController) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet applicantSheet = workbook.getSheet("Applicants");
            Sheet officerSheet = workbook.getSheet("Officers");
            Sheet managerSheet = workbook.getSheet("Managers");
            Sheet projectSheet = workbook.getSheet("ProjectListings");
            Sheet enquries = workbook.getSheet("Enquiries");
            Sheet applications = workbook.getSheet("FlatBookings");

            List<Applicant> applicants = loadApplicants(applicantSheet);
            List<HDBOfficer> officers = loadOfficers(officerSheet);
            List<HDBManager> managers = loadManagers(managerSheet);
            List<Project> projects = loadProjects(projectSheet);
            List<Application> applicationsList = loadApplications(applications, authController);
            List<Enquiry> enquiries = loadEnquiries(enquries);

            return new ExcelData(applicants, officers, managers, projects, applicationsList, enquiries);

        } catch (IOException e) {
            System.out.println("Error reading Excel file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Load Applicants
    public static List<Applicant> loadApplicants(Sheet sheet) {
        if (sheet == null) {
            System.out.println("Sheet not found in Excel. Skipping.");
            return Collections.emptyList();
        }
        
        List<Applicant> applicants = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String name = row.getCell(0).getStringCellValue();
            String nric = row.getCell(1).getStringCellValue();
            int age = (int) row.getCell(2).getNumericCellValue();
            String maritalStatus = row.getCell(3).getStringCellValue();
            String password = row.getCell(4).getStringCellValue();

            applicants.add(new Applicant(name, nric, password, age, maritalStatus));
        }

        return applicants;
    }

    // Load HDB Officers
    public static List<HDBOfficer> loadOfficers(Sheet sheet) {
        if (sheet == null) {
            System.out.println("Sheet not found in Excel. Skipping.");
            return Collections.emptyList();
        }
        List<HDBOfficer> officers = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String name = row.getCell(0).getStringCellValue();
            String nric = row.getCell(1).getStringCellValue();
            int age = (int) row.getCell(2).getNumericCellValue();
            String maritalStatus = row.getCell(3).getStringCellValue();
            String password = row.getCell(4).getStringCellValue();

            officers.add(new HDBOfficer(name, nric, password, age, maritalStatus));
        }

        return officers;
    }

    // Load HDB Managers
    public static List<HDBManager> loadManagers(Sheet sheet) {
        if (sheet == null) {
            System.out.println("Sheet not found in Excel. Skipping.");
            return Collections.emptyList();
        }

        List<HDBManager> managers = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String name = row.getCell(0).getStringCellValue();
            String nric = row.getCell(1).getStringCellValue();
            int age = (int) row.getCell(2).getNumericCellValue();
            String maritalStatus = row.getCell(3).getStringCellValue();
            String password = row.getCell(4).getStringCellValue();

            managers.add(new HDBManager(name, nric, password, age, maritalStatus));
        }

        return managers;
    }

    // Load Projects
    public static List<Project> loadProjects(Sheet sheet) {
        if (sheet == null) {
            System.out.println("ProjectListing sheet not found in Excel. Skipping.");
            return Collections.emptyList();
        }

        List<Project> projects = new ArrayList<>();
    
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String projectName = row.getCell(0).getStringCellValue();
            String neighborhood = row.getCell(1).getStringCellValue();

            // 2-Room
            String type1 = row.getCell(2).getStringCellValue();
            int units1 = (int) row.getCell(3).getNumericCellValue();
            // double price1 = row.getCell(4).getNumericCellValue();

            // 3-Room
            String type2 = row.getCell(5).getStringCellValue();
            int units2 = (int) row.getCell(6).getNumericCellValue();
            // double price2 = row.getCell(7).getNumericCellValue();

            LocalDate openDate = row.getCell(8).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate closeDate = row.getCell(9).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String managerName = row.getCell(10).getStringCellValue();
            int officerSlots = (int) row.getCell(11).getNumericCellValue();

            String officerNames = (row.getCell(12) != null) ? row.getCell(12).getStringCellValue().trim() : "";
            String[] officerName = officerNames.split(",");

            String visibilityCell = (row.getCell(13) != null) ? row.getCell(13).getStringCellValue().trim() : "TRUE";
            boolean visibility = visibilityCell.equalsIgnoreCase("true");
            
            // Create and populate the project
            Project project = new Project(projectName, neighborhood, openDate, closeDate, visibility, officerSlots, managerName);
            project.addFlatType(type1, units1);
            project.addFlatType(type2, units2);
            
            for (String officer : officerName) {
                if (!officer.trim().isEmpty()) { // ensure the name is not like ""
                    project.addOfficer(officer);
                }
            }
            
            projects.add(project);
        }

        return projects;
    }

    // load applications
    public static List<Application> loadApplications(Sheet sheet, AuthController authController) {
        if (sheet == null) {
            System.out.println("Application sheet not found in Excel. Skipping.");
            return Collections.emptyList();
        }

        List<Application> applications = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            // String name = row.getCell(0).getStringCellValue();
            String nric = row.getCell(1).getStringCellValue();
            // int age = (int) row.getCell(2).getNumericCellValue();
            // String maritalStatus = row.getCell(3).getStringCellValue();
            String flatType = row.getCell(4).getStringCellValue();
            String projectName = row.getCell(5).getStringCellValue();
            LocalDate applicationDate = row.getCell(6).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String statusString = row.getCell(7).getStringCellValue();

            // validate objects before creation
            User user = authController.getUserByNRIC(nric);
            if (!(user instanceof Applicant applicant)) {
                continue;
            }

            Project project = ProjectRegistry.getProjectByName(projectName);
            if (project == null) {
                System.out.println("Project " + projectName + " not found. Skipping application.");
                continue;
            }
            
            Application application = new Application(applicant, project, flatType);
            application.setStatus(Application.Status.valueOf(statusString.toUpperCase()));
            application.setApplicationDate(applicationDate);

            applications.add(application);
        }

        return applications;
    }
    
    public static List<Enquiry> loadEnquiries(Sheet sheet) {
        if (sheet == null) {
            System.out.println("Enquiries sheet not found in Excel. Skipping.");
            return Collections.emptyList();
        }

        List<Enquiry> enquiries = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            int enquiryId = (int) row.getCell(0).getNumericCellValue();
            String senderNRIC = row.getCell(1).getStringCellValue();
            String projectName = row.getCell(2).getStringCellValue();
            String content = row.getCell(3).getStringCellValue();
            String reply = row.getCell(4) != null ? row.getCell(4).getStringCellValue() : null;
            String staffName = row.getCell(5) != null ? row.getCell(5).getStringCellValue() : null;

            Enquiry enquiry = new Enquiry(senderNRIC, projectName, content);
            enquiry.setEnquiryId(enquiryId); // Set correct ID manually
            if (reply != null && !reply.isEmpty()) {
                enquiry.reply(reply);
            }
            if (staffName != null && !staffName.isEmpty()) {
                enquiry.setReplyBy(staffName);
            }

            enquiries.add(enquiry);
        }

        return enquiries;
    }    
}
