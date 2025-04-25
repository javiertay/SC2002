package util;

import model.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
* Utility for reading user, project, application, and enquiry data from an Excel file.
* Populates in-memory registries on application startup.
* 
* Depends on Apache POI.
* 
* @author Javier
* @version 1.0
*/
public class ExcelReader {

    public static class ExcelData {
        public final List<Applicant> applicants;
        public final List<HDBOfficer> officers;
        public final List<HDBManager> managers;
        public final List<Project> projects;
        public final List<Application> applications;
        public final List<Enquiry> enquiries;

        /**
        * Constructs an {@code ExcelData} object containing all loaded data from the Excel file.
        *
        * @param applicants   List of loaded applicants.
        * @param officers     List of loaded HDB officers.
        * @param managers     List of loaded HDB managers.
        * @param projects     List of loaded BTO projects.
        * @param applications List of loaded flat applications.
        * @param enquiries    List of loaded enquiries.
        */
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

    /**
    * Loads all relevant sheets from the given Excel file and parses them into lists.
    *
    * @param filePath Path to the Excel file.
    * @return A populated ExcelData object containing all loaded records.
    */
    public static ExcelData loadAllData(String filePath) {
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
            List<Application> applicationsList = loadApplications(applications, applicants, officers, projects);
            List<Enquiry> enquiries = loadEnquiries(enquries);

            return new ExcelData(applicants, officers, managers, projects, applicationsList, enquiries);

        } catch (IOException e) {
            System.out.println("Error reading Excel file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
    * Loads applicants from the given Excel sheet.
    *
    * @param sheet The Excel sheet containing applicant data.
    * @return A list of applicants.
    */
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
            int age = getSafeNumericCellValue((row.getCell(2)));
            String maritalStatus = row.getCell(3).getStringCellValue();
            String password = row.getCell(4).getStringCellValue();

            applicants.add(new Applicant(name, nric, password, age, maritalStatus));
        }

        return applicants;
    }

    /**
    * Loads HDB officers from the given Excel sheet.
    *
    * @param sheet The Excel sheet containing officer data.
    * @return A list of HDB officers.
    */
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
            int age = getSafeNumericCellValue((row.getCell(2)));
            String maritalStatus = row.getCell(3).getStringCellValue();
            String password = row.getCell(4).getStringCellValue();

            officers.add(new HDBOfficer(name, nric, password, age, maritalStatus));
        }

        return officers;
    }

    /**
    * Loads HDB managers from the given Excel sheet.
    *
    * @param sheet The Excel sheet containing manager data.
    * @return A list of HDB managers.
    */
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
            int age = getSafeNumericCellValue((row.getCell(2)));
            String maritalStatus = row.getCell(3).getStringCellValue();
            String password = row.getCell(4).getStringCellValue();

            managers.add(new HDBManager(name, nric, password, age, maritalStatus));
        }

        return managers;
    }

    /**
    * Loads housing projects from the given Excel sheet.
    *
    * @param sheet The Excel sheet containing project data.
    * @return A list of projects.
    */
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
            int units1 = getSafeNumericCellValue(row.getCell(3));
            int price1 = getSafeNumericCellValue((row.getCell(4)));

            // 3-Room
            String type2 = row.getCell(5).getStringCellValue();
            int units2 = getSafeNumericCellValue((row.getCell(6)));
            int price2 = getSafeNumericCellValue((row.getCell(7)));

            LocalDate openDate = row.getCell(8).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate closeDate = row.getCell(9).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String managerName = row.getCell(10).getStringCellValue();
            int officerSlots = getSafeNumericCellValue((row.getCell(11)));

            String officerNames = (row.getCell(12) != null) ? row.getCell(12).getStringCellValue().trim() : "";
            String[] officerName = officerNames.split(",");

            String visibilityCell = (row.getCell(13) != null) ? row.getCell(13).getStringCellValue().trim() : "TRUE";
            boolean visibility = visibilityCell.equalsIgnoreCase("true");
            
            // Create and populate the project
            Project project = new Project(projectName, neighborhood, openDate, closeDate, visibility, officerSlots, managerName);
            project.addFlatType(type1, units1, price1);
            project.addFlatType(type2, units2, price2);
            
            for (String officer : officerName) {
                if (!officer.trim().isEmpty()) { // ensure the name is not like ""
                    project.addOfficer(officer);
                }
            }
            
            projects.add(project);
        }

        return projects;
    }

    /**
    * Loads applications from the given sheet using reference lists for applicants, officers, and projects.
    *
    * @param sheet The Excel sheet containing application data.
    * @param applicants The list of applicants to match against.
    * @param officers The list of officers to match against.
    * @param projects The list of projects to match against.
    * @return A list of applications.
    */
    public static List<Application> loadApplications(Sheet sheet, List<Applicant> applicants, List<HDBOfficer> officers, List<Project> projects) {
        if (sheet == null) {
            System.out.println("Application sheet not found in Excel. Skipping.");
            return Collections.emptyList();
        }

        // load all valid applicants
        Map<String, Applicant> applicantMap = new HashMap<>();
        for (Applicant a : applicants) applicantMap.put(a.getNric().toUpperCase(), a);
        for (HDBOfficer o : officers) applicantMap.put(o.getNric().toUpperCase(), o); 

        // load valid projects
        Map<String, Project> projectMap = new HashMap<>();
        for (Project p : projects) projectMap.put(p.getName().toLowerCase(), p);

        List<Application> applications = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            // String name = row.getCell(0).getStringCellValue();
            String nric = row.getCell(1).getStringCellValue().toUpperCase();
            // int age = getSafeNumericCellValue((row.getCell(2)));
            // String maritalStatus = row.getCell(3).getStringCellValue();
            String flatType = row.getCell(4).getStringCellValue();
            String projectName = row.getCell(5).getStringCellValue();
            LocalDate applicationDate = row.getCell(6).getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String statusString = row.getCell(7).getStringCellValue();


            // validate objects before creation
            Applicant applicant = applicantMap.get(nric);
            if (applicant == null) {
                System.out.println("Skipping NRIC " + nric + ": Not a valid applicant or officer.");
                continue;
            }

            Project project = projectMap.get(projectName.toLowerCase());
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
    
    /**
    * Loads enquiries from the given Excel sheet.
    *
    * @param sheet The Excel sheet containing enquiry data.
    * @return A list of enquiries.
    */
    public static List<Enquiry> loadEnquiries(Sheet sheet) {
        if (sheet == null) {
            System.out.println("Enquiries sheet not found in Excel. Skipping.");
            return Collections.emptyList();
        }

        List<Enquiry> enquiries = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            int enquiryId = getSafeNumericCellValue((row.getCell(0)));
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
    
    /**
    * Safely extracts an integer value from a numeric or string cell.
    * <p>
    * Accepts both numeric and string types. If the cell is null or contains
    * non-numeric content, a {@code NumberFormatException} is thrown.
    *
    * @param cell The Excel cell to extract the numeric value from.
    * @return The extracted integer value.
    * @throws NumberFormatException If the cell is null or contains invalid content.
    */
    private static int getSafeNumericCellValue(Cell cell) throws NumberFormatException {
        if (cell == null) throw new NumberFormatException("Cell is null");
    
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String str = cell.getStringCellValue().trim();
            if (!str.isEmpty()) {
                return Integer.parseInt(str);
            }
        }
    
        throw new NumberFormatException("Invalid cell format at row " + cell.getRowIndex() + ", col " + cell.getColumnIndex());
    }
}
