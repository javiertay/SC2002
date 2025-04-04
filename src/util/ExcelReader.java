package util;

import model.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

        public ExcelData(List<Applicant> applicants, List<HDBOfficer> officers,
                         List<HDBManager> managers, List<Project> projects) {
            this.applicants = applicants;
            this.officers = officers;
            this.managers = managers;
            this.projects = projects;
        }
    }

    public static ExcelData loadAllData(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet applicantSheet = workbook.getSheet("Applicants");
            Sheet officerSheet = workbook.getSheet("Officers");
            Sheet managerSheet = workbook.getSheet("Managers");
            Sheet projectSheet = workbook.getSheet("ProjectListings");

            List<Applicant> applicants = loadApplicants(applicantSheet);
            List<HDBOfficer> officers = loadOfficers(officerSheet);
            List<HDBManager> managers = loadManagers(managerSheet);
            List<Project> projects = loadProjects(projectSheet);

            return new ExcelData(applicants, officers, managers, projects);

        } catch (IOException e) {
            System.out.println("Error reading Excel file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Load Applicants
    public static List<Applicant> loadApplicants(Sheet sheet) {
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
            
            // Create and populate the project
            Project project = new Project(projectName, neighborhood, openDate, closeDate, true, officerSlots, managerName);
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
    
}
