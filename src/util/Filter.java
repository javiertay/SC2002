package util;

import java.util.Set;

import model.Application;

/**
* Represents a reusable set of filter criteria for searching and listing
* projects or applications based on flat type, price, age, marital status, etc.
* 
* Used by FilterUtil and searchable views.
* 
* @author Javier
* @version 1.0
*/
public class Filter {
    // Filters for projects listing
    private String flatType;
    private Set<String> neighbourhood;
    private Integer minPrice;
    private Integer maxPrice;

    // Additional filters for applications listing
    private Set<String> projectName;
    private String maritalStatus;
    private Integer minAge;
    private Integer maxAge;
    private Set<Application.Status> status;

    public String getFlatType() {
        return flatType;
    }

    public void setFlatType(String flatType) {
        this.flatType = flatType;
    }

    public Set<String> getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(Set<String> neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public Set<String> getProjectName() {
        return projectName;
    }

    public void setProjectName(Set<String> projectName) {
        this.projectName = projectName;
    }

    public Set<Application.Status> getStatus() {
        return status;
    }

    public void setStatus(Set<Application.Status> status) {
        this.status = status;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    /**
    * Checks if the filter is currently empty (i.e., no criteria applied).
    *
    * @return True if no filter criteria are set; false otherwise.
    */
    public boolean isEmpty() {
        return flatType == null && neighbourhood == null && projectName == null && maritalStatus == null && minAge == null && maxAge == null && status == null && minPrice == null && maxPrice == null;
    }

    /**
    * Resets all filter criteria to null.
    */
    public void clear() {
        flatType = null;
        neighbourhood = null;
        projectName = null;
        maritalStatus = null;
        minAge = null;
        maxAge = null;
        status = null;
        minPrice = null;
        maxPrice = null;
    }
}
