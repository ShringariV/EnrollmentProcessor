package org.example;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that validates the full end-to-end pipeline:
 *  CSVReader → Sorting → CSVWriter
 */
public class EnrollmentIntegrationTest {

    private Path inputCsv;
    private Path outputDir;

    @BeforeEach
    public void setUp() throws IOException {
        inputCsv = Files.createTempFile("integration_input", ".csv");
        outputDir = Paths.get("src/main/resources/output");

        // Clean up any old outputs before running
        if (Files.exists(outputDir)) {
            Files.walk(outputDir)
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    public void testEndToEndReadSortWrite() throws IOException {
        // Prepare synthetic input CSV
        String csvData = """
                User Id,Full Name,Version,Insurance Company
                3,Bob Smith,1,Acme Insurance
                2,Jane Doe,2,Acme Insurance
                1,Alice Adams,3,Zenith Health
                2,Jane Doe,3,Acme Insurance
                """;
        Files.writeString(inputCsv, csvData);

        Map<String, Map<String, Enrolled>> rawData = CSVReader.readEnrollees(inputCsv.toString());
        assertEquals(2, rawData.size(), "Should detect two insurance companies");

        Map<String, Map<String, Enrolled>> sortedData = Sorting.sortByName(rawData);

        List<Enrolled> acmeList = new ArrayList<>(sortedData.get("Acme Insurance").values());
        assertEquals("Doe", acmeList.get(0).lastName(), "Sorted by last name first");

        CSVWriter.writeByCompany(sortedData);

        Path acmeFile = outputDir.resolve("Acme_Insurance.csv");
        Path zenithFile = outputDir.resolve("Zenith_Health.csv");
        assertTrue(Files.exists(acmeFile), "Acme Insurance output file should exist");
        assertTrue(Files.exists(zenithFile), "Zenith Health output file should exist");

        String acmeContent = Files.readString(acmeFile);
        assertTrue(acmeContent.contains("User ID,Full Name,Version,Insurance Company"));
        assertTrue(acmeContent.contains("Jane Doe"));
        assertTrue(acmeContent.contains("Acme Insurance"));

        assertTrue(acmeContent.contains("3"), "Should retain record with highest version for Jane Doe");
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up temporary test files
        if (Files.exists(inputCsv)) Files.delete(inputCsv);
    }

    @Test
    public void testIntegrationWithEmptyFileProducesNoOutput() throws IOException {
        Files.writeString(inputCsv, "User Id,Full Name,Version,Insurance Company\n");
        Map<String, Map<String, Enrolled>> rawData = CSVReader.readEnrollees(inputCsv.toString());
        assertTrue(rawData.isEmpty());
    }

    @Test
    public void testIntegrationHandlesWhitespaceAndSorting() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,   Jane   Doe   ,2,   Acme Insurance  
            2,  Alice  Brown  ,1,  Acme Insurance
            """;
        Files.writeString(inputCsv, csv);
        var data = Sorting.sortByName(CSVReader.readEnrollees(inputCsv.toString()));
        var acmeList = new ArrayList<>(data.get("Acme Insurance").values());
        assertEquals("Brown", acmeList.get(0).lastName());
    }
}
