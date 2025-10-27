package org.example;

import org.slf4j.LoggerFactory;

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
     * insurance company is mapped to its corresponding enrollees.
     */
    public static Map<String, Map<String, Enrolled>> readUniqueEnrollees(String filePath) throws IOException {
        // open file as stream
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            return
                    // skip header
                    lines.skip(1)
                            .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
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

    /**
     * Parses a line's values into an {@link Enrolled} object.
     *
     * Splits the full name into first and last names if applicable.
     * If only one name is provided, it is stored as the first name.
     */
    private static Enrolled getEnrollee(String[] values) {
        try {
            String userId = values[0].trim();

            if (userId.isEmpty()) {
                logger.warning("Skipping row with missing User ID: " + String.join(",", values));
                return null;
            }

            String fullName = values[1].trim();

            fullName = fullName
                    .replaceAll("^\"|\"$", "")      // remove quotes at start/end
                    .replaceAll("\\s+", " ")        // collapse whitespace
                    .trim();

            int version = Integer.parseInt(values[2].trim());
            String insuranceCompany = values[3].trim().replaceAll("^\"|\"$", "");

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
