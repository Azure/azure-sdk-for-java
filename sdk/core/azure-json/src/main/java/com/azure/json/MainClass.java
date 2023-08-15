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
        System.out.println(firstNames);

        // Initialising lastNames List
        String[] lastNamesExamples = { "Anderson", "Jones", "Campbell", "Taylor", "Williams", "Miller", "Johnson", "Wilson", "Simpson", "Harris", "Evans", "Skipper", "McNeill", "Murphy", "Robinson", "Allen", "Knight" };
        Collections.addAll(lastNames, lastNamesExamples);
        System.out.println(lastNames);

        // Initialising countries List
        String[] countryExamples = { "New Zealand", "Australia", "United States", "England", "Ireland", "South Africa", "Canada" };
        Collections.addAll(countries, countryExamples);
        System.out.println(countries);
    }

    public static void main(String[] args) throws InvalidJsonDataTypeException {
        example_1();

        // example_1_simple_json_object();
        // example_2_simple_json_array();
        // example_3_chaining_json_object();
        // example_4_chaining_json_array();

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

        // Testing that LinkedHashMap is working as expected
        JsonObject testingPropertyOrder = new JsonObject();
        for(int i = 0; i < 10; i++) {
            testingPropertyOrder.addProperty(
                "Property"+Integer.toString(i),
                "value"+Integer.toString(i)
            );
        }
        System.out.println(testingPropertyOrder);
*/

        /*
        JsonObject meme2 = new JsonObject();
        meme2.addProperty("DavidTester", "fiveGuys");
        meme2.addProperty("lkoveTester", "fasdsafdsafys");
        String output2 = meme2.toJson();
        System.out.println(output2);
        */
    }

    // String[] firstNamesExamples = { "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda", "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica" };
    // String[] lastNamesExamples = { "Anderson", "Jones", "Campbell", "Taylor", "Williams", "Miller", "Johnson", "Wilson", "Simpson", "Harris", "Evans", "Skipper", "McNeill", "Murphy", "Robinson", "Allen", "Knight" };
    // String[] countryExamples = { "New Zealand", "Australia", "United States", "England", "Ireland", "South Africa", "Canada" };

    public static void example_1() {
        JsonObject jsonObj = new JsonObject();

        jsonObj.addProperty("James", "Anderson")
            .addProperty("Michael", "Campbell")
            .addProperty("a", "b");

        System.out.println(jsonObj);

        jsonObj.removeProperty("Michael");
        System.out.println(jsonObj);
    }

    public static void example_1_simple_json_object() {
        JsonObject jsonObject = new JsonObject();

        int properties = 10;
        for(int i = 0; i < properties; i++) {
            jsonObject.addProperty(
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
        jsonObject.addProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        ).addProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        ).addProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        ).addProperty(
            firstNames.get(randomiser.nextInt(firstNames.size())),
            countries.get(randomiser.nextInt(countries.size()))
        ).addProperty(
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

    public static void example_5_get_propert_json_object() {

    }
}
