package org.example;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVReader {
    private static final Logger logger = Logger.getLogger(CSVReader.class.getName());
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CSVReader.class);

    private CSVReader() {
        throw new IllegalStateException("Utility class");
    }
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
     *         {@link Enrolled} objects as values.
     */
    public static Map<String, Map<String, Enrolled>> readEnrollees(String filePath) throws IOException {
        // open file as stream
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            return
                    // skip header
                    lines.skip(1)
                            // split each line
                            .map(line -> line.split(","))
                            // skip lines without all 4 fields
                            .filter(values -> values.length >= 4)
                            // Convert each line into enrolled record
                            .map(CSVReader::getEnrollee)
                            // skip invalid rows
                            .filter(Objects::nonNull)
                            // group by insurance company
                            .collect(Collectors.groupingBy(
                                    Enrolled::insuranceCompany,
                                    // transform inner list into a map
                                    // key = userID
                                    // value = enroll
                                    // merge => if there are duplicates with the same userID, keep the highest version number
                                    Collectors.toMap(
                                            Enrolled::userId,
                                            Function.identity(),
                                            (e1, e2) -> e1.version() >= e2.version() ? e1 : e2
                                    )
                            ));
        } catch (IOException e) {
            logger.severe("Error reading file: " + filePath + ": " + e.getMessage());
            throw new IOException("Error reading file: " + filePath, e);
        }
    }
//        // Key: Insurance company name
//        // Value: Another map where the key is the user ID and the value is the Enrollee object.
//        Map<String, Map<String, Enrolled>> companyMap = new HashMap<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//            System.out.println("Reading file: " + filePath);
//            br.readLine();
//            String line;
//
//            while ((line = br.readLine()) != null) {
//                String[] values = line.split(",");
//                if (values.length < 4) continue;
//
//                Enrolled enrolled = getEnrollee(values);
//                String company = enrolled.insuranceCompany();
//                String userId = enrolled.userId();
//
//                // Get the map for this company
//                Map<String, Enrolled> enrolleesByCompany =
//                        companyMap.computeIfAbsent(company, k -> new HashMap<>());
//
//                // Check if an existing enrollee with the same userId exists
//                Enrolled existing = enrolleesByCompany.get(userId);
//
//                // Keep the enrollee with the highest version number
//                if (existing == null || enrolled.version() > existing.version()) {
//                    enrolleesByCompany.put(userId, enrolled);
//                }
//            }
//        } catch (IOException e) {
//            logger.severe("Error reading file: " + filePath + ": " + e.getMessage());
//            throw new IOException("Error reading file: " + filePath, e);
//        }
//
//        return companyMap;


    /**
     * Parses a line's values into an {@link Enrolled} object.
     * <p>
     * Splits the full name into first and last names if applicable.
     * If only one name is provided, it is stored as the first name.
     *
     * @param values an array containing CSV fields in the order:
     *               [UserId, FullName, Version, InsuranceCompany]
     * @return a constructed {@link Enrolled} object.
     */
    private static Enrolled getEnrollee(String[] values) {
        try {
            String userId = values[0].trim();
            String fullName = values[1].trim();
            // remove leading/trailing whitespace
            // collapse all internal whitespace
            fullName = fullName.trim().replaceAll("\\s+", " ");
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

            return new Enrolled(userId, firstName, lastName, version, insuranceCompany);
        } catch (NumberFormatException e) {
            logger.warning(() -> "Skipping row due to invalid version number: " + String.join(",", values) +
                    " | Error: " + e.getMessage());
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
          logger.warning(() -> "Skipping row due to invalid number of fields: " + String.join(",", values) +
                    " | Error: " + e.getMessage());
        } catch (Exception e) {
            logger.severe(() -> "Unexpected error parsing row: " + String.join(",", values) +
                    " | Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        return null;
    }
}
