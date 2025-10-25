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

}
