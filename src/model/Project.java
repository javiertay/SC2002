package model;

import java.time.LocalDate;
import java.util.*;

import util.Filter;
import util.ISearchable;

/**
* Represents a Build-To-Order (BTO) housing project.
* 
* Each project has a name, neighborhood, application window, visibility status,
* flat type breakdown, and assigned manager/officers.
* 
* Implements {@code ISearchable} to support dynamic filtering for applicants and managers.
* 
* Officer registration is limited by a maximum slot quota, and flat types are customizable.
* 
* @author Javier
* @version 1.0
*/
public class Project implements ISearchable{
    private String name;
    private String neighborhood;
    private Map<String, FlatType> flatTypes;  // "2-Room", "3-Room"
    private LocalDate openDate;
    private LocalDate closeDate;
    private String managerName;
    private int maxOfficerSlots;
    private List<String> officerList; // List of officer names

    private int currentOfficerSlots = 0;
    private boolean visibility;
    
    /**
    * Constructs a new BTO project.
    *
    * @param name Project name.
    * @param neighborhood The estate where the project is located.
    * @param openDate Application start date.
    * @param closeDate Application end date.
    * @param visibility Whether the project is visible to applicants.
    * @param maxOfficerSlots Maximum number of officers allowed to register.
    * @param managerName Name of the manager who created this project.
    */
    public Project(String name, String neighborhood, LocalDate openDate, LocalDate closeDate, boolean visibility, int maxOfficerSlots, String managerName) {
        this.name = name;
        this.neighborhood = neighborhood;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.visibility = visibility;
        this.maxOfficerSlots = maxOfficerSlots;
        this.managerName = managerName;
        this.flatTypes = new HashMap<>();
        this.officerList = new ArrayList<>();
    }

    /**
    * Adds a new flat type configuration to the project.
    *
    * @param type The flat type name (e.g., "2-Room").
    * @param totalUnits Total number of units.
    * @param price Price per unit.
    */
    public void addFlatType(String type, int totalUnits, int price) {
        flatTypes.put(type, new FlatType(type, totalUnits, price));
    }

    /**
    * Gets the flat type object by name.
    *
    * @param type The flat type name.
    * @return The corresponding {@code FlatType}, or null if not found.
    */
    public FlatType getFlatType(String type) {
        return flatTypes.get(type);
    }

    /**
    * Returns all flat types offered in this project.
    *
    * @return Map of flat type names to {@code FlatType} objects.
    */
    public Map<String, FlatType> getFlatTypes() {
        return flatTypes;
    }

    /**
    * Gets the name of the project.
    *
    * @return The project name.
    */
    public String getName() {
        return name;
    }

    /**
    * Gets the neighborhood where the project is located.
    *
    * @return The neighborhood name.
    */
    public String getNeighborhood() {
        return neighborhood;
    }

    /**
    * Updates the neighborhood of the project.
    *
    * @param neighborhood The new neighborhood name.
    */
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    /**
    * Gets the opening date of the application window.
    *
    * @return The open date.
    */
    public LocalDate getOpenDate() {
        return openDate;
    }

    /**
    * Sets the opening date for the project application window.
    *
    * @param openDate The new open date.
    */
    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    /**
    * Gets the closing date of the application window.
    *
    * @return The close date.
    */
    public LocalDate getCloseDate() {
        return closeDate;
    }

    /**
    * Sets the closing date for the project application window.
    *
    * @param closeDate The new close date.
    */
    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    /**
    * Checks if the project is currently visible to applicants.
    *
    * @return True if visible; false otherwise.
    */
    public boolean isVisible() {
        return visibility;
    }

    /**
    * Sets the visibility status of the project.
    *
    * @param visible True to make the project visible; false to hide it.
    */
    public void setVisibility(boolean visible) {
        this.visibility = visible;
    }

    /**
    * Gets the maximum number of officers that can be assigned.
    *
    * @return Maximum officer slots.
    */
    public int getMaxOfficerSlots() {
        return maxOfficerSlots;
    }

    /**
    * Sets the maximum number of officers that can be assigned.
    *
    * @param maxOfficerSlots The slot limit.
    */
    public void setMaxOfficerSlots(int maxOfficerSlots) {
        this.maxOfficerSlots = maxOfficerSlots;
    }

    /**
    * Gets the current number of officers assigned to the project.
    *
    * @return Number of officers assigned.
    */
    public int getCurrentOfficerSlots() {
        return currentOfficerSlots;
    }

    /**
    * Checks if there are open slots available for officer assignment.
    *
    * @return True if additional officers can register; false otherwise.
    */
    public boolean hasAvailableOfficerSlot() {
        return currentOfficerSlots < maxOfficerSlots;
    }

    /**
    * Increments the officer slot counter by one.
    */
    public void incrementOfficerSlot() {
        currentOfficerSlots++;
    }

    /**
    * Gets the name of the manager registered to the project
    *
    * @return Name of the manager handling the project
    */
    public String getManagerName() {
        return managerName;
    }

    /**
    * Gets a list of all officer names registered to the project.
    *
    * @return List of officer names.
    */
    public List<String> getOfficerList() {
        return officerList;
    }
    
    /**
    * Adds a new officer to the project if slots are available.
    *
    * @param officerName The name of the officer to add.
    */
    public void addOfficer(String officerName) {
        if (officerName != null && !officerName.trim().isEmpty() && hasAvailableOfficerSlot()) {
            officerList.add(officerName.trim());
            currentOfficerSlots++;
        }
    }
    
    /**
    * Determines whether this project satisfies the given filter.
    *
    * @param filter The filtering criteria.
    * @return True if the project matches the filter; false otherwise.
    */
    @Override
    public boolean matches(Filter filter) {
        if (filter.getNeighbourhood() != null &&
            filter.getNeighbourhood().stream().noneMatch(
                n -> n.equalsIgnoreCase(this.neighborhood))) {
            return false;
        }

        if (filter.getFlatType() != null) {
            boolean hasMatchingAndAvailableFlatType = flatTypes.entrySet().stream()
                .anyMatch(entry ->
                    entry.getKey().trim().equalsIgnoreCase(filter.getFlatType().trim()) &&
                    entry.getValue().getRemainingUnits() > 0
                );
        
            if (!hasMatchingAndAvailableFlatType) return false;
        }

        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            boolean hasMatchingFlat = false;
            for (FlatType ft : this.getFlatTypes().values()) {
                int price = ft.getPrice();
                if ((filter.getMinPrice() == null || price >= filter.getMinPrice()) &&
                    (filter.getMaxPrice() == null || price <= filter.getMaxPrice())) {
                    hasMatchingFlat = true;
                    break;
                }
            }
            if (!hasMatchingFlat) return false;
        }

        return true;
    }
}