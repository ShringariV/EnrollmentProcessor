package org.example;

/**
 * Represents an enrollee with personal and insurance-related details.
 * This class is used to store and manage information about an enrollee.
 */
public class Enrollee {
    private final String userId;
    private final String firstName;
    private final String lastName;
    private final Integer version;
    private final String insuranceCompany;
    public Enrollee(String userId, String firstName, String lastName, Integer version, String insuranceCompany) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.version = version;
        this.insuranceCompany = insuranceCompany;
    }
    public String getUserId() {
        return userId;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public Integer getVersion() {
        return version;
    }
    public String getInsuranceCompany() {
        return insuranceCompany;
    }
    @Override
    public String toString() {
        return "Enrollee{" +
                "userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", version=" + version +
                ", insuranceCompany='" + insuranceCompany + '\'' +
                '}';
    }
}
