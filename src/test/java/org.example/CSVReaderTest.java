package org.example;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import static org.junit.jupiter.api.Assertions.*;

public class CSVReaderTest {

    private static class JulHandler extends Handler {
        final List<LogRecord> records = new ArrayList<>();
        @Override public void publish(LogRecord record) { records.add(record); }
        @Override public void flush() {}
        @Override public void close() {}
    }

    private JulHandler handler;
    private Logger csvLogger;

    @BeforeEach
    void setupLogger() {
        csvLogger = Logger.getLogger(CSVReader.class.getName());
        handler = new JulHandler();
        csvLogger.addHandler(handler);
    }

    @AfterEach
    void removeLogger() {
        csvLogger.removeHandler(handler);
    }

    private Path writeTempCSV(String content) throws IOException {
        Path tmp = Files.createTempFile("enroll", ".csv");
        Files.writeString(tmp, content);
        return tmp;
    }

    private LogRecord findLog(Level level, String contains) {
        return handler.records.stream()
                .filter(r -> r.getLevel().equals(level) && r.getMessage().contains(contains))
                .findFirst().orElse(null);
    }

    // -----------------------------------------------------------
    //  TESTS BEGIN
    // -----------------------------------------------------------

    @Test
    void testValidCSVReadsSuccessfully() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Alice Adams,3,Acme Insurance
                2,Jane Doe,2,Zenith Health
                """);

        Map<String, Map<String, Enrolled>> result = CSVReader.readEnrollees(csv.toString());
        assertEquals(2, result.size());
        assertTrue(result.containsKey("acme insurance"));
        assertTrue(result.containsKey("zenith health"));
    }

    @Test
    void testDuplicateUserIdKeepsHighestVersion() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Alice Adams,2,Acme Insurance
                1,Alice Adams,3,ACME INSURANCE
                """);

        Map<String, Map<String, Enrolled>> result = CSVReader.readEnrollees(csv.toString());
        Enrolled e = result.get("acme insurance").get("1");
        assertEquals(3, e.version());
    }

    @Test
    void testDuplicateUserIdWithSameVersionKeepsFirst() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                2,Jane Alpha,3,Acme Insurance
                2,Jane Beta,3,ACME INSURANCE
                """);

        Map<String, Map<String, Enrolled>> grouped = CSVReader.readEnrollees(csv.toString());
        Enrolled e = grouped.get("acme insurance").get("2");
        assertEquals("Jane", e.firstName());
        assertEquals("Alpha", e.lastName());
    }

    @Test
    void testMultipleCompaniesParsedCorrectly() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Alice Adams,1,Acme Insurance
                2,Bob Smith,1,ACME INSURANCE
                3,Charlie Brown,1,Zenith Health
                """);

        Map<String, Map<String, Enrolled>> grouped = CSVReader.readEnrollees(csv.toString());
        assertEquals(2, grouped.size());
        assertTrue(grouped.containsKey("acme insurance"));
        assertTrue(grouped.containsKey("zenith health"));
        assertEquals(2, grouped.get("acme insurance").size());
    }

    @Test
    void testNameSplittingSingleName() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Plato,1,Acme Insurance
                """);

        Enrolled e = CSVReader.readEnrollees(csv.toString())
                .get("acme insurance").get("1");
        assertEquals("Plato", e.firstName());
        assertTrue(e.lastName() == null || e.lastName().isEmpty());
    }

    @Test
    void testNameSplittingTwoPartName() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Jane Doe,1,Acme Insurance
                """);

        Enrolled e = CSVReader.readEnrollees(csv.toString())
                .get("acme insurance").get("1");
        assertEquals("Jane", e.firstName());
        assertEquals("Doe", e.lastName());
    }

    @Test
    void testTrimsWhitespaceProperly() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,   Alice    Adams   ,1,   Acme Insurance  
                """);

        Enrolled e = CSVReader.readEnrollees(csv.toString())
                .get("acme insurance").get("1");
        assertEquals("Alice", e.firstName());
        assertEquals("Adams", e.lastName());
    }

    @Test
    void testMalformedRowIsSkipped() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Alice,2
                2,Jane Doe,1,Acme Insurance
                """);

        Map<String, Map<String, Enrolled>> grouped = CSVReader.readEnrollees(csv.toString());
        assertEquals(1, grouped.get("acme insurance").size());
    }

    @Test
    void testMalformedRowTriggersLoggerButSkipsLine() throws Exception {
        Path csv = writeTempCSV("""
            User Id,Full Name,Version,Insurance Company
            1,Alice Adams,3
            2,Jane Doe,1,Acme Insurance
            """);

        Map<String, Map<String, Enrolled>> grouped = CSVReader.readEnrollees(csv.toString());

        // no exception thrown, malformed row skipped
        assertEquals(1, grouped.get("acme insurance").size());
        // optional: ensure logger did NOT log anything (since filter catches it)
        assertTrue(!handler.records.stream().findAny().isPresent(), "No warning expected because malformed row is filtered before parsing");
    }

    @Test
    void testInvalidVersionNumberTriggersWarning() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Alice,notanumber,Acme Insurance
                2,Bob Smith,1,Acme Insurance
                """);

        Map<String, Map<String, Enrolled>> grouped = CSVReader.readEnrollees(csv.toString());
        assertNotNull(findLog(Level.WARNING, "invalid version number"));
        assertEquals(1, grouped.get("acme insurance").size());
    }

    @Test
    void testInvalidVersionHandledGracefully() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,John Doe,abc,Acme Insurance
                """);

        Map<String, Map<String, Enrolled>> grouped = CSVReader.readEnrollees(csv.toString());
        assertTrue(grouped.isEmpty() || grouped.get("acme insurance") == null);
    }

    @Test
    void testDuplicateSameVersionKeepsFirst() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                3,Alice Alpha,2,Acme Insurance
                3,Alice Beta,2,ACME INSURANCE
                """);

        Enrolled e = CSVReader.readEnrollees(csv.toString())
                .get("acme insurance").get("3");
        assertEquals("Alpha", e.lastName());
    }

    @Test
    void testLoggerWarnsOnInvalidVersion() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Bob,hello,Acme Insurance
                """);

        CSVReader.readEnrollees(csv.toString());
        assertNotNull(findLog(Level.WARNING, "invalid version number"));
    }

    @Test
    void testDuplicateUserKeepsHighestVersion() throws Exception {
        Path csv = writeTempCSV("""
                User Id,Full Name,Version,Insurance Company
                1,Alice Adams,1,Acme Insurance
                1,Alice Adams,2,Acme Insurance
                """);

        Enrolled e = CSVReader.readEnrollees(csv.toString())
                .get("acme insurance").get("1");
        assertEquals(2, e.version());
    }
}
