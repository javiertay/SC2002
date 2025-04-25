package model;

/**
* Represents an applicant in the Build-To-Order system.
* 
* Applicants are users who can apply for BTO flats based on age and marital status criteria.
* Extends the {@code User} class.
* 
* Eligibility Rules:
* <ul>
*   <li>Single applicants must be at least 35 years old and can only apply for 2-Room flats.</li>
*   <li>Married applicants must be at least 21 years old and can apply for all flat types.</li>
* </ul>
* 
* @author Javier
* @version 1.0
*/
public class Applicant extends User {
    /**
    * Constructs a new Applicant with the specified details.
    *
    * @param name The name of the applicant.
    * @param nric The NRIC of the applicant.
    * @param password The login password.
    * @param age The applicant's age.
    * @param maritalStatus The marital status ("Single" or "Married").
    */
    public Applicant(String name, String nric, String password, int age, String maritalStatus) {
        super(name, nric, password, age, maritalStatus);
    }

    /**
    * Checks if the applicant is eligible to apply for the given flat type.
    *
    * @param flatType The flat type to check eligibility for.
    * @return True if the applicant meets criteria to apply; false otherwise.
    */
    public boolean canApply(String flatType) {
        if (getMaritalStatus().equalsIgnoreCase("Single") && getAge() >= 35) {
            return flatType.equals("2-Room");
        } else if (getMaritalStatus().equalsIgnoreCase("Married") && getAge() >= 21) {
            return true;
        }
        return false;
    }

    /**
    * Returns the role of the user as a string.
    *
    * @return The role "Applicant".
    */
    @Override
    public String getRole() {
        return "Applicant";
    }
}
