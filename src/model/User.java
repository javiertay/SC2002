package model;

public abstract class User {
    protected String name;
    protected String nric;
    protected String password;
    protected int age;
    protected String maritalStatus;

    public User(String name, String nric, String password, int age, String maritalStatus) {
        this.name = name;
        this.nric = nric;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
    }

    public String getName() {
        return name;
    }

    public String getNric() {
        return nric;
    }

    public String getPassword() {
        return password;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public int getAge() {
        return age;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public abstract String getRole();  // For identifying user type
}
