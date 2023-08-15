package com.azure.json;
import com.azure.json.InvalidJsonDataTypeException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class acts purely as a playground for testing and playing around with
 * the current implementation of the JSON model related classes:
 *      - JsonElement
 *      - JsonArray
 *      - JsonBoolean
 *      - JsonNull
 *      - JsonNumber
 *      - JsonObject
 *      - JsonString
 * NOTE: can be removed later.
 */
public class MainClass {
    // Utility static fields
    public static List<String> firstNames = new ArrayList<>();
    public static List<String> lastNames = new ArrayList<>();
    public static List<String> countries = new ArrayList<>();
    public static final Random randomiser = new Random();

    // Initialising the static members/fields
    static {
        // Initialising firstNames List
        String[] firstNamesExamples = { "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda", "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica" };
        Collections.addAll(firstNames, firstNamesExamples);
        // System.out.println(firstNames);

        // Initialising lastNames List
        String[] lastNamesExamples = { "Anderson", "Jones", "Campbell", "Taylor", "Williams", "Miller", "Johnson", "Wilson", "Simpson", "Harris", "Evans", "Skipper", "McNeill", "Murphy", "Robinson", "Allen", "Knight" };
        Collections.addAll(lastNames, lastNamesExamples);
        // System.out.println(lastNames);

        // Initialising countries List
        String[] countryExamples = { "New Zealand", "Australia", "United States", "England", "Ireland", "South Africa", "Canada" };
        Collections.addAll(countries, countryExamples);
        // System.out.println(countries);
    }

    public static void main(String[] args) throws InvalidJsonDataTypeException {
        example_1_json_object();
        example_2_json_object();
        example_3_json_object();
        example_4_simple_json_array();
        example_5_chaining_json_array();
    }

    // Showcasing the JsonObject methods in isolation (not chained)
    public static void example_1_json_object() {
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("EXAMPLE 1 - Showcasing the various JsonObject methods in work in isolation:");
        System.out.println("---------------------------------------------------------------------------");

        JsonObject jsonObj = new JsonObject();

        // Adding properties - showcasing addProperty.
        jsonObj.addProperty("James", "Anderson")
            .addProperty("Michael", "Campbell")
            .addProperty("Mary", "Jones")
            .addProperty("John", "Williams");
        System.out.println("Printing the constructed JsonObject:");
        System.out.println(jsonObj);
        System.out.println();

        // Showcasing getProperty(key)
        System.out.println("Getting the value (the last name) of Mary:");
        System.out.println(jsonObj.getProperty("Mary"));
        System.out.println();

        // Showcasing removeProperty and that order is preserved
        System.out.println("Removing Michael from the JsonObject:");
        jsonObj.removeProperty("Michael");
        System.out.println(jsonObj);
        System.out.println();
    }

    // Example of chaining JsonObject methods
    public static void example_2_json_object() {
        System.out.println("-------------------------------------------------------------");
        System.out.println("EXAMPLE 2 - Showcasing the chaining of JsonObject methods:");
        System.out.println("-----------------------------------------------------------");

        JsonObject jsonObj = new JsonObject();

        jsonObj.addProperty("James", "Anderson")
            .addProperty("Michael", "Campbell")
            .addProperty("Mary", "Jones")
            .removeProperty("Michael")
            .addProperty("John", "Williams")
            .removeProperty("James");
        System.out.println("Printing the constructed JsonObject after additions and removals:");
        System.out.println(jsonObj); // Michael and James are not in the resulting JsonObject
        System.out.println();
    }

    // Example of nesting other JsonObjects
    public static void example_3_json_object() {
        System.out.println("--------------------------------------------------");
        System.out.println("EXAMPLE 3 - Showcasing nesting of JsonObjects:");
        System.out.println("--------------------------------------------------");

        JsonObject jsonObj = new JsonObject();

        jsonObj.addProperty(
            "James",
            new JsonObject().addProperty("Country", "New Zealand").addProperty("Surname", "Anderson")
        ).addProperty(
            "Michael",
            new JsonObject().addProperty("Country", "Australia").addProperty("Surname", "Campbell")
        ).addProperty(
            "Mary",
            new JsonObject().addProperty("Country", "Canada").addProperty("Surname", "Jones")
        ).addProperty(
            "John",
            new JsonObject().addProperty("Country", "Australia").addProperty("Surname", "Williams")
        );
        System.out.println("Printing the constructed JsonObject:");
        System.out.println(jsonObj);
        System.out.println();
    }

    // Example of simple JsonArray - a JsonArray of countries
    public static void example_4_simple_json_array() {
        System.out.println("------------------------------------------------------------------");
        System.out.println("EXAMPLE 4 - Showcasing simple JsonArray - JsonArray of Countries:");
        System.out.println("------------------------------------------------------------------");

        JsonArray countryArray = new JsonArray();

        int elements = 10;
        for(int i = 0; i < elements; i++) {
            countryArray.addElement(
                new JsonString(countries.get(randomiser.nextInt(countries.size())))
            );
        }

        System.out.println("JsonArray constructed - a JsonArray of randomly selected countries:");
        System.out.println(countryArray);
        System.out.println();
    }

    // Example of chaining JsonArray methods to add and remove elements
    public static void example_5_chaining_json_array() {
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("EXAMPLE 5 - Showcasing chaining of JsonArray methods - additions and removals");
        System.out.println("-------------------------------------------------------------------------------");

        JsonArray countryArray = new JsonArray();

        // JsonArray after additions
        countryArray.addElement(new JsonString("New Zealand"))
            .addElement(new JsonString("Australia"))
            .addElement(new JsonString("Canada"))
            .removeElement(1)
            .addElement(new JsonString("United States"))
            .removeElement(2);

        System.out.println("Removed index 1 (Australia) then removed index 2 (which became United States). So left with New Zealand and Canada:");
        System.out.println(countryArray);
        System.out.println();
    }
}
