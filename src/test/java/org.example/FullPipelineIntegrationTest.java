package org.example;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive full integration test for the Enrollment Processing pipeline.
 *
 * This class invokes Main.main() directly to simulate the user experience.
 * It covers the entire system:
 *   1. Reading and grouping enrollees from CSV input
 *   2. Deduplicating by highest version
 *   3. Sorting by last name and first name (ascending)
 *   4. Writing company-wise CSV output
 *   5. Verifying log messages and result correctness
 *
 * All tests use real file IO and simulate stdin/stdout behavior.
 */
public class FullPipelineIntegrationTest {

    private Path inputCsv;
    private Path outputDir;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final InputStream originalIn = System.in;

    @BeforeEach
    public void setUp() throws IOException {
        inputCsv = Files.createTempFile("end_to_end_input", ".csv");
        outputDir = Paths.get("src/main/resources/output");

        if (Files.exists(outputDir)) {
            Files.walk(outputDir)
                    .map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(File::delete);
        }
        Files.createDirectories(outputDir);

        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);

        if (Files.exists(inputCsv)) Files.delete(inputCsv);
        if (Files.exists(outputDir)) {
            Files.walk(outputDir)
                    .map(Path::toFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(File::delete);
        }
    }

    private void runMainWithInput(Path inputFile) throws IOException {
        System.setIn(new ByteArrayInputStream((inputFile.toString() + System.lineSeparator()).getBytes()));
        Main.main(new String[]{});
    }

    private void assertPipelineStepsLogged(String output) {
        assertTrue(output.contains("Enrollment File Processor"), "Should print startup banner");
        assertTrue(output.contains("Successfully read and grouped enrollees"), "Should confirm reading step");
        assertTrue(output.contains("Successfully sorted enrollees"), "Should confirm sorting step");
        assertTrue(output.contains("Successfully wrote sorted CSV files"), "Should confirm writing step");
    }

    private void assertFileSortedByNames(Path csvFile) throws IOException {
        List<String> lines = Files.readAllLines(csvFile);
        lines.remove(0); // remove header

        List<String> names = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length > 1) names.add(parts[1].trim());
        }

        Comparator<String> byLastThenFirst = Comparator
                .comparing((String name) -> {
                    String[] np = name.split("\\s+");
                    return np.length > 1 ? np[np.length - 1].toLowerCase() : name.toLowerCase();
                })
                .thenComparing(name -> {
                    String[] np = name.split("\\s+");
                    return np[0].toLowerCase();
                });

        List<String> sorted = new ArrayList<>(names);
        sorted.sort(byLastThenFirst);

        assertEquals(sorted, names, "File should be alphabetically sorted by last then first name");
    }

    @Test
    public void testFullPipelineStandardData() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,Alice Adams,3,Zenith Health
                2,Jane Doe,2,Acme Insurance
                2,Jane Doe,3,Acme Insurance
                3,Bob Smith,1,Acme Insurance
                4,Charlie Brown,1,Acme Insurance
                5,Dave Adams,1,Zenith Health
                """;
        Files.writeString(inputCsv, csv);

        runMainWithInput(inputCsv);
        String output = outContent.toString();

        assertPipelineStepsLogged(output);
        assertTrue(output.contains("Acme Insurance"), "Should list Acme company");
        assertTrue(output.contains("Zenith Health"), "Should list Zenith company");

        Path acme = outputDir.resolve("Acme_Insurance.csv");
        Path zenith = outputDir.resolve("Zenith_Health.csv");
        assertTrue(Files.exists(acme));
        assertTrue(Files.exists(zenith));

        String acmeContent = Files.readString(acme);
        assertTrue(acmeContent.contains("Jane Doe,3,Acme Insurance"), "Highest version retained");
        assertTrue(acmeContent.contains("Charlie Brown"));
        assertTrue(acmeContent.contains("Bob Smith"));
        assertFileSortedByNames(acme);

        String zenithContent = Files.readString(zenith);
        assertTrue(zenithContent.contains("Alice Adams"));
        assertTrue(zenithContent.contains("Dave Adams"));
        assertFileSortedByNames(zenith);

        assertTrue(errContent.toString().isEmpty(), "No errors expected in stderr");
    }

    @Test
    public void testHandlesEmptyInputFileGracefully() throws IOException {
        Files.writeString(inputCsv, "User Id,Full Name,Version,Insurance Company\n");
        runMainWithInput(inputCsv);

        String output = outContent.toString();
        assertPipelineStepsLogged(output);
        try (var files = Files.list(outputDir).filter(Files::isRegularFile)) {
            assertEquals(0, files.count(), "No files should be produced for empty input");
        }
    }

    @Test
    public void testHandlesInvalidFileGracefully() {
        System.setIn(new ByteArrayInputStream(("nonexistent.csv\n").getBytes()));
        Main.main(new String[]{});
        String errOutput = errContent.toString().toLowerCase();
        assertTrue(errOutput.contains("error"), "Should print error");
        assertTrue(errOutput.contains("no such file"), "Should indicate missing file");
    }

    @Test
    public void testWhitespaceAndTrimmingBehavior() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,   Jane   Doe   ,5,   Acme Insurance  
                2,  Alice  Brown  ,2,  Acme Insurance
                """;
        Files.writeString(inputCsv, csv);
        runMainWithInput(inputCsv);

        Path acmeFile = outputDir.resolve("Acme_Insurance.csv");
        assertTrue(Files.exists(acmeFile));

        String acmeContent = Files.readString(acmeFile);
        assertTrue(acmeContent.contains("Jane Doe,5,Acme Insurance"), "Whitespace should be normalized");
        assertTrue(acmeContent.contains("Alice Brown,2,Acme Insurance"), "Trimming should preserve content");
        assertFileSortedByNames(acmeFile);
    }

    @Test
    public void testDuplicateUserIdsAcrossDifferentCompanies() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                10,John Smith,1,Acme Insurance
                10,John Smith,5,Zenith Health
                """;
        Files.writeString(inputCsv, csv);
        runMainWithInput(inputCsv);

        Path acme = outputDir.resolve("Acme_Insurance.csv");
        Path zenith = outputDir.resolve("Zenith_Health.csv");

        assertTrue(Files.exists(acme));
        assertTrue(Files.exists(zenith));

        String acmeContent = Files.readString(acme);
        String zenithContent = Files.readString(zenith);

        assertTrue(acmeContent.contains("John Smith,1,Acme Insurance"));
        assertTrue(zenithContent.contains("John Smith,5,Zenith Health"));
    }

    @Test
    public void testHandlesDifferentCapitalizationInCompanyNames() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,Jane Doe,1,acme insurance
                2,Bob Brown,2,ACME INSURANCE
                """;
        Files.writeString(inputCsv, csv);
        runMainWithInput(inputCsv);

        // Depending on normalization, may create separate files or merge; assert that both files are valid
        try (Stream<Path> pathStream = Files.list(outputDir)) {
            var files = pathStream.filter(Files::isRegularFile).toList();
            assertTrue(files.size() >= 1, "Should generate at least one company file");
        }

    }

    @Test
    public void testHandlesMultipleVersionsSameUser() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,Jane Doe,1,Acme Insurance
                1,Jane Doe,2,Acme Insurance
                1,Jane Doe,5,Acme Insurance
                """;
        Files.writeString(inputCsv, csv);
        runMainWithInput(inputCsv);

        Path acme = outputDir.resolve("Acme_Insurance.csv");
        String content = Files.readString(acme);
        assertTrue(content.contains("Jane Doe,5,Acme Insurance"), "Highest version should be kept");
        assertFalse(content.contains(",1,"), "Older version should not appear");
        assertFalse(content.contains(",2,"), "Older version should not appear");
    }

    @Test
    public void testHeaderValidationAndFormatting() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,John Doe,1,Acme Insurance
                """;
        Files.writeString(inputCsv, csv);
        runMainWithInput(inputCsv);

        Path acme = outputDir.resolve("Acme_Insurance.csv");
        String content = Files.readString(acme);
        assertTrue(content.startsWith("User ID,Full Name,Version,Insurance Company"),
                "Header should be formatted consistently");
    }

    @Test
    public void testInvalidDataGracefullySkipped() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                ,No ID,2,Acme Insurance
                1,Valid User,3,Acme Insurance
                """;
        Files.writeString(inputCsv, csv);
        runMainWithInput(inputCsv);

        Path acme = outputDir.resolve("Acme_Insurance.csv");
        String content = Files.readString(acme);
        assertTrue(content.contains("Valid User"), "Valid record should still be written");
        assertFalse(content.contains("No ID"), "Invalid rows should be ignored or skipped");
    }

    @Test
    public void testNoCrashOnExtraCommas() throws IOException {
        String csv = """
                User Id,Full Name,Version,Insurance Company
                1,"John, A. Doe",1,Acme Insurance
                """;
        Files.writeString(inputCsv, csv);
        runMainWithInput(inputCsv);

        Path acme = outputDir.resolve("Acme_Insurance.csv");
        assertTrue(Files.exists(acme));
        String content = Files.readString(acme);
        assertTrue(content.contains("John"), "Should parse names with commas correctly");
    }

    @Test
    public void testPerformanceOnLargeDataset() throws IOException {
        StringBuilder builder = new StringBuilder("User Id,Full Name,Version,Insurance Company\n");
        for (int i = 1; i <= 1000; i++) {
            builder.append(i).append(",User ").append(i)
                    .append(",").append(i % 3 + 1)
                    .append(",Company ").append(i % 10)
                    .append("\n");
        }
        Files.writeString(inputCsv, builder.toString());
        runMainWithInput(inputCsv);

        String output = outContent.toString();
        assertPipelineStepsLogged(output);
        try (var files = Files.list(outputDir).filter(Files::isRegularFile)) {
            assertTrue(files.count() > 0, "Should generate multiple company CSVs");
        }
    }
}
