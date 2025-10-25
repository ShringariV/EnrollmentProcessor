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
}
