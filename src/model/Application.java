package model;

import java.time.LocalDate;

import util.ISearchable;
import util.Filter;

/**
* Represents a BTO application submitted by an applicant for a specific project and flat type.
* 
* Stores the status of the application and its submission date, and supports filters
* for searching and reporting.
* Implements the {@code ISearchable} interface for dynamic filtering.
* 
* Possible statuses:
* <ul>
*   <li>PENDING</li>
*   <li>SUCCESSFUL</li>
*   <li>UNSUCCESSFUL</li>
*   <li>BOOKED</li>
*   <li>WITHDRAWN</li>
* </ul>
* 
* @author Javier
* @version 1.0
*/
public class Application implements ISearchable{
    private Applicant applicant;
    private Project project;
    private String flatType; // "2-Room" or "3-Room"
    private Status status;
    private LocalDate applicationDate;
    private Boolean withdrawalRequested = false;

    /**
    * Represents the status of an application throughout the BTO process.
    * 
    * <ul>
    *   <li><b>PENDING</b> – Application is under review.</li>
    *   <li><b>SUCCESSFUL</b> – Application has been approved.</li>
    *   <li><b>UNSUCCESSFUL</b> – Application was not successful.</li>
    *   <li><b>BOOKED</b> – A flat has been booked by the applicant.</li>
    *   <li><b>WITHDRAWN</b> – The applicant has withdrawn their application.</li>
    * </ul>
    */
    public enum Status {
        /** The application is awaiting review. */
        PENDING,

        /** The application has been approved. */
        SUCCESSFUL,

        /** The application was rejected. */
        UNSUCCESSFUL,

        /** The applicant has booked a flat. */
        BOOKED,

        /** The applicant has withdrawn their application. */
        WITHDRAWN
    }

    /**
    * Creates a new application for the given applicant and project.
    * Initializes the application date to the current date and status to PENDING.
    *
    * @param applicant The applicant submitting the application.
    * @param project The project the application is for.
    * @param flatType The flat type applied for.
    */
    public Application(Applicant applicant, Project project, String flatType) {
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.status = Status.PENDING; // Default to pending on submission
        this.applicationDate = LocalDate.now(); // Default to current date
    }

    /**
    * Gets the date this application was submitted.
    *
    * @return The application date.
    */
    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    /**
    * Sets the application date.
    *
    * @param applicationDate The date to set.
    */
    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    /**
    * Returns the applicant who submitted this application.
    *
    * @return The applicant.
    */
    public Applicant getApplicant() {
        return applicant;
    }

    /**
    * Returns the project this application is for.
    *
    * @return The project.
    */
    public Project getProject() {
        return project;
    }

    /**
    * Gets the flat type applied for.
    *
    * @return The flat type.
    */
    public String getFlatType() {
        return flatType;
    }

    /**
    * Gets the current application status.
    *
    * @return The status of the application.
    */
    public Status getStatus() {
        return status;
    }

    /**
    * Updates the status of the application.
    *
    * @param newStatus The new status to assign.
    */
    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    /**
    * Checks if the application is eligible to proceed to flat selection.
    *
    * @return True if the status is SUCCESSFUL.
    */
    public boolean isBookable() {
        return this.status == Status.SUCCESSFUL;
    }

    /**
    * Checks if the application has successfully booked a flat.
    *
    * @return True if status is BOOKED.
    */
    public boolean isBooked() {
        return this.status == Status.BOOKED;
    }

    /**
    * Checks if the applicant has requested to withdraw the application.
    *
    * @return True if withdrawal has been requested.
    */
    public boolean isWithdrawalRequested() {
        return withdrawalRequested;
    }

    /**
    * Marks or unmarks the application as having a withdrawal request.
    *
    * @param requested True to mark as withdrawal requested.
    */
    public void setWithdrawalRequested(boolean requested) {
        this.withdrawalRequested = requested;
    }

    /**
    * Checks whether this application matches a given filter.
    *
    * @param filter The filter criteria to apply.
    * @return True if the application satisfies all filter conditions; false otherwise.
    */
    @Override
    public boolean matches(Filter filter) {        
        if (filter.getFlatType() != null &&
            !this.getFlatType().equalsIgnoreCase(filter.getFlatType())) {
            return false;
        }

        if (filter.getProjectName() != null &&
            filter.getProjectName().stream().noneMatch(
                n -> n.equalsIgnoreCase(this.getProject().getName()))) {
            return false;
        }
        

        if (filter.getStatus() != null && !filter.getStatus().contains(this.getStatus())) {
            return false;
        }

        if (filter.getMaritalStatus() != null &&
            !this.getApplicant().getMaritalStatus().equalsIgnoreCase(filter.getMaritalStatus())) {
            return false;
        }

        if (filter.getMinAge() != null &&
            this.getApplicant().getAge() < filter.getMinAge()) {
            return false;
        }

        if (filter.getMaxAge() != null &&
            this.getApplicant().getAge() > filter.getMaxAge()) {
            return false;
        }

        return true;
    }
}
