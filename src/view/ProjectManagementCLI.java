package view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import controller.ManagerController;
import model.FlatType;
import model.HDBManager;
import model.Project;
import model.ProjectRegistry;
import util.Breadcrumb;
import util.Filter;
import util.InputUtil;

public class ProjectManagementCLI {
    private final Filter filter;
    private static final Map<String, Filter> managerFilters = new HashMap<>();
    private final HDBManager manager;
    private final ManagerController managerController;
    private final Scanner scanner;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
    private Breadcrumb breadcrumb;

    public ProjectManagementCLI(HDBManager manager, ManagerController managerController, Scanner scanner, Breadcrumb breadcrumb) {
        this.breadcrumb = breadcrumb;
        this.manager = manager;
        this.managerController = managerController;
        this.scanner = scanner;
        this.filter = managerFilters.computeIfAbsent(manager.getNric(), k -> new Filter());
    }
    
    public void start() {
        int choice;
        do {
            showMenu();
            choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> viewAllProjects();
                case 2 -> {
                    breadcrumb.push("My Projects");
                    viewMyProjects();
                    breadcrumb.pop(); // Return to Project Management Hub after exiting My Projects
                }
                case 3 -> createProject();
                case 0 -> System.out.println("Exiting...");            
                default -> System.out.println("Invalid option");
            }
        } while (choice != 0);
    }

    private void showMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. View All BTO Projects");
        System.out.println("2. View My Projects");
        System.out.println("3. Create Project");
        System.out.println("0. Back to Previous Menu");
    }

    private void viewAllProjects() {
        System.out.println("\n------ Current Filters ------");
        System.out.println(" - Neighborhood: " + (filter.getNeighbourhood() != null ? filter.getNeighbourhood() : "-"));
        System.out.println(" - Flat Type: " + (filter.getFlatType() != null ? filter.getFlatType() : "-"));
        System.out.println(" - Price Range: " +
            (filter.getMinPrice() != null && filter.getMaxPrice() != null
                ? "$" + filter.getMinPrice() + " - $" + filter.getMaxPrice()
                : (filter.getMinPrice() != null ? "More than $" + filter.getMinPrice()
                : (filter.getMaxPrice() != null ? "Less than $" + filter.getMaxPrice() : "-")))
        );
        System.out.println();

        if (!filter.isEmpty()) {
            clearFilters();
            managerController.viewAllProject(filter);
        } else {
            System.out.print("Would you like to filter the projects? (Y/N): ");
            String response = scanner.nextLine().trim().toLowerCase();
    
            if (response.equals("y")) {
                setProjectFilter(); // prompts user and updates the 'filter' object
            }
            managerController.viewAllProject(filter);
        }

        // managerController.viewAllProject(filter);
    }

    private void createProject() {
        System.out.println("\n--- Creating New BTO Project (type 'cancel' at any time to abort) ---");

        LocalDate today = LocalDate.now();

        System.out.print("Project Name: ");
        String name = scanner.nextLine().trim();
        if (name.equalsIgnoreCase("cancel") || name.equalsIgnoreCase("")) return;

        System.out.print("Neighborhood: ");
        String neighbourhood = scanner.nextLine().trim();
        if (neighbourhood.equalsIgnoreCase("cancel") || neighbourhood.equalsIgnoreCase("")) return;

        int twoRoom = -1;
        while (twoRoom < 0) {
            System.out.print("Number of 2-Room units that will be available: ");
            try {
                twoRoom = Integer.parseInt(scanner.nextLine().trim());
                if (twoRoom < 0) {
                    System.out.println("Number of units cannot be negative.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Exiting project creation");
                return;
            }
        }

        int priceTwoRoom = -1;
        while (priceTwoRoom < 0) {
            System.out.print("Price of a 2-Room unit: ");
            try {
                priceTwoRoom = Integer.parseInt(scanner.nextLine().trim());
                if (priceTwoRoom < 0) {
                    System.out.println("Price cannot be negative.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Exiting project creation");
                return;
            }
        }

        int threeRoom = -1;
        while (threeRoom < 0) {
            System.out.print("Number of 3-Room units that will be available: ");
            try {
                threeRoom = Integer.parseInt(scanner.nextLine().trim());
                if (threeRoom < 0) {
                    System.out.println("Number of units cannot be negative.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Exiting project creation");
                return;
            }
        }

        int priceThreeRoom = -1;
        while (priceThreeRoom < 0) {
            System.out.print("Price of a 3-Room unit:  ");
            try {
                priceThreeRoom = Integer.parseInt(scanner.nextLine().trim());
                if (priceThreeRoom < 0) {
                    System.out.println("Price of units cannot be negative.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Exiting project creation");
                return;
            }
        }

        int maxSlots = 10; // default
        while (true) {
            System.out.print("Max Officer Slots (Press Enter for default 10): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                break; // use default
            }
            try {
                int val = Integer.parseInt(input);
                if (val < 1 || val > 10) {
                    System.out.println("Officer slots must be between 1 and 10.");
                } else {
                    maxSlots = val;
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }

        LocalDate openDate = null;
        while (openDate == null) {
            System.out.print("Enter Project Open Date (dd/MM/yyyy): ");
            String openDateStr = scanner.nextLine().trim();
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
            System.out.print("Enter Project Close Date (dd/MM/yyyy or how many days the project stays open): ");
            String closeDateStr = scanner.nextLine().trim();
            try {
                if (closeDateStr.matches("\\d+")) {
                    int days = Integer.parseInt(closeDateStr);
                    if (days <= 0) {
                        System.out.println("Duration must be at least 1 day.");
                    } else {
                        closeDate = openDate.plusDays(days);
                    }
                } else {
                    closeDate = LocalDate.parse(closeDateStr, formatter);
                }
                if (closeDate.isBefore(openDate)) {
                    System.out.println("Close date cannot be before open date. Please try again.");
                    closeDate = null;
                }
            } catch (Exception e) {
                System.out.println("Invalid date format. Please try again.");
            }
        }

        Project p = new Project(name, neighbourhood, openDate, closeDate, true, maxSlots, manager.getName());
        p.addFlatType("2-Room", twoRoom, priceTwoRoom);
        p.addFlatType("3-Room", threeRoom, priceThreeRoom);
        managerController.createProject(p, manager);
    }

    private void editProjectDetails(Project project) {
        boolean editing = true;
        while (editing) {
            System.out.println("\n--- Edit Project: " + project.getName() + " ---");
            System.out.println("1. Edit Neighborhood (Current: " + project.getNeighborhood() + ")");
            System.out.println("2. Edit Open Date (Current: " + project.getOpenDate().format(formatter) + ")");
            System.out.println("3. Edit Close Date (Current: " + project.getCloseDate().format(formatter) + ")");
            System.out.println("4. Edit Flat Units");
            System.out.println("5. Edit Officer Slots (Current: " + project.getMaxOfficerSlots() + ")");
            System.out.println("0. Back");
            int choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter new neighborhood: ");
                    String neighborhood = scanner.nextLine().trim();
                    System.out.println(managerController.updateNeighborhood(manager, project.getName(), neighborhood) ? "Neighborhood updated." : "Update failed.");
                }
                case 2 -> {
                    System.out.print("Enter new open date (DD-MM-YYYY): ");
                    try {
                        LocalDate date = LocalDate.parse(scanner.nextLine().trim(), formatter);
                        System.out.println(managerController.updateOpenDate(manager, project.getName(), date) ? "Open date updated." : "Update failed.");
                    } catch (Exception e) {
                        System.out.println("Invalid date format.");
                    }
                }
                case 3 -> {
                    System.out.print("Enter new close date (DD-MM-YYYY): ");
                    try {
                        LocalDate date = LocalDate.parse(scanner.nextLine().trim(), formatter);
                        System.out.println(managerController.updateCloseDate(manager, project.getName(), date) ? "Close date updated." : "Update failed.");
                    } catch (Exception e) {
                        System.out.println("Invalid date format.");
                    }
                }
                case 4 -> {
                    for (String type : project.getFlatTypes().keySet()) {
                        System.out.print("Enter new units for " + type + ": ");
                        try {
                            int units = Integer.parseInt(scanner.nextLine().trim());
                            if (units < 0) {
                                System.out.println("Units cannot be negative.");
                                continue;
                            }
                    
                            System.out.print("Enter new price for " + type + ": ");
                            int price = Integer.parseInt(scanner.nextLine().trim());
                            if (price < 0) {
                                System.out.println("Price cannot be negative.");
                                continue;
                            }

                            boolean success = managerController.updateFlatUnits(manager, project.getName(), type, units, price);
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
                        if (slots < 1 || slots > 10) {
                            System.out.println("Number of officer slots must be between 1 and 10.");
                            break;
                        }
                        System.out.println(managerController.updateOfficerSlots(manager, project.getName(), slots) ? "Officer slots updated." : "Failed.");
                    } catch (Exception e) {
                        System.out.println("Invalid number.");
                    }
                }
                case 0 -> editing = false;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void viewProjectDetails(Project project) {
        System.out.println("  Neighborhood: " + project.getNeighborhood());
        System.out.println("  Open Date: " + project.getOpenDate().format(formatter) + " | Close Date: " + project.getCloseDate().format(formatter));
        System.out.println("  Max officer slots: " + project.getMaxOfficerSlots());
        System.out.println("  Officer(s): " + (project.getOfficerList().isEmpty() ? "No officer assigned" : String.join(", ", project.getOfficerList())));
        System.out.println("  Flat Types:");
        for (FlatType ft : project.getFlatTypes().values()) {
            System.out.println("  -" +ft.getType() + ": " + ft.getRemainingUnits() + " units left at $" + ft.getPrice() + " each");
        }
        System.out.println("  Visibility: " + (project.isVisible() ? "Visible" : "Hidden"));
        System.out.println("------------------------------------");
    }

    private void viewMyProjects() {    
        while (true) {
            List<String> projectNames = manager.getManagedProjects();

            List<Project> projects = projectNames.stream()
                .map(ProjectRegistry::getProjectByName)
                .filter(Objects::nonNull)
                .toList();

            if (projects.isEmpty()) {
                System.out.println("You are not managing any projects.");
                return;
            }

            System.out.println("\n=== " + breadcrumb.getPath() + " ===");
            for (int i = 0; i < projects.size(); i++) {
                Project p = projects.get(i);
                String status = p.getCloseDate().isBefore(LocalDate.now()) ? "Closed" : 
                        p.getOpenDate().isAfter(LocalDate.now())
                    ? "Upcoming: opens " + p.getOpenDate().format(formatter)
                    : "Open: " + p.getOpenDate().format(formatter) + " - " + p.getCloseDate().format(formatter);
                System.out.println((i + 1) + ". " + p.getName() + " (" + status + ")");
            }
            System.out.println("0. Back");
            System.out.print("\nSelect a project to manage: ");
    
            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
    
            if (choice == 0) return;
            
            if (choice >= 1 && choice <= projects.size()) {
                Project selected = projects.get(choice - 1);
                manageProject(selected);
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void manageProject(Project project) {
        while (true) {
            System.out.println("\n--- Managing: " + project.getName() + " ---");
            viewProjectDetails(project);
            System.out.println("1. Edit Project Details");
            System.out.println("2. Toggle Visibility (Currently: " + (project.isVisible() ? "Visible" : "Hidden") + ")");
            System.out.println("3. Delete Project");
            System.out.println("0. Back to Previous Menu");
            System.out.print("Enter choice: ");
    
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> editProjectDetails(project);
                case "2" -> managerController.toggleProjectVisibility(project.getName());
                case "3" -> {
                    System.out.print("Are you sure you want to delete this project? (Y/N): ");
                    String confirm = scanner.nextLine().trim().toUpperCase();
                    if (confirm.equals("Y")) {
                        managerController.deleteProject(manager, project.getName());
                        return;
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                    return;
                }
                case "0" -> { return; }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void setProjectFilter() {
        System.out.println("\n== Set Project Filters ==");

        System.out.print("Enter neighborhoods (comma-separated or leave blank to skip): ");
        String neighborhoodInput = scanner.nextLine().trim();
        if (!neighborhoodInput.isEmpty()) {
            Set<String> neighborhoods = Arrays.stream(neighborhoodInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
            filter.setNeighbourhood(neighborhoods);
        } else {
            filter.setNeighbourhood(null);
        }

    
        System.out.print("Filter by Flat Type (e.g., 2-Room/3-Room, leave blank to skip): ");
        String flatType = scanner.nextLine().trim();
        filter.setFlatType(flatType.isEmpty() ? null : flatType);

        System.out.print("Filter by Minimum Price (leave blank to skip): ");
        String minPrice = scanner.nextLine().trim();
        if (!minPrice.isEmpty()) {
            try {
                filter.setMinPrice(Integer.parseInt(minPrice));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for minimum price. Skipping.");
            }
        }
        
        System.out.print("Filter by Maximum Price (leave blank to skip): ");
        String maxPrice = scanner.nextLine().trim();
        if (!maxPrice.isEmpty()) {
            try {
                filter.setMaxPrice(Integer.parseInt(maxPrice));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for maximum price. Skipping.");
            }
        }
        
        System.out.println("Filter set.");
    }
    
    private void clearFilters() {
        if (!filter.isEmpty()) {
            System.out.print("Do you want to reset all filters? (Y/N): ");
            String confirmation = scanner.nextLine().trim();
            
            if (confirmation.equalsIgnoreCase("y")) {
                filter.clear();
                System.out.println("Filter cleared.");
            }
        }
    }
}