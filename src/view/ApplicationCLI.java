package view;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import controller.ApplicationController;
import model.*;
import util.*;

public class ApplicationCLI {
    private final ApplicationController applicationController;
    private final Filter filter;
    private final Scanner scanner;
    private Breadcrumb breadcrumb;

    public ApplicationCLI(ApplicationController applicationController, String nric, Map<String, Filter> userFilters) {
        this.applicationController = applicationController;
        this.filter = userFilters.computeIfAbsent(nric, k -> new Filter());
        this.scanner = new Scanner(System.in);
    }    

    public void setBreadcrumb(Breadcrumb breadcrumb) {
        this.breadcrumb = breadcrumb;
    }

    public void start(Applicant applicant) {
        int choice;
        do {
            showMenu();
            choice = InputUtil.readInt(scanner);

            switch (choice) {
                case 1 -> viewAllAvailableProject(applicant);
                case 2 -> viewApplicationStatus(applicant);
                case 3 -> withdrawApplication(applicant);
                case 0 -> {
                    System.out.println("Exiting Application Management.");
                    System.out.println();
                }
                default -> System.out.println("Invalid option. Please try again.");
            }

        } while (choice != 0);
    }

    private void showMenu() {
        System.out.println("\n=== " + breadcrumb.getPath() + " ===");
        System.out.println("1. View Available Projects");
        System.out.println("2. View Application Status");
        System.out.println("3. Withdraw Application");
        System.out.println("0. Back to Previous Menu");
    }

    private void viewAllAvailableProject(Applicant applicant){
        List<Project> visibleProjects = applicationController.getAllAvailableProjects(applicant);

        if (visibleProjects.isEmpty()) {
            System.out.println("No available projects at the moment.");
            return;
        }

        List<Project> filteredProjects = visibleProjects;
        
        System.out.println("-----------------------------------");
        System.out.println("Current Filters:");
        System.out.println(" - Neighborhood: " + (filter.getNeighbourhood() != null ? filter.getNeighbourhood() : "-"));
        System.out.println(" - Flat Type: " + (filter.getFlatType() != null ? filter.getFlatType() : "-"));
        System.out.println(" - Price Range: " + (filter.getMinPrice() != null && filter.getMaxPrice() != null ? "$" + filter.getMinPrice() + " - $" + filter.getMaxPrice()
                                                    : (filter.getMinPrice() != null ? "More than $" + filter.getMinPrice()
                                                        : (filter.getMaxPrice() != null ? "Less than $" + filter.getMaxPrice() : "-"))));
        System.out.println();

        
        if (!filter.isEmpty()) {
            clearFilters();
            filteredProjects = FilterUtil.applyFilter(visibleProjects, filter);
        } else {
            System.out.print("Would you like to filter the projects? (Y/N): ");
            String response = scanner.nextLine().trim().toLowerCase();
    
            if (response.equals("y")) {
                setProjectFilter(); // prompts user and updates the 'filter' object
                filteredProjects = FilterUtil.applyFilter(visibleProjects, filter);
            }
        }

        if (filteredProjects.isEmpty()) {
            System.out.print("No matching projects found. Clear Filters and try again? (Y/N): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("y")) {
                clearFilters();
            }
            
            return;
        }

        TableUtil.printProjectTable(filteredProjects, applicant, filter); // shows table of projects

        if (!ApplicationRegistry.hasActiveApplication(applicant.getNric())) {
            submitApplication(applicant); // calls method to prompt user to apply only if they have no successful or pending application
        }
    }

    private void submitApplication(Applicant applicant) {
        Project project = null;
        String projectName = null;

        while (project == null) {
            System.out.print("\nEnter Project Name you would like to apply for (or 'back' to cancel): ");
            projectName = scanner.nextLine().trim();
            if (projectName.equalsIgnoreCase("back") || projectName.isEmpty()) return;

            project = ProjectRegistry.getProjectByName(projectName);
            if (project == null) {
                System.out.println("Project not found. Please check the name and try again.");
            }
        }

        String flatType;

        if (applicant.getMaritalStatus().equalsIgnoreCase("single")) {
            if (project.getFlatTypes().get("2-Room").getRemainingUnits() == 0) {
                System.out.println("No available 2-Room flats left in this project.");
                return;
            }
            flatType = "2-Room";
            System.out.println("As a single applicant, you will automatically be assigned a 2-Room flat.");
        } else if (applicant.getMaritalStatus().equalsIgnoreCase("married")) {
            System.out.println("Available Flat Types:");
            project.getFlatTypes().forEach((type, flatTypeObj) -> {
                int remainingUnits = flatTypeObj.getRemainingUnits();
                if (remainingUnits > 0) {
                    System.out.println("- " + type + ": " + remainingUnits + " units left");
                }
            });

            // check for flat type input (allow user to type in caps, lowercase, or mixed case)
            Map<String, FlatType> availableTypes = project.getFlatTypes();
            flatType = null;

            while (flatType == null) {
                System.out.print("Enter Flat Type (e.g., 2-Room or 3-Room): ");
                String input = scanner.nextLine().trim();

                for (String typeKey : availableTypes.keySet()) {
                    if (typeKey.equalsIgnoreCase(input)) {
                        flatType = typeKey; // use actual casing
                        break;
                    }
                }

                if (flatType == null) {
                    System.out.println("Invalid flat type. Please choose from what is available:");
                }
            }
            
        } else {
            System.out.println("You are not eligible to apply for a flat.");
            return;
        }

        boolean success = applicationController.submitApplication(applicant, projectName, flatType);
        System.out.println(success ? "Application process submitted! Pending Approval." : "Application failed. Please try again.");
    }

    private void viewApplicationStatus(Applicant applicant) {
        applicationController.viewApplicationStatus(applicant);
    }

    private void withdrawApplication(Applicant applicant) {
        if (ApplicationRegistry.getApplicationByNRIC(applicant.getNric()) == null) {
            System.out.println("You have no application to withdraw.");
            return;
        }
        
        System.out.print("Are you sure you want to withdraw your application? (Y/N): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if (confirmation.equals("y")) {
            applicationController.reqToWithdrawApp(applicant);
        } else {
            System.out.println("Withdrawal cancelled.");
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
                System.out.println("Project filter cleared.");
            }
        }
    }
    
    
}