package org.example;

/**
 * Represents an enrollee with personal and insurance-related details.
 * This class is used to store and manage information about an enrollee.
 */
public record Enrolled(String userId, String firstName, String lastName, Integer version, String insuranceCompany) {
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
