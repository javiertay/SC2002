package model;

/**
* Abstract base class representing a user in the Build-To-Order system.
* 
* All users share common attributes such as name, NRIC, password, age, and marital status.
* Subclasses include {@code Applicant}, {@code HDBOfficer}, and {@code HDBManager}.
* 
* Provides accessors and a password change method. Each subclass must implement {@code getRole()}.
* 
* @author Javier
* @version 1.0
*/
public abstract class User {
    private String name;
    private String nric;
    private String password;
    private int age;
    private String maritalStatus;

    /**
    * Constructs a user with the given details.
    *
    * @param name The full name of the user.
    * @param nric The NRIC of the user.
    * @param password The login password.
    * @param age The user's age.
    * @param maritalStatus The marital status of the user.
    */
    public User(String name, String nric, String password, int age, String maritalStatus) {
        this.name = name;
        this.nric = nric;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
    }

    /**
    * Returns the user's full name.
    *
    * @return The name.
    */
    public String getName() {
        return name;
    }

    /**
    * Returns the user's NRIC.
    *
    * @return The NRIC.
    */
    public String getNric() {
        return nric;
    }

    /**
    * Returns the user's password.
    *
    * @return The password.
    */
    public String getPassword() {
        return password;
    }

    /**
    * Changes the user's password to the specified new value.
    *
    * @param newPassword The new password to set.
    */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
    * Returns the user's age.
    *
    * @return The age.
    */
    public int getAge() {
        return age;
    }

    /**
    * Returns the user's marital status.
    *
    * @return The marital status.
    */
    public String getMaritalStatus() {
        return maritalStatus;
    }

    /**
    * Returns the role of the user.
    * This method must be implemented by subclasses.
    *
    * @return A string representing the user role.
    */
    public abstract String getRole();  // For identifying user type
}
