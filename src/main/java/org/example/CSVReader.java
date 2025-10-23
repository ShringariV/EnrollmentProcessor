package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CSVReader {

    /**
     * Reads enrollee data from a CSV file and organizes it into a map where each
     * insurance company is mapped to its corresponding enrollees. Each enrollee is uniquely
     * identified by a combination of user ID and insurance company. In case of duplicates,
     * the enrollee with the highest version is retained.
     *
     * @param filePath the path to the CSV file containing enrollee data. The file should have
     *                 a header row and follow the format:
     *                 UserId, FullName, Version, InsuranceCompany
     * @return a map containing insurance companies as keys, where each key maps
     *         to another map. This inner map has user IDs as keys and the corresponding
     *         {@link Enrollee} objects as values.
     */
    public static Map<String, Map<String, Enrollee>> readEnrollees(String filePath) {
        // Key: Insurance company name
        // Value: Another map where the key is the user ID and the value is the Enrollee object.
        Map<String, Map<String, Enrollee>> companyMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            System.out.println("Reading file: " + filePath);
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 4) continue;

                Enrollee enrollee = getEnrollee(values);
                String company = enrollee.getInsuranceCompany();
                String userId = enrollee.getUserId();

                // Get the map for this company
                Map<String, Enrollee> enrolleesByCompany =
                        companyMap.computeIfAbsent(company, k -> new HashMap<>());

                // Check if an existing enrollee with the same userId exists
                Enrollee existing = enrolleesByCompany.get(userId);

                // Keep the enrollee with the highest version number
                if (existing == null || enrollee.getVersion() > existing.getVersion()) {
                    enrolleesByCompany.put(userId, enrollee);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }

        return companyMap;
    }

    /**
     * Parses a line's values into an {@link Enrollee} object.
     * <p>
     * Splits the full name into first and last names if applicable.
     * If only one name is provided, it is stored as the first name.
     *
     * @param values an array containing CSV fields in the order:
     *               [UserId, FullName, Version, InsuranceCompany]
     * @return a constructed {@link Enrollee} object.
     */
    private static Enrollee getEnrollee(String[] values) {
        String userId = values[0].trim();
        String fullName = values[1].trim();
        int version = Integer.parseInt(values[2].trim());
        String insuranceCompany = values[3].trim();

        String firstName = "";
        String lastName = "";
        String[] nameParts = fullName.split(" ");

        if (nameParts.length >= 2) {
            firstName = nameParts[0];
            lastName = nameParts[1];
        } else if (nameParts.length == 1) {
            firstName = nameParts[0];
        }

        return new Enrollee(userId, firstName, lastName, version, insuranceCompany);
    }
}
