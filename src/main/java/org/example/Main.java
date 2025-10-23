package org.example;

import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\vedan\\Downloads\\EnrollmentProcessor\\src\\main\\resources\\testingReading.csv";
        List<Enrollee> enrollees = CSVReader.readEnrollees(filePath);
    }
}