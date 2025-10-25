package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CSVReaderTest {
    private Path tempCsv;

    @BeforeEach
    public void setUp() throws IOException {
        tempCsv = Files.createTempFile("csvreader_test", ".csv");
    }

    @Test
    public void testValidCSVReadsSuccessfully() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,John Doe,3,Acme Insurance
                2,Jane Smith,1,Zenith Health
                """;
        Files.writeString(tempCsv, csv);

        Map<String, Map<String, Enrolled>> result = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(2, result.size());
        assertTrue(result.containsKey("Acme Insurance"));
        assertEquals("John", result.get("Acme Insurance").get("1").firstName());
    }

    @Test
    public void testDuplicateUserKeepsHighestVersion() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,John Doe,1,Acme Insurance
                1,John Doe,5,Acme Insurance
                1,John Doe,3,Acme Insurance
                """;
        Files.writeString(tempCsv, csv);

        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(5, map.get("Acme Insurance").get("1").version());
    }

    @Test
    public void testTrimsWhitespaceProperly() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,   John   Doe   ,2,   Acme Insurance  
                """;
        Files.writeString(tempCsv, csv);

        var map = CSVReader.readEnrollees(tempCsv.toString());
        Enrolled john = map.get("Acme Insurance").get("1");
        assertEquals("John", john.firstName());
        assertEquals("Doe", john.lastName());
    }

    @Test
    public void testHandlesEmptyFileGracefully() throws IOException {
        Files.writeString(tempCsv, "User Id,Full Name,Version,Insurance Company\n");
        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testThrowsIOExceptionOnMissingFile() {
        assertThrows(IOException.class, () -> CSVReader.readEnrollees("missing.csv"));
    }
    @Test
    public void testMultipleCompaniesParsedCorrectly() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,1,Acme Insurance
            2,Jane Roe,2,Zenith Health
            3,Bob Joe,3,Acme Insurance
            """;
        Files.writeString(tempCsv, csv);
        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(2, map.size());
        assertEquals(2, map.get("Acme Insurance").size());
        assertTrue(map.get("Zenith Health").containsKey("2"));
    }

    @Test
    public void testMalformedLineIsIgnored() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,2,Acme Insurance
            bad,line,here
            2,Jane Smith,3,Zenith Health
            """;
        Files.writeString(tempCsv, csv);
        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(2, map.size());
    }

    @Test
    public void testInvalidVersionSkipped() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,notanumber,Acme Insurance
            """;
        Files.writeString(tempCsv, csv);
        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testMalformedRowTriggersLoggerButSkipsLine() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,abc,Acme Insurance
            bad,data,line
            2,Jane Smith,2,Zenith Health
            """;
        Files.writeString(tempCsv, csv);

        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Zenith Health"));
    }
    @Test
    public void testMissingFileThrowsIOException() {
        assertThrows(IOException.class, () -> CSVReader.readEnrollees("nonexistent.csv"));
    }

    @Test
    public void testInvalidVersionHandledGracefully() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,abc,Acme Insurance
            2,Jane Smith,2,Zenith Health
            """;
        Files.writeString(tempCsv, csv);
        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(1, map.size(), "Should skip invalid version and keep valid row");
        assertTrue(map.containsKey("Zenith Health"));
    }

    @Test
    public void testMalformedRowIsSkipped() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            bad,data,line
            1,John Doe,3,Acme Insurance
            """;
        Files.writeString(tempCsv, csv);
        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Acme Insurance"));
    }

    @Test
    public void testDuplicateSameVersionKeepsFirst() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,2,Acme Insurance
            1,John Doe,2,Acme Insurance
            """;
        Files.writeString(tempCsv, csv);
        var map = CSVReader.readEnrollees(tempCsv.toString());
        assertEquals(1, map.get("Acme Insurance").size());
        assertEquals("John", map.get("Acme Insurance").get("1").firstName());
    }

    @Test
    public void testInvalidVersionNumberTriggersWarning() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,abc,Acme Insurance
            2,Jane Smith,3,Zenith Health
            """;
        Files.writeString(tempCsv, csv);

        var map = CSVReader.readEnrollees(tempCsv.toString());
        // first record skipped, second valid → 1 company total
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Zenith Health"));
    }

    @Test
    public void testTooFewColumnsTriggersArrayIndexError() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,2
            2,Jane Smith,3,Zenith Health
            """;
        Files.writeString(tempCsv, csv);

        var map = CSVReader.readEnrollees(tempCsv.toString());
        // first line skipped, second valid
        assertEquals(1, map.size());
        assertTrue(map.containsKey("Zenith Health"));
    }

    @Test
    public void testUnexpectedExceptionBranchHandled() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,3,Acme Insurance
            """;
        Files.writeString(tempCsv, csv);

        // Inject a null line manually to simulate bad data stream
        var lines = Files.readAllLines(tempCsv);
        lines.add(1, null);
        Files.write(tempCsv, lines);

        assertDoesNotThrow(() -> CSVReader.readEnrollees(tempCsv.toString()),
                "Reader should skip null/invalid lines gracefully");
    }
    @Test
    public void testDuplicateUserIdKeepsHighestVersion() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,1,Acme Insurance
            1,John Doe,3,Acme Insurance
            1,John Doe,2,Acme Insurance
            """;
        Files.writeString(tempCsv, csv);

        var map = CSVReader.readEnrollees(tempCsv.toString());

        assertEquals(1, map.size());
        var enrollee = map.get("Acme Insurance").get("1");
        assertEquals(3, enrollee.version());
    }

    @Test
    public void testDuplicateUserIdWithSameVersionKeepsFirst() throws IOException {
        String csv = """
            User Id,Full Name,Version,Insurance Company
            1,John Doe,2,Acme Insurance
            1,John Doe,2,Acme Insurance
            """;
        Files.writeString(tempCsv, csv);

        var map = CSVReader.readEnrollees(tempCsv.toString());

        assertEquals(1, map.size());
        var enrollee = map.get("Acme Insurance").get("1");
        assertEquals(2, enrollee.version());
        assertEquals("John", enrollee.firstName());
    }

}
