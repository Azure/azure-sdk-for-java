package com.azure.json;
import java.io.IOException;
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
public class JsonAPIExamples {
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

    public static void main(String[] args) throws IOException {
        example_1_json_object();
        example_2_json_object();
        example_3_json_object();
        example_4_simple_json_array();
        example_5_chaining_json_array();


        // Original (old) samples.
        // These are deprecated due to no longer working due to our current
        // implementation. For example, passig a boolean array to JsonObject.addProperty,
        // no longer works - you can only add JsonElement types.
        /*
        int[] numArray = new int[10];
        numArray[0] = 3;
        numArray[5] = 27;
        numArray[2] = 1;
        numArray[3] = 5;

        String contest = "a";
        String con = String.valueOf(contest);
        System.out.println("Value is " + con);

        boolean[] boolArray1 = new boolean[]{true, false};

        JsonObject tester = new JsonObject().addProperty("David", boolArray1);
        System.out.println(tester);
        System.out.println(tester.getProperty("David").getClass().getName());

        String arrayTest = new JsonArray()
                .addElement(new JsonObject().addProperty("Value1", "One").addProperty("Value2", 2))
                .addElement(new JsonObject().addProperty("Value3", true).addProperty("Value4", null))
                .toJson();
        System.out.println(arrayTest);
        */
        /*

        JsonObject meme2 = new JsonObject();
        meme2.addProperty("DavidTester", "fiveGuys");
        meme2.addProperty("lkoveTester", "fasdsafdsafys");
        String output2 = meme2.toJson();
        System.out.println(output2);
        */
    } // End of main method

    // Showcasing the JsonObject methods in isolation (not chained)
    public static void example_1_json_object() throws IOException {
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("EXAMPLE 1 - Showcasing the various JsonObject methods in work in isolation:");
        System.out.println("---------------------------------------------------------------------------");

        JsonObject jsonObj = new JsonObject();

        // Adding properties - showcasing addProperty.
        jsonObj.setProperty("James", "Anderson")
            .setProperty("Michael", "Campbell")
            .setProperty("Mary", "Jones")
            .setProperty("John", "Williams");
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
    public static void example_2_json_object() throws IOException {
        System.out.println("-------------------------------------------------------------");
        System.out.println("EXAMPLE 2 - Showcasing the chaining of JsonObject methods:");
        System.out.println("-----------------------------------------------------------");

        JsonObject jsonObj = new JsonObject();

        jsonObj.setProperty("James", "Anderson")
            .setProperty("Michael", "Campbell")
            .setProperty("Mary", "Jones")
            .removeProperty("Michael")
            .setProperty("John", "Williams")
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

        jsonObj.setProperty(
            "James",
            new JsonObject().setProperty("Country", "New Zealand").setProperty("Surname", "Anderson")
        ).setProperty(
            "Michael",
            new JsonObject().setProperty("Country", "Australia").setProperty("Surname", "Campbell")
        ).setProperty(
            "Mary",
            new JsonObject().setProperty("Country", "Canada").setProperty("Surname", "Jones")
        ).setProperty(
            "John",
            new JsonObject().setProperty("Country", "Australia").setProperty("Surname", "Williams")
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

        public static void example_1() throws IOException {
        JsonObject jsonObj = new JsonObject();

        jsonObj.setProperty("James", "Anderson")
            .setProperty("Michael", "Campbell")
            .setProperty("a", "b");

        System.out.println(jsonObj);

        jsonObj.removeProperty("Michael");
        System.out.println(jsonObj);
    }

    public static void example_1_simple_json_object() {
        JsonObject jsonObject = new JsonObject();

        int properties = 10;
        for(int i = 0; i < properties; i++) {
            jsonObject.setProperty(
                firstNames.get(randomiser.nextInt(firstNames.size())),
                countries.get(randomiser.nextInt(countries.size()))
            );
        }

        System.out.println("EXAMPLE 1 - SIMPLE JSON OBJECT");
        System.out.println("Object:");
        System.out.println(jsonObject);
        System.out.println("Getting properties:");
        System.out.println(jsonObject.getProperty(firstNames.get(randomiser.nextInt(firstNames.size()))));
    }

    public static void example_2_simple_json_array() {
        JsonArray countryArray = new JsonArray();

        int elements = 10;
        for(int i = 0; i < elements; i++) {
            countryArray.addElement(
                new JsonString(countries.get(randomiser.nextInt(countries.size())))
            );
        }

        System.out.println(countryArray);
    }

    public static void example_3_chaining_json_object() {
        JsonObject jsonObject = new JsonObject();

        // Showcasing chaining of JsonObject method calls
        jsonObject.setProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        ).setProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        ).setProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        ).setProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        ).setProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        );

        System.out.println(jsonObject);
    }

    public static void example_4_chaining_json_array() {
        JsonArray countryArray = new JsonArray();

        // Showcasing chaining of JsonArray method calls
        countryArray.addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))))
            .addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))))
            .addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))))
            .addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))))
            .addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))))
            .addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))))
            .addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))))
            .addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))))
            .addElement(new JsonString(countries.get(randomiser.nextInt(countries.size()))));

        System.out.println(countryArray);
    }
}
