package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A utility class for reading data from a CSV file and transforming it into a list of Enrollee objects.
 */
public class CSVReader {
    public static List<Enrollee> readEnrollees(String filePath){
        List<Enrollee> enrollees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(values.length == 4){
                    Enrollee enrollee = getEnrollee(values);
                    System.out.println(enrollee.toString());
                    enrollees.add(enrollee);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return enrollees;
    }

    private static Enrollee getEnrollee(String[] values) {
        String userId = values[0].trim();
        String fullName = values[1].trim();
        Integer version = Integer.parseInt(values[2].trim());
        String insuranceCompany = values[3].trim();
        String firstName = "";
        String lastName = "";
        String[] nameParts = fullName.split(" ");
        if(nameParts.length == 2){
            firstName = nameParts[0];
            lastName = nameParts[1];
        }
        else if (nameParts.length == 1){
            firstName = nameParts[0];
        }
        Enrollee enrollee = new Enrollee(userId, firstName, lastName, version, insuranceCompany);
        return enrollee;
    }
}
