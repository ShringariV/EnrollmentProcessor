package org.example;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final InputStream originalIn = System.in;

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    private Path tempCsv;

    @BeforeEach
    public void setUp() throws IOException {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        // Create a temporary CSV file for testing
        tempCsv = Files.createTempFile("test_enrollment", ".csv");
        String csvData = """
                User Id,Full Name,Version,Insurance Company
                1,John Doe,3,Acme Insurance
                2,Jane Smith,2,Zenith Health
                3,Bob Johnson,1,Acme Insurance
                """;
        Files.writeString(tempCsv, csvData);
    }

    @AfterEach
    public void tearDown() throws IOException {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);
        Files.deleteIfExists(tempCsv);
    }

    @Test
    public void testMainRunsSuccessfully() {
        // Simulate user entering the path and pressing Enter
        String simulatedInput = tempCsv.toString() + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // Execute main
        Main.main(new String[]{});

        String output = outContent.toString();

        // ✅ Check expected messages
        assertTrue(output.contains("Enrollment File Processor"));
        assertTrue(output.contains("Successfully read and grouped enrollees"));
        assertTrue(output.contains("Successfully sorted enrollees"));
        assertTrue(output.contains("Successfully wrote sorted CSV files"));
        assertTrue(output.contains("Enrollees by Insurance Company"));

        // Verify companies printed
        assertTrue(output.contains("Acme Insurance"));
        assertTrue(output.contains("Zenith Health"));

        // No unexpected error messages
        assertTrue(errContent.toString().isEmpty());
    }

    @Test
    public void testMainHandlesInvalidFileGracefully() {
        String simulatedInput = "non_existent.csv" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        Main.main(new String[]{});

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error during processing"));
    }
}
