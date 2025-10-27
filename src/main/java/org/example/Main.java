package org.example;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Enrollment File Processor ===");
        try {
            // Read file path from stdin
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            String inputPath = scanner.nextLine().trim();

            Map<String, Map<String, Enrolled>> grouped = CSVReader.readEnrollees(inputPath);
            System.out.println("Successfully read and grouped enrollees");

            Map<String, Map<String, Enrolled>> sorted = Sorting.sortByName(grouped);
            System.out.println("Successfully sorted enrollees");

            CSVWriter.writeByCompany(sorted);
            System.out.println("Successfully wrote sorted CSV files");

        } catch (NoSuchFileException e) {
            System.err.println("Error: " + e.getMessage() + " (no such file)");
        } catch (IOException e) {
            // fallback for other IO errors
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
