package model;

import java.time.LocalDate;
import java.util.*;

public class Project {
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

    // Add flat type info (e.g., during creation)
    public void addFlatType(String type, int totalUnits) {
        flatTypes.put(type, new FlatType(type, totalUnits));
    }

    public FlatType getFlatType(String type) {
        return flatTypes.get(type);
    }

    public Map<String, FlatType> getFlatTypes() {
        return flatTypes;
    }

    public String getName() {
        return name;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public boolean isVisible() {
        return visibility;
    }

    public void setVisibility(boolean visible) {
        this.visibility = visible;
    }

    public int getMaxOfficerSlots() {
        return maxOfficerSlots;
    }

    public int getCurrentOfficerSlots() {
        return currentOfficerSlots;
    }

    public boolean hasAvailableOfficerSlot() {
        return currentOfficerSlots < maxOfficerSlots;
    }

    public void incrementOfficerSlot() {
        currentOfficerSlots++;
    }

    public String getManagerName() {
        return managerName;
    }

    public List<String> getOfficerList() {
        return officerList;
    }
    
    public void addOfficer(String officerName) {
        if (officerName != null && !officerName.trim().isEmpty() && hasAvailableOfficerSlot()) {
            officerList.add(officerName.trim());
            currentOfficerSlots++;
        }
    }
    
}