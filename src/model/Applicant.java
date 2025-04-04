package model;

public class Applicant extends User {
    private boolean hasApplied = false;

    public Applicant(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
    }

    public boolean canApply(String flatType) {
        if (maritalStatus.equalsIgnoreCase("Single") && age >= 35) {
            return flatType.equals("2-Room");
        } else if (maritalStatus.equalsIgnoreCase("Married") && age >= 21) {
            return true;
        }
        return false;
    }

    public boolean hasApplied() {
        return hasApplied;
    }

    public void setHasApplied(boolean applied) {
        this.hasApplied = applied;
    }

    @Override
    public String getRole() {
        return "Applicant";
    }
}
