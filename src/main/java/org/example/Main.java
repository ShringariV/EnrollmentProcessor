package org.example;


import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String filePath = "src/main/resources/testingReading.csv";
        System.out.println("=== Reading Enrollees ===");
        Map<String, Map<String, Enrollee>> companyMap = CSVReader.readEnrollees(filePath);
        System.out.println("=== Enrollees by Insurance Company ===");
        companyMap.forEach((company, enrolleeMap) -> {
            System.out.println("\n" + company + ":");
            System.out.println("---------------------------------------------");

            enrolleeMap.forEach((userId, enrollee) -> {
                System.out.printf("  %-10s | %-10s %-10s | v%-2d%n",
                        enrollee.getUserId(),
                        enrollee.getFirstName(),
                        enrollee.getLastName(),
                        enrollee.getVersion());
            });
        });
    }
}