package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that writes enrollee data to separate CSV files per insurance company.
 * <p>
 * Output directory: src/main/resources/output
 * CSV format:
 * User ID, Full Name, Version, Insurance Company
 */
public class CSVWriter {

    private static final Logger logger = Logger.getLogger(CSVWriter.class.getName());

    // Prevent instantiation of this utility class
    private CSVWriter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Writes each insurance company's enrollees to a separate CSV file.
     * Creates the output directory if it does not exist.
     *
     * @param grouped Map of company → (userId → enrollee)
     * @throws IOException if directory creation fails
     */
    public static void writeByCompany(Map<String, Map<String, Enrolled>> grouped) throws IOException {
        Path outputDirectory = Paths.get("src/main/resources/output");

        // Create output directory if missing
        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
            logger.info("Created output directory: " + outputDirectory.toAbsolutePath());
        }

        grouped.forEach((company, enrolleeMap) -> {
            String fileName = company
                    // sanitise invalid chars
                    // replace spaces with _
                    .replaceAll("[^a-zA-Z0-9\\-_ ]", "_")
                    .replaceAll(" +", "_") + ".csv";

            Path filePath = outputDirectory.resolve(fileName);

            try {
                
                Stream<String> lines = Stream.concat(
                        Stream.of("User ID,Full Name,Version,Insurance Company"), // header
                        enrolleeMap.values().stream()
                                .map(e -> String.join(",",
                                        e.userId(),
                                        e.firstName() + " " + e.lastName(),
                                        e.version().toString(),
                                        e.insuranceCompany()
                                ))
                );

                // Write to file
                Files.writeString(
                        filePath,
                        lines.collect(Collectors.joining(System.lineSeparator())),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

                logger.info("Wrote file: " + filePath.toAbsolutePath());

            } catch (IOException e) {
                logger.severe("Failed to write file for company: " + company + " - " + e.getMessage());
            }
        });
    }
}
