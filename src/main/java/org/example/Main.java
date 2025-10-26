package org.example;


import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
/*
ArrayList — Dynamic Array

Time Complexity:
    Insertion        : O(1) amortized
    Removal          : O(n)
    Access (by index): O(1)
    Search           : O(n)

Space Complexity:
    O(n)
*/

/*
LinkedList — Doubly Linked List

Time Complexity:
    Insertion (head/tail) : O(1)
    Removal (head/tail)   : O(1)
    Access (by index)     : O(n)
    Search                : O(n)

Space Complexity:
    O(n)
*/

/*
HashMap<K, V> — Key-Value Mapping

Time Complexity:
    Insert  : O(1) average
    Lookup  : O(1) average
    Remove  : O(1) average
    Traverse: O(n)

Space Complexity:
    O(n)
*/

/*
HashSet / TreeSet — Unique Collections

Time Complexity:
    HashSet:
        Insert / Search / Remove : O(1) average
    TreeSet:
        Insert / Search / Remove : O(log n)

Space Complexity:
    O(n)
*/

/*
TreeMap<K, V> — Sorted Map (Red-Black Tree)

Time Complexity:
    Insert : O(log n)
    Lookup : O(log n)
    Remove : O(log n)

Space Complexity:
    O(n)
*/


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the path to the CSV file: ");
        String filePath = scanner.nextLine();

        try {
            System.out.println("\nEnrollment File Processor");

            Map<String, Map<String, Enrolled>> companyMap = CSVReader.readEnrollees(filePath);
            System.out.println("Successfully read and grouped enrollees by insurance company.");

            Map<String, Map<String, Enrolled>> sortedMap = Sorting.sortByName(companyMap);
            System.out.println("Successfully sorted enrollees (last name, first name).");

            CSVWriter.writeByCompany(sortedMap);
            System.out.println("Successfully wrote sorted CSV files to: src/main/resources/output");

            System.out.println("\nEnrollees by Insurance Company:");
            sortedMap.forEach((company, enrollees) -> {
                System.out.println("\n" + company);
                System.out.println("=".repeat(company.length()));
                enrollees.forEach((id, e) ->
                        System.out.printf("  %-10s | %-12s %-12s | v%-2d%n",
                                e.userId(), e.firstName(), e.lastName(), e.version()));
            });

        } catch (IOException e) {
            System.err.println("Error during processing: " + e.getMessage());
        }

        scanner.close();
    }
}