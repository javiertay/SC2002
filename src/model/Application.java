package model;

import java.time.LocalDate;

public class Application {
    private Applicant applicant;
    private Project project;
    private String flatType; // "2-Room" or "3-Room"
    private Status status;
    private LocalDate applicationDate;
    private Boolean withdrawalRequested = false;

    public enum Status {
        PENDING, SUCCESSFUL, UNSUCCESSFUL, BOOKED, WITHDRAWN
    }

    public Application(Applicant applicant, Project project, String flatType) {
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.status = Status.PENDING; // Default to pending on submission
        this.applicationDate = LocalDate.now(); // Default to current date
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public Project getProject() {
        return project;
    }

    public String getFlatType() {
        return flatType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status newStatus) {
        this.status = newStatus;
    }

    public boolean isBookable() {
        return this.status == Status.SUCCESSFUL;
    }

    public boolean isBooked() {
        return this.status == Status.BOOKED;
    }

    public boolean isWithdrawalRequested() {
        return withdrawalRequested;
    }

    public void setWithdrawalRequested(boolean requested) {
        this.withdrawalRequested = requested;
    }

    @Override
    public String toString() {
        return "Application[" +
                "Applicant=" + applicant.getNric() +
                ", Project=" + project.getName() +
                ", FlatType=" + flatType +
                ", Status=" + status +
                ']';
    }
}
