package util;

import model.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExcelWriter {
    private static final String filePath = "src/data/CombinedExcel.xlsx";

    public static void updateUserPassword(User user, String newPassword) {    
        String sheetName;

        switch (user.getRole()) {
            case "Applicant" -> sheetName = "Applicants";
            case "HDBOfficer" -> sheetName = "Officers";
            case "HDBManager" -> sheetName = "Managers";
            default -> {
                System.out.println("Unknown role: " + user.getRole());
                return;
            }
        }

        try (FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.out.println("Sheet not found for role: " + sheetName);
                return;
            }

            boolean found = false;

            for (Row row : sheet) {
                Cell nricCell = row.getCell(1);
                if (nricCell != null && user.getNric().equalsIgnoreCase(nricCell.getStringCellValue())) {
                    Cell passwordCell = row.getCell(4);
                    if (passwordCell == null)
                        passwordCell = row.createCell(4);

                    passwordCell.setCellValue(newPassword);
                    found = true;
                    break;
                }
            }

            if (found) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                    System.out.println("Password updated in Excel.");
                }
            } else {
                System.out.println("User not found in Excel.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error updating password in Excel.");
        }
    }
    
    private static void removeRow(Sheet sheet, int rowIndex) {
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
        } else if (rowIndex == lastRowNum) {
            Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }
    
    public static void writeNewProjectInfo(Project project) {
        System.out.println("Project " + project.getName() + " is being written to Excel file...");
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("ProjectListings");
            if (sheet == null) {
                sheet = workbook.createSheet("ProjectListings");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Project Name");
                header.createCell(1).setCellValue("Neighborhood");
                header.createCell(2).setCellValue("Type 1");
                header.createCell(3).setCellValue("Units Type 1");
                header.createCell(4).setCellValue("Price");
                header.createCell(5).setCellValue("Type 2");
                header.createCell(6).setCellValue("Units Type 2");
                header.createCell(7).setCellValue("Price");
                header.createCell(8).setCellValue("Open Date");
                header.createCell(9).setCellValue("Close Date");
                header.createCell(10).setCellValue("Manager");
                header.createCell(11).setCellValue("Max Officer Slots");
                header.createCell(12).setCellValue("Officer");
            }

            int lastRowNum = sheet.getLastRowNum();
            int rowIndex = lastRowNum + 1;

            Row row = sheet.createRow(rowIndex);

            row.createCell(0).setCellValue(project.getName());
            row.createCell(1).setCellValue(project.getNeighborhood());
            List<FlatType> flatList = new ArrayList<>(project.getFlatTypes().values());

            if (flatList.size() > 0) {
                FlatType ft1 = flatList.get(0);
                row.createCell(2).setCellValue("2-Room");
                row.createCell(3).setCellValue(ft1.getTotalUnits());
                // row.createCell(4).setCellValue(ft1.getPrice());
            }

            if (flatList.size() > 1) {
                FlatType ft2 = flatList.get(1);
                row.createCell(5).setCellValue("3-Room");
                row.createCell(6).setCellValue(ft2.getTotalUnits());
                // row.createCell(7).setCellValue(ft2.getPrice());
            }

            // row.createCell(8).setCellValue(project.getOpenDate().format(formatter));
            // row.createCell(9).setCellValue(project.getCloseDate().format(formatter));

            // Excel date formatter if not will store as a string val
            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("d/M/yyyy"));

            Cell openDateCell = row.createCell(8);
            openDateCell.setCellValue(java.sql.Date.valueOf(project.getOpenDate()));
            openDateCell.setCellStyle(dateCellStyle);

            Cell closeDateCell = row.createCell(9);
            closeDateCell.setCellValue(java.sql.Date.valueOf(project.getCloseDate()));
            closeDateCell.setCellStyle(dateCellStyle);

            row.createCell(10).setCellValue(project.getManagerName());
            row.createCell(11).setCellValue(project.getMaxOfficerSlots());

            // Write back to the same file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
                System.out.println("Projects appended successfully to: " + filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteProjectFromExcel(String projectName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {
    
            Sheet sheet = workbook.getSheet("ProjectListings"); 
            boolean found = false;
    
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0); // assuming project name is in column 0
                    if (cell != null && projectName.equalsIgnoreCase(cell.getStringCellValue())) {
                        removeRow(sheet, i);
                        found = true;
                        break;
                    }
                }
            }
    
            if (found) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    workbook.write(fos);
                    System.out.println("Project deleted from Excel.");
                }
            } else {
                System.out.println("Project not found in Excel.");
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error deleting project from Excel.");
        }
    }
    
    public static void writeNewApplication(Applicant applicant, Project project, String flatType) {

        try (FileInputStream fis = new FileInputStream(filePath);
            Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet("FlatBookings");
            if (sheet == null) {
                sheet = workbook.createSheet("FlatBookings");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Name");
                header.createCell(1).setCellValue("NRIC");
                header.createCell(2).setCellValue("Age");
                header.createCell(3).setCellValue("Marital Status");
                header.createCell(4).setCellValue("Flat Type Booked");
                header.createCell(5).setCellValue("Project Name");
                header.createCell(6).setCellValue("Submission Date");
                header.createCell(7).setCellValue("Application Status");
            }

            int lastRow = sheet.getLastRowNum();
            Row newRow = sheet.createRow(lastRow + 1);

            newRow.createCell(0).setCellValue(applicant.getName());
            newRow.createCell(1).setCellValue(applicant.getNric());
            newRow.createCell(2).setCellValue(applicant.getAge());
            newRow.createCell(3).setCellValue(applicant.getMaritalStatus());
            newRow.createCell(4).setCellValue(flatType);
            newRow.createCell(5).setCellValue(project.getName());
            newRow.createCell(6).setCellValue(LocalDate.now().format(DateTimeFormatter.ofPattern("d/M/yyyy")));
            newRow.createCell(7).setCellValue("PENDING");

            // Date
            // Cell dateCell = newRow.createCell(6);
            // dateCell.setCellValue(LocalDate.now().format(DateTimeFormatter.ofPattern("d/M/yyyy")));

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
                System.out.println("Application saved to Excel.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error writing application to Excel.");
        }
    }
}
