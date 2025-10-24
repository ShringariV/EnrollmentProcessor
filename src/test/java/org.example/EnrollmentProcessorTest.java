package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentProcessorTest {

    private Path tempCsv;

    @BeforeEach
    public void setUp() throws IOException {
        tempCsv = Files.createTempFile("enrollment_test", ".csv");
    }

    @Test
    public void testReadValidCSV() throws IOException {
        String csvData = """
                User Id,Full Name,Version,Insurance Company
                1,John Doe,3,Acme Insurance
                2,Jane Smith,1,Zenith Health
                3,Bob Johnson,2,Acme Insurance
                """;
        Files.writeString(tempCsv, csvData);

        Map<String, Map<String, Enrolled>> companyMap = CSVReader.readEnrollees(tempCsv.toString());

        assertEquals(2, companyMap.size(), "Should contain 2 insurance companies");
        assertTrue(companyMap.containsKey("Acme Insurance"));
        assertTrue(companyMap.containsKey("Zenith Health"));

        Enrolled john = companyMap.get("Acme Insurance").get("1");
        assertNotNull(john);
        assertEquals("John", john.firstName());
        assertEquals("Doe", john.lastName());
        assertEquals(3, john.version());
    }

    @Test
    public void testIgnoreMalformedRows() throws IOException {
        String csvData = """
                User Id,Full Name,Version,Insurance Company
                1,John Doe,3,Acme Insurance
                invalid_line_without_commas
                2,Jane Smith,1,Zenith Health
                """;
        Files.writeString(tempCsv, csvData);

        Map<String, Map<String, Enrolled>> companyMap = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(2, companyMap.size(), "Should still have valid entries despite malformed line");
    }

    @Test
    public void testDuplicateUserIdKeepsHighestVersion() throws IOException {
        String csvData = """
                User Id,Full Name,Version,Insurance Company
                1,John Doe,2,Acme Insurance
                1,John Doe,5,Acme Insurance
                1,John Doe,3,Acme Insurance
                """;
        Files.writeString(tempCsv, csvData);

        Map<String, Map<String, Enrolled>> companyMap = CSVReader.readEnrollees(tempCsv.toString());
        Enrolled john = companyMap.get("Acme Insurance").get("1");

        assertEquals(5, john.version(), "Should keep the highest version for duplicate User IDs");
    }

    @Test
    public void testSplitsFirstAndLastName() throws IOException {
        String csvData = """
                User Id,Full Name,Version,Insurance Company
                1,Alice Cooper,1,Global Health
                """;
        Files.writeString(tempCsv, csvData);

        Map<String, Map<String, Enrolled>> companyMap = CSVReader.readEnrollees(tempCsv.toString());
        Enrolled alice = companyMap.get("Global Health").get("1");

        assertEquals("Alice", alice.firstName());
        assertEquals("Cooper", alice.lastName());
    }

    @Test
    public void testHandlesEmptyFileGracefully() throws IOException {
        String csvData = "User Id,Full Name,Version,Insurance Company\n";
        Files.writeString(tempCsv, csvData);

        Map<String, Map<String, Enrolled>> companyMap = CSVReader.readEnrollees(tempCsv.toString());
        assertTrue(companyMap.isEmpty(), "Empty CSV (just header) should produce empty map");
    }

    @Test
    public void testDifferentCompaniesSeparatedProperly() throws IOException {
        String csvData = """
                User Id,Full Name,Version,Insurance Company
                1,John Doe,1,Acme Insurance
                2,Jane Smith,1,Zenith Health
                3,Tom Cruise,2,Acme Insurance
                """;
        Files.writeString(tempCsv, csvData);

        Map<String, Map<String, Enrolled>> companyMap = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(2, companyMap.size());
        assertEquals(2, companyMap.get("Acme Insurance").size());
        assertEquals(1, companyMap.get("Zenith Health").size());
    }

    @Test
    public void testTrimsWhitespaceProperly() throws IOException {
        String csvData = """
                User Id,Full Name,Version,Insurance Company
                1,   John   Doe   ,3,   Acme Insurance  
                """;
        Files.writeString(tempCsv, csvData);

        Map<String, Map<String, Enrolled>> companyMap = CSVReader.readEnrollees(tempCsv.toString());
        Enrolled john = companyMap.get("Acme Insurance").get("1");

        assertEquals("John", john.firstName());
        assertEquals("Doe", john.lastName());
    }

    @Test
    public void testThrowsWhenFileDoesNotExist() {
        String invalidPath = "nonexistent_file.csv";
        IOException exception = assertThrows(IOException.class, () -> {
            CSVReader.readEnrollees(invalidPath);
        });
        assertTrue(
                exception.getMessage().contains("nonexistent_file.csv")
                        || exception instanceof java.nio.file.NoSuchFileException,
                "Expected IOException indicating missing file"
        );
    }
}
