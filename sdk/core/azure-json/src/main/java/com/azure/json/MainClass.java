package com.azure.json;
import com.azure.json.InvalidJsonDataTypeException; 

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
    public static void main(String[] args) throws InvalidJsonDataTypeException {

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

        /*

        JsonObject meme2 = new JsonObject();
        meme2.addProperty("DavidTester", "fiveGuys");
        meme2.addProperty("lkoveTester", "fasdsafdsafys");
        String output2 = meme2.toJson();
        System.out.println(output2);
        */
    }
}
