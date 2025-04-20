package util;

import model.Application;

public class Filter {
    // Filters for projects listing
    private String flatType;
    private String neighbourhood;

    // Additional filters for applications listing
    private String projectName;
    private String maritalStatus;
    private Integer minAge;
    private Integer maxAge;
    private Application.Status status;

    public String getFlatType() {
        return flatType;
    }

    public void setFlatType(String flatType) {
        this.flatType = flatType;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Application.Status getStatus() {
        return status;
    }

    public void setStatus(Application.Status status) {
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

    public boolean isEmpty() {
        return flatType == null && neighbourhood == null && projectName == null && maritalStatus == null && minAge == null && maxAge == null && status == null;
    }

    public void clear() {
        flatType = null;
        neighbourhood = null;
        projectName = null;
        maritalStatus = null;
        minAge = null;
        maxAge = null;
        status = null;
    }
}
