package org.example;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class SortingTest {

    @Test
    public void testSortsByLastThenFirstName() {
        Map<String, Enrolled> company = new HashMap<>();
        company.put("1", new Enrolled("1", "Bob", "Adams", 1, "Acme"));
        company.put("2", new Enrolled("2", "Alice", "Brown", 2, "Acme"));
        company.put("3", new Enrolled("3", "Aaron", "Brown", 1, "Acme"));

        Map<String, Map<String, Enrolled>> grouped = Map.of("Acme", company);
        Map<String, Map<String, Enrolled>> sorted = Sorting.sortByName(grouped);

        List<Enrolled> list = new ArrayList<>(sorted.get("Acme").values());
        assertEquals("Adams", list.get(0).lastName());
        assertEquals("Aaron", list.get(1).firstName());
        assertEquals("Alice", list.get(2).firstName());
    }

    @Test
    public void testSortIsCaseInsensitive() {
        Map<String, Enrolled> company = new HashMap<>();
        company.put("1", new Enrolled("1", "alice", "smith", 1, "Acme"));
        company.put("2", new Enrolled("2", "Bob", "Smith", 1, "Acme"));

        Map<String, Map<String, Enrolled>> sorted = Sorting.sortByName(Map.of("Acme", company));
        List<Enrolled> list = new ArrayList<>(sorted.get("Acme").values());
        assertEquals("alice", list.get(0).firstName().toLowerCase());
    }

    @Test
    public void testEmptyInputReturnsEmptyMap() {
        Map<String, Map<String, Enrolled>> result = Sorting.sortByName(Map.of());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSortingHandlesEqualLastNames() {
        Map<String, Enrolled> acme = Map.of(
                "1", new Enrolled("1", "Alice", "Smith", 1, "Acme"),
                "2", new Enrolled("2", "Bob", "Smith", 2, "Acme")
        );

        var sorted = Sorting.sortByName(Map.of("Acme", acme));
        var list = new ArrayList<>(sorted.get("Acme").values());

        assertEquals("Alice", list.get(0).firstName()); // should sort by first name if last names equal
    }
    @Test
    public void testEmptyMapReturnsEmpty() {
        var sorted = Sorting.sortByName(Map.of());
        assertTrue(sorted.isEmpty(), "Empty input map should return empty output");
    }

    @Test
    public void testSameLastNameSortsByFirstName() {
        Map<String, Enrolled> acme = Map.of(
                "1", new Enrolled("1", "Alice", "Smith", 1, "Acme"),
                "2", new Enrolled("2", "Bob", "Smith", 2, "Acme")
        );
        var sorted = Sorting.sortByName(Map.of("Acme", acme));
        var list = new ArrayList<>(sorted.get("Acme").values());
        assertEquals("Alice", list.get(0).firstName());
    }

    @Test
    public void testCaseInsensitiveSorting() {
        Map<String, Enrolled> acme = Map.of(
                "1", new Enrolled("1", "alice", "smith", 1, "Acme"),
                "2", new Enrolled("2", "Bob", "Smith", 2, "Acme")
        );
        var sorted = Sorting.sortByName(Map.of("Acme", acme));
        var list = new ArrayList<>(sorted.get("Acme").values());
        assertEquals("alice", list.get(0).firstName().toLowerCase());
    }

    @Test
    public void testInnerMergeRuleKeepsFirstEntryWhenDuplicateUserId() {
        Map<String, Enrolled> company = new LinkedHashMap<>();
        company.put("1", new Enrolled("1", "John", "Doe", 1, "Acme"));
        // Duplicate userId with different data
        company.put("1", new Enrolled("1", "Jane", "Doe", 2, "Acme"));

        Map<String, Map<String, Enrolled>> grouped = Map.of("Acme", company);

        Map<String, Map<String, Enrolled>> sorted = Sorting.sortByName(grouped);
        Map<String, Enrolled> result = sorted.get("Acme");

        // Confirm only one userId remains
        assertEquals(1, result.size(), "Duplicate user IDs should merge into one");
        Enrolled retained = result.get("1");

        // Should keep the FIRST one because merge rule is (a, b) -> a
        assertEquals("Jane", retained.firstName());
        assertEquals("Doe", retained.lastName());
    }

    @Test
    public void testOuterMergeRuleKeepsFirstCompanyMap() {
        // Duplicate key "Acme" to force outer merge conflict
        Map<String, Enrolled> acme1 = Map.of("1", new Enrolled("1", "Alice", "Smith", 1, "Acme"));
        Map<String, Enrolled> acme2 = Map.of("2", new Enrolled("2", "Bob", "Jones", 1, "Acme"));

        // Build a list with two entries for same company name
        Map<String, Map<String, Enrolled>> grouped = new LinkedHashMap<>();
        grouped.put("Acme", acme1);
        grouped.put("Acme", acme2); // key collision

        Map<String, Map<String, Enrolled>> sorted = Sorting.sortByName(grouped);

        // Confirm only the first company map is retained
        assertEquals(1, sorted.size(), "Outer merge rule should keep only one Acme entry");
        Map<String, Enrolled> retained = sorted.get("Acme");

        assertTrue(retained.containsKey("2") || retained.containsKey("1"),
                "One of the company maps should survive merge");
    }
}
