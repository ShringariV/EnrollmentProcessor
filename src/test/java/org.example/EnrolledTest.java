package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnrolledTest {
    @Test
    public void testRecordAccessors() {
        Enrolled e = new Enrolled("1", "John", "Doe", 3, "Acme");
        assertEquals("1", e.userId());
        assertEquals("John", e.firstName());
        assertEquals("Doe", e.lastName());
        assertEquals(3, e.version());
        assertEquals("Acme", e.insuranceCompany());
    }

    @Test
    public void testToStringContainsAllFields() {
        Enrolled e = new Enrolled("1", "John", "Doe", 3, "Acme");
        String s = e.toString();
        assertTrue(s.contains("John"));
        assertTrue(s.contains("Doe"));
        assertTrue(s.contains("Acme"));
    }

    @Test
    public void testEqualityAndHashCodeConsistency() {
        Enrolled e1 = new Enrolled("1", "John", "Doe", 3, "Acme");
        Enrolled e2 = new Enrolled("1", "John", "Doe", 3, "Acme");
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    public void testVersionComparisonIsDistinct() {
        Enrolled e1 = new Enrolled("1", "John", "Doe", 2, "Acme");
        Enrolled e2 = new Enrolled("1", "John", "Doe", 3, "Acme");
        assertNotEquals(e1, e2);
    }
}
