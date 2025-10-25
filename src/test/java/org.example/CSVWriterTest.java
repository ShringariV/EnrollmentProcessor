package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CSVWriterTest {
    private Path outputDir;

    @BeforeEach
    void setup() throws IOException {
        outputDir = Paths.get("src/main/resources/output");
        if (Files.exists(outputDir))
            Files.walk(outputDir).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void testWriteByCompanyCreatesFiles() throws IOException {
        Map<String, Enrolled> acme = Map.of(
                "1", new Enrolled("1", "John", "Doe", 3, "Acme Insurance")
        );
        Map<String, Map<String, Enrolled>> grouped = Map.of("Acme Insurance", acme);

        CSVWriter.writeByCompany(grouped);

        Path outputFile = outputDir.resolve("Acme_Insurance.csv");
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("John Doe"));
        assertTrue(content.startsWith("User ID,Full Name"));
    }

    @Test
    public void testOutputDirectoryCreatedIfMissing() throws IOException {
        Path dir = Paths.get("src/main/resources/output");
        if (Files.exists(dir)) {
            Files.walk(dir).map(Path::toFile).forEach(File::delete);
        }
        CSVWriter.writeByCompany(Map.of());
        assertTrue(Files.exists(dir));
    }

    @Test
    public void testWritesMultipleCompanies() throws IOException {
        var acme = Map.of("1", new Enrolled("1", "John", "Doe", 3, "Acme Insurance"));
        var zenith = Map.of("2", new Enrolled("2", "Jane", "Smith", 2, "Zenith Health"));
        var grouped = Map.of("Acme Insurance", acme, "Zenith Health", zenith);

        CSVWriter.writeByCompany(grouped);

        assertTrue(Files.exists(outputDir.resolve("Acme_Insurance.csv")));
        assertTrue(Files.exists(outputDir.resolve("Zenith_Health.csv")));
    }

    @Test
    public void testEmptyDataProducesNoFiles() throws IOException {
        CSVWriter.writeByCompany(Map.of());
        try (var files = Files.list(outputDir).filter(Files::isRegularFile)) {
            assertEquals(0, files.count(), "No output CSV files should be created for empty input");
        }
    }
}
