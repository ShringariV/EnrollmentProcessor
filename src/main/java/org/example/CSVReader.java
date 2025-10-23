package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A utility class for reading data from a CSV file and transforming it into a list of Enrollee objects.
 */
public class CSVReader {
    /**
     * Reads the contents of a CSV file and converts each valid line into an {@link Enrollee} object.
     * <p>
     * This method:
     * <ul>
     *     <li>Skips the header line.</li>
     *     <li>Splits each subsequent line by commas.</li>
     *     <li>Trims whitespace from all fields.</li>
     *     <li>Parses the version field as an integer.</li>
     *     <li>Splits the full name into first and last names (if both exist).</li>
     * </ul>
     *
     * @param filePath the path to the CSV file.
     * @return a list of {@link Enrollee} objects parsed from the CSV file.
     */
    public static List<Enrollee> readEnrollees(String filePath){
        List<Enrollee> enrollees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Skip the header line
            br.readLine();
            // Read csv file line by line
            while ((line = br.readLine()) != null) {
                // Split by delimiter
                String[] values = line.split(",");
                // Validate line format
                if (values.length == 4) {
                    Enrollee enrollee = getEnrollee(values);
                    System.out.println(enrollee.toString());
                    enrollees.add(enrollee);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return enrollees;
    }
    /**
     * Helper method that constructs an {@link Enrollee} object from a line's values.
     * <p>
     * Handles splitting the full name into first and last names when possible.
     * If only one name is provided, it is treated as the first name.
     *
     * @param values an array of CSV values: [userId, fullName, version, insuranceCompany].
     * @return a fully constructed {@link Enrollee} instance.
     * @throws NumberFormatException if the version field cannot be parsed as an integer.
     */
    private static Enrollee getEnrollee(String[] values) {
        String userId = values[0].trim();
        String fullName = values[1].trim();
        Integer version = Integer.parseInt(values[2].trim());
        String insuranceCompany = values[3].trim();

        String firstName = "";
        String lastName = "";
        String[] nameParts = fullName.split(" ");

        // Handle one or two name parts gracefully
        if (nameParts.length == 2) {
            firstName = nameParts[0];
            lastName = nameParts[1];
        } else if (nameParts.length == 1) {
            firstName = nameParts[0];
        }

        return new Enrollee(userId, firstName, lastName, version, insuranceCompany);
    }
}
