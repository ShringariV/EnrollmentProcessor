package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.NoSuchFileException;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Enrollment File Processor");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Enter path to CSV file: ");
            String path = br.readLine();

            // Read + group (dedupe by highest version)
            Map<String, Map<String, Enrolled>> grouped = CSVReader.readUniqueEnrollees(path);
            System.out.println("Successfully read and grouped enrollees");

            // Sort
            Map<String, Map<String, Enrolled>> sorted = Sorting.sortByName(grouped);
            System.out.println("Successfully sorted enrollees");

            // Write
            CSVWriter.writeByCompany(sorted);
            System.out.println("Successfully wrote sorted CSV files");

            // Summary
            System.out.println("Enrollees by Insurance Company:");
            sorted.keySet().forEach(System.out::println);

        } catch (IOException e) {
            // Walk through causes to see if it's a missing file
            Throwable cause = e;
            boolean missingFile = false;

            while (cause != null) {
                if (cause instanceof java.nio.file.NoSuchFileException) {
                    missingFile = true;
                    break;
                }
                cause = cause.getCause();
            }

            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String msg = root.getMessage() != null ? root.getMessage() : e.toString();

            if (missingFile && !msg.toLowerCase().contains("no such file")) {
                msg = msg + " (no such file or directory)";
            }

            System.err.println("error: " + msg.toLowerCase());
        }
    }
}
