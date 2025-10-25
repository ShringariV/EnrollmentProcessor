package org.example;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.logging.Logger;
public class Sorting {
    private static final Logger logger = Logger.getLogger(Sorting.class.getName());
    private Sorting() {
        throw new IllegalStateException("Utility class");
    }
    /**
     * Sorts each company's enrollees by last name, then first name (ascending).
     * Returns a new Map with sorted inner maps.
     *
     * @param grouped the map from CSVReader (company -> userId -> enrollee)
     * @return a map with sorted enrollees for each company
     */
    public static Map<String, Map<String, Enrolled>> sortByName(Map<String, Map<String, Enrolled>> grouped) {
        logger.info("Sorting enrollees by last and first name (ascending)");
        // convert outer map entries into a stream for transformation
        return grouped.entrySet().stream()
                .collect(Collectors.toMap(
                        // preserve insurance company name
                        Map.Entry::getKey,
                        // build a new sorted inner map for each company
                        entry -> entry.getValue().values().stream()
                                // sort by last then first name
                                .sorted(Comparator
                                        .comparing(Enrolled::lastName, Comparator.nullsLast(String::compareToIgnoreCase))
                                        .thenComparing(Enrolled::firstName, Comparator.nullsLast(String::compareToIgnoreCase))
                                )
                                // collect enrolles into a linked hash map
                                // key = user id; value = enrollee obj
                                .collect(Collectors.toMap(
                                        Enrolled::userId,
                                        Function.identity(),
                                        // merge rule just in case there are keys with the same user id
                                        // No need for deduplication since we already read it
                                        (a, b) -> a,
                                        LinkedHashMap::new
                                )),
                        // merge rule just in case there are keys with the same user id
                        // No need for deduplication since we already read it
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

}
