package view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import controller.ManagerController;
import model.FlatType;
import model.HDBManager;
import model.Project;
import model.ProjectRegistry;

public class ProjectManagementCLI {
    private final HDBManager manager;
    private final ManagerController managerController;
    private final Scanner scanner;

    public ProjectManagementCLI(HDBManager manager, ManagerController managerController, Scanner scanner) {
        this.manager = manager;
        this.managerController = managerController;
        this.scanner = scanner;
    }
    
    public void start() {
        int choice;
        do {
            showMenu();
            choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> viewAllProjects();
                case 2 -> viewCreatedProjects();
                case 3 -> createProject();
                case 4 -> editProjectDetails();
                case 5 -> toggleProjectVisibility();
                case 6 -> deleteProject();
                case 7 -> System.out.println("Exiting...");            
                default -> System.out.println("Invalid option");
            }
        } while (choice != 7);
    }

    private void showMenu() {
        System.out.println("\n=== Project Management Menu ===");
        System.out.println("1. View All Projects");
        System.out.println("2. View My Projects");
        System.out.println("3. Create Project");
        System.out.println("4. Edit Project Details");
        System.out.println("5. Toggle Project Visibility");
        System.out.println("6. Delete Project");
        System.out.println("7. Exit");
        System.out.print("Enter your choice: ");
    }

    private void viewAllProjects() {
        managerController.viewAllProject();
    }

    private void createProject() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        LocalDate today = LocalDate.now();

        System.out.print("Project Name: ");
        String name = scanner.nextLine();
        System.out.print("Neighborhood: ");
        String neighbourhood = scanner.nextLine();
        System.out.print("2-Room Units: ");
        int twoRoom = Integer.parseInt(scanner.nextLine());
        System.out.print("3-Room Units: ");
        int threeRoom = Integer.parseInt(scanner.nextLine());
        System.out.print("Max Officer Slots: ");
        int maxSlots = Integer.parseInt(scanner.nextLine());

        LocalDate openDate = null;
        while (openDate == null) {
            System.out.print("Enter Project Open Date (dd/MM/yyyy): ");
            String openDateStr = scanner.nextLine();
            try {
                openDate = LocalDate.parse(openDateStr, formatter);
                if (openDate.isBefore(today)) {
                    System.out.println("Open date cannot be in the past. Please try again.");
                    openDate = null;
                }
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }

        LocalDate closeDate = null;
        while (closeDate == null) {
            System.out.print("Enter Project Close Date (dd/MM/yyyy): ");
            String closeDateStr = scanner.nextLine();
            try {
                if (closeDateStr.matches("\\d+")) {
                    int days = Integer.parseInt(closeDateStr);
                    closeDate = openDate.plusDays(days);
                } else {
                    closeDate = LocalDate.parse(closeDateStr, formatter);
                }
                // closeDate = LocalDate.parse(closeDateStr, formatter);
                if (closeDate.isBefore(openDate)) {
                    System.out.println("Close date cannot be before open date. Please try again.");
                    closeDate = null;
                }
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }

        Project p = new Project(name, neighbourhood, openDate, closeDate, true, maxSlots, manager.getName());
        p.addFlatType("2-Room", twoRoom);
        p.addFlatType("3-Room", threeRoom);
        managerController.createProject(p, manager);
    }

    private void editProjectDetails() {
        Project project = ProjectRegistry.getProjectByName(manager.getAssignedProject());

        if (project == null) {
            System.out.println("You have not created any projects / are not assigned to one");
            return;
        }

        boolean editing = true;
        while (editing) {
            System.out.println("\n--- Edit Project: " + project.getName() + " ---");
            System.out.println("1. Edit Neighborhood (Current: " + project.getNeighborhood() + ")");
            System.out.println("2. Edit Open Date (Current: " + project.getOpenDate() + ")");
            System.out.println("3. Edit Close Date (Current: " + project.getCloseDate() + ")");
            System.out.println("4. Edit Flat Units");
            System.out.println("5. Edit Officer Slots (Current: " + project.getMaxOfficerSlots() + ")");
            System.out.println("6. Toggle Visibility (Currently: " + (project.isVisible() ? "Visible" : "Hidden") + ")");
            System.out.println("7. Back");
            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter new neighborhood: ");
                    String neighborhood = scanner.nextLine().trim();
                    if (managerController.updateNeighborhood(manager, neighborhood)) {
                        System.out.println("Neighborhood updated.");
                    } else {
                        System.out.println("Update failed.");
                    }
                }
                case 2 -> {
                    System.out.print("Enter new open date (YYYY-MM-DD): ");
                    try {
                        LocalDate date = LocalDate.parse(scanner.nextLine().trim());
                        System.out.println(managerController.updateOpenDate(manager, date) ? "Open date updated." : "Update failed.");
                    } catch (Exception e) {
                        System.out.println("Invalid date format.");
                    }
                }
                case 3 -> {
                    System.out.print("Enter new close date (YYYY-MM-DD): ");
                    try {
                        LocalDate date = LocalDate.parse(scanner.nextLine().trim());
                        System.out.println(managerController.updateCloseDate(manager, date) ? "Close date updated." : "Update failed.");
                    } catch (Exception e) {
                        System.out.println("Invalid date format.");
                    }
                }
                case 4 -> {
                    for (String type : project.getFlatTypes().keySet()) {
                        System.out.print("Enter new units for " + type + ": ");
                        try {
                            int units = Integer.parseInt(scanner.nextLine().trim());
                            boolean success = managerController.updateFlatUnits(manager, type, units);
                            System.out.println(success ? "Updated." : "Failed to update " + type);
                        } catch (Exception e) {
                            System.out.println("Invalid number.");
                        }
                    }
                }
                case 5 -> {
                    System.out.print("Enter new number of officer slots: ");
                    try {
                        int slots = Integer.parseInt(scanner.nextLine().trim());
                        System.out.println(managerController.updateOfficerSlots(manager, slots) ? "Officer slots updated." : "Failed.");
                    } catch (Exception e) {
                        System.out.println("Invalid number.");
                    }
                }
                case 6 -> {
                    System.out.println(managerController.toggleVisibility(manager) ? "Visibility toggled." : "Failed to update visibility.");
                }
                case 7 -> editing = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void deleteProject() {
        System.out.print("Enter Project Name to delete: ");
        String name = scanner.nextLine();

        System.out.print("Are you sure you want to delete this project? (Y/N): ");
        String confirm = scanner.nextLine().trim().toUpperCase();
        if (confirm.equals("Y")) {
            managerController.deleteProject(name);
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    
    private void toggleProjectVisibility() {
        System.out.print("Enter Project Name to toggle visibility: ");
        String name = scanner.nextLine();
        managerController.toggleProjectVisibility(name);
    }

    private void viewCreatedProjects() {
        List<Project> created = managerController.getProjectsCreatedByManager(manager);
        
        if (created.isEmpty()) {
            System.out.println("You haven’t created any projects yet.");
            return;
        }

        System.out.println("\n=== Projects Created By You ===");
        for (Project p : created) {
            System.out.println("- Name: " + p.getName());
            System.out.println("  Neighborhood: " + p.getNeighborhood());
            System.out.println("  Open: " + p.getOpenDate() + " | Close: " + p.getCloseDate());
            System.out.println("  Officer(s): " + (p.getOfficerList().isEmpty() ? "No officer assigned" : String.join(", ", p.getOfficerList())));
            System.out.println("  Flat Types:");
            for (FlatType ft : p.getFlatTypes().values()) {
                System.out.println("  -" +ft.getType() + ": " + ft.getRemainingUnits() + " units");
            }
            System.out.println("  Visibility: " + (p.isVisible() ? "Visible" : "Hidden"));
            System.out.println("------------------------------------");
        }
    }

}
