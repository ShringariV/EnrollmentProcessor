package org.example;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class UtilityClassTest {

    private void assertUtilityClassNonInstantiable(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException thrown =
                assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertEquals("Utility class", thrown.getCause().getMessage());
    }

    @Test
    public void testCSVWriterCannotBeInstantiated() throws Exception {
        assertUtilityClassNonInstantiable(CSVWriter.class);
    }

    @Test
    public void testCSVReaderCannotBeInstantiated() throws Exception {
        assertUtilityClassNonInstantiable(CSVReader.class);
    }

    @Test
    public void testSortingCannotBeInstantiated() throws Exception {
        assertUtilityClassNonInstantiable(Sorting.class);
    }
}
