package com.azure.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonConversionTests {

        /*
    General Rules for conversions:

    Converting Empty Entries:
    In all cases, converting an empty JsonElement to any type should result in a new instance of the specified type.

    Object and Array Conversions:
    Conversions can only happen if the Object/Array only contains one element that is not JsonObject or JsonArray.
    If this is the case, check type of contained JsonElement and apply applicable rule. If it contains multiple entries,
    or if a JsonObject or JsonArray is inside, conversion fails unless converting Object to Array and reverse, or if
    converting to JsonNull. Further explanations below:


    Converting to JsonNumber:
        JsonString: If value is a numeral (eg: "1"), parse the text as int or decimal and convert. Otherwise, fails.
        JsonNull: Fails
        JsonBoolean: If true, converts to 0. If false converts to 1.

    Converting to JsonString:
        JsonNumber, JsonBoolean and JsonNull: simply get value from toString and set that as the value.

    Converting to JsonBoolean:
        JsonNumber: If 1, value is true, if 0, value is false. If any other value, fails.
        JsonString: If text is "1" or "true", value is true. If text is "0" or "false", value is false. Else fails.
        JsonNull: Fails

    Converting to JsonNull:
        In all cases: Original value is ignored and replaced with Null.

    Converting to JsonObject:
        All except JsonArray: Stores original value in the LinkedHashMap with the keyword "Value".
        JsonArray: Stores values in LinkedHashMap, using the index for the keyword. Eg: First value in array becomes {"0": "Entry"}

    Converting to JsonArray:
        All except JsonObject: Stores original value inside the Array. Should automatically be assigned the index of 0.
        JsonObject: Ignores key and only stores the values listed in the object.


        Alternate Tests have been commented out, but were made for the areas where unsure of the appropriate functionality.
    */


    //Part 1: Convert empty entry into type, check it becomes correct type. Since there is no data, we don't need to worry about converting values here.

    //1.1: Object
    @Test
    public void convertEmptyObjectToNumber(){
        JsonElement test = new JsonObject();
        JsonNumber converted = test.asNumber();
        assertTrue(converted.isNumber());
    }

    @Test
    public void convertEmptyObjectToArray(){
        JsonElement test = new JsonObject();
        JsonArray converted = test.asArray();
        assertTrue(converted.isArray());
    }

    @Test
    public void convertEmptyObjectToBoolean(){
        JsonElement test = new JsonObject();
        JsonBoolean converted = test.asBoolean();
        assertTrue(converted.isBoolean());
    }

    @Test
    public void convertEmptyObjectToNull(){
        JsonElement test = new JsonObject();
        JsonNull converted = test.asNull();
        assertTrue(converted.isNull());
    }

    @Test
    public void convertEmptyObjectToString(){
        JsonElement test = new JsonObject();
        JsonString converted = test.asString();
        assertTrue(converted.isString());
    }

    @Test
    public void convertEmptyObjectToSelf(){ //What should converting to same type actually do? Create a new one, return self, or throw error?
        JsonElement test = new JsonObject();
        JsonObject converted = test.asObject();
        assertTrue(converted.isObject());
    }

    //1.2: Number
    @Test
    public void convertEmptyNumberToSelf(){
        JsonElement test = new JsonNumber(0);
        JsonNumber converted = test.asNumber();
        assertTrue(converted.isNumber());
    }

    @Test
    public void convertEmptyNumberToArray(){
        JsonElement test = new JsonNumber(0);
        JsonArray converted = test.asArray();
        assertTrue(converted.isArray());
    }

    @Test
    public void convertEmptyNumberToBoolean(){
        JsonElement test = new JsonNumber(0);
        JsonBoolean converted = test.asBoolean();
        assertTrue(converted.isBoolean());
    }

    @Test
    public void convertEmptyNumberToNull(){
        JsonElement test = new JsonNumber(0);
        JsonNull converted = test.asNull();
        assertTrue(converted.isNull());
    }

    @Test
    public void convertEmptyNumberToString(){
        JsonElement test = new JsonNumber(0);
        JsonString converted = test.asString();
        assertTrue(converted.isString());
    }

    @Test
    public void convertEmptyNumberToObject(){
        JsonElement test = new JsonNumber(0);
        JsonObject converted = test.asObject();
        assertTrue(converted.isObject());
    }

    //1.3: Boolean
    @Test
    public void convertEmptyBooleanToNumber(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonNumber converted = test.asNumber();
        assertTrue(converted.isNumber());
    }

    @Test
    public void convertEmptyBooleanToArray(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonArray converted = test.asArray();
        assertTrue(converted.isArray());
    }

    @Test
    public void convertEmptyBooleanToSelf(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonBoolean converted = test.asBoolean();
        assertTrue(converted.isBoolean());
    }

    @Test
    public void convertEmptyBooleanToNull(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonNull converted = test.asNull();
        assertTrue(converted.isNull());
    }

    @Test
    public void convertEmptyBooleanToString(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonString converted = test.asString();
        assertTrue(converted.isString());
    }

    @Test
    public void convertEmptyBooleanToObject(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonObject converted = test.asObject();
        assertTrue(converted.isObject());
    }

    //1.4: String
    @Test
    public void convertEmptyStringToNumber(){
        JsonElement test = new JsonString("");
        JsonNumber converted = test.asNumber();
        assertTrue(converted.isNumber());
    }

    @Test
    public void convertEmptyStringToArray(){
        JsonElement test = new JsonString("");
        JsonArray converted = test.asArray();
        assertTrue(converted.isArray());
    }

    @Test
    public void convertEmptyStringToBoolean(){
        JsonElement test = new JsonString("");
        JsonBoolean converted = test.asBoolean();
        assertTrue(converted.isBoolean());
    }

    @Test
    public void convertEmptyStringToNull(){
        JsonElement test = new JsonString("");
        JsonNull converted = test.asNull();
        assertTrue(converted.isNull());
    }

    @Test
    public void convertEmptyStringToSelf(){
        JsonElement test = new JsonString("");
        JsonString converted = test.asString();
        assertTrue(converted.isString());
    }

    @Test
    public void convertEmptyStringToObject(){
        JsonElement test = new JsonString("");
        JsonObject converted = test.asObject();
        assertTrue(converted.isObject());
    }

    //1.5: Array
    @Test
    public void convertEmptyArrayToNumber(){
        JsonElement test = new JsonArray();
        JsonNumber converted = test.asNumber();
        assertTrue(converted.isNumber());
    }

    @Test
    public void convertEmptyArrayToSelf(){
        JsonElement test = new JsonArray();
        JsonArray converted = test.asArray();
        assertTrue(converted.isArray());
    }

    @Test
    public void convertEmptyArrayToBoolean(){
        JsonElement test = new JsonArray();
        JsonBoolean converted = test.asBoolean();
        assertTrue(converted.isBoolean());
    }

    @Test
    public void convertEmptyArrayToNull(){
        JsonElement test = new JsonArray();
        JsonNull converted = test.asNull();
        assertTrue(converted.isNull());
    }

    @Test
    public void convertEmptyArrayToString(){
        JsonElement test = new JsonArray();
        JsonString converted = test.asString();
        assertTrue(converted.isString());
    }

    @Test
    public void convertEmptyArrayToObject(){
        JsonElement test = new JsonArray();
        JsonObject converted = test.asObject();
        assertTrue(converted.isObject());
    }

    //Part 2: Converting Empty, check they don't retain old type.
    //2.1.1: Object as Number
    @Test
    public void convertedObjectNumberNotBoolean(){
        JsonElement test = new JsonObject();
        JsonNumber converted = test.asNumber();
        assertFalse(converted.isBoolean());
    }

    @Test
    public void convertedObjectNumberNotString(){
        JsonElement test = new JsonObject();
        JsonNumber converted = test.asNumber();
        assertFalse(converted.isString());
    }

    @Test
    public void convertedObjectNumberNotNull(){
        JsonElement test = new JsonObject();
        JsonNumber converted = test.asNumber();
        assertFalse(converted.isNull());
    }

    @Test
    public void convertedObjectNumberNotArray(){
        JsonElement test = new JsonObject();
        JsonNumber converted = test.asNumber();
        assertFalse(converted.isArray());
    }

    @Test
    public void convertedObjectNumberNotObject(){
        JsonElement test = new JsonObject();
        JsonNumber converted = test.asNumber();
        assertFalse(converted.isObject());
    }


    //2.1.2: Object as Boolean
    @Test
    public void convertedObjectBooleanNotNumber(){
        JsonElement test = new JsonObject();
        JsonBoolean converted = test.asBoolean();
        assertFalse(converted.isNumber());
    }

    @Test
    public void convertedObjectBooleanNotString(){
        JsonElement test = new JsonObject();
        JsonBoolean converted = test.asBoolean();
        assertFalse(converted.isString());
    }

    @Test
    public void convertedObjectBooleanNotNull(){
        JsonElement test = new JsonObject();
        JsonBoolean converted = test.asBoolean();
        assertFalse(converted.isNull());
    }

    @Test
    public void convertedObjectBooleanNotArray(){
        JsonElement test = new JsonObject();
        JsonBoolean converted = test.asBoolean();
        assertFalse(converted.isArray());
    }

    @Test
    public void convertedObjectBooleanNotObject(){
        JsonElement test = new JsonObject();
        JsonBoolean converted = test.asBoolean();
        assertFalse(converted.isObject());
    }

    //2.1.3: Object as String
    @Test
    public void convertedObjectStringNotNumber(){
        JsonElement test = new JsonObject();
        JsonString converted = test.asString();
        assertFalse(converted.isNumber());
    }

    @Test
    public void convertedObjectStringNotBoolean(){
        JsonElement test = new JsonObject();
        JsonString converted = test.asString();
        assertFalse(converted.isBoolean());
    }

    @Test
    public void convertedObjectStringNotNull(){
        JsonElement test = new JsonObject();
        JsonString converted = test.asString();
        assertFalse(converted.isNull());
    }

    @Test
    public void convertedObjectStringNotArray(){
        JsonElement test = new JsonObject();
        JsonString converted = test.asString();
        assertFalse(converted.isArray());
    }

    @Test
    public void convertedObjectStringNotObject(){
        JsonElement test = new JsonObject();
        JsonString converted = test.asString();
        assertFalse(converted.isObject());
    }




    //2.1.4: Object as Null
    @Test
    public void convertedObjectNullNotNumber(){
        JsonElement test = new JsonObject();
        JsonNull converted = test.asNull();
        assertFalse(converted.isNumber());
    }

    @Test
    public void convertedObjectNullNotBoolean(){
        JsonElement test = new JsonObject();
        JsonNull converted = test.asNull();
        assertFalse(converted.isBoolean());
    }

    @Test
    public void convertedObjectNullNotString(){
        JsonElement test = new JsonObject();
        JsonNull converted = test.asNull();
        assertFalse(converted.isString());
    }

    @Test
    public void convertedObjectNullNotArray(){
        JsonElement test = new JsonObject();
        JsonNull converted = test.asNull();
        assertFalse(converted.isArray());
    }

    @Test
    public void convertedObjectNullNotObject(){
        JsonElement test = new JsonObject();
        JsonNull converted = test.asNull();
        assertFalse(converted.isObject());
    }



    //2.1.5: Object as Array
    @Test
    public void convertedObjectArrayNotNumber(){
        JsonElement test = new JsonObject();
        JsonArray converted = test.asArray();
        assertFalse(converted.isNumber());
    }

    @Test
    public void convertedObjectArrayNotBoolean(){
        JsonElement test = new JsonObject();
        JsonArray converted = test.asArray();
        assertFalse(converted.isBoolean());
    }

    @Test
    public void convertedObjectArrayNotString(){
        JsonElement test = new JsonObject();
        JsonArray converted = test.asArray();
        assertFalse(converted.isNull());
    }

    @Test
    public void convertedObjectArrayNotNull(){
        JsonElement test = new JsonObject();
        JsonArray converted = test.asArray();
        assertFalse(converted.isNull());
    }

    @Test
    public void convertedObjectArrayNotObject(){
        JsonElement test = new JsonObject();
        JsonArray converted = test.asArray();
        assertFalse(converted.isObject());
    }

    //2.1.6: Object as Object... Is this necessary?
    @Test
    public void convertedObjectObjectNotNumber(){
        JsonElement test = new JsonObject();
        JsonObject converted = test.asObject();
        assertFalse(converted.isNumber());
    }

    @Test
    public void convertedObjectObjectNotBoolean(){
        JsonElement test = new JsonObject();
        JsonObject converted = test.asObject();
        assertFalse(converted.isBoolean());
    }

    @Test
    public void convertedObjectObjectNotString(){
        JsonElement test = new JsonObject();
        JsonObject converted = test.asObject();
        assertFalse(converted.isNull());
    }

    @Test
    public void convertedObjectObjectNotNull(){
        JsonElement test = new JsonObject();
        JsonObject converted = test.asObject();
        assertFalse(converted.isNull());
    }

    @Test
    public void convertedObjectObjectNotArray(){
        JsonElement test = new JsonObject();
        JsonObject converted = test.asObject();
        assertFalse(converted.isArray());
    }

    //Part 3: Converting filled JsonElements to other Types

    //3.1 JsonObject to other types...
    @Test
    public void filledObjectToNumberValid(){
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("1"));
        JsonNumber converted = test.asNumber();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledObjectToNumberInvalid() { //Is this the correct formatting?
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("one"));
        JsonNumber converted = test.asNumber();
        assertEquals("0", converted.toString());
    }

    @Test
    public void filledObjectMultipleToNumberInvalid() {
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonNumber(1)).setProperty("Key 2", new JsonNumber(2));
        JsonNumber converted = test.asNumber();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledObjectToBooleanValid(){
        JsonElement test = new JsonObject().setProperty("Key 1", JsonBoolean.getInstance(true));
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledObjectToBooleanInvalid() {
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("lies"));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledObjectToBoolValidNum(){ //Should numbers from an object be converted properly?
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("0"));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledObjectMultipleToBooleanInvalid() {
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("true")).setProperty("Key 2", new JsonNumber(0));
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledObjectToString(){
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("aaa"));
        JsonString converted = test.asString();
        assertEquals("aaa", converted.toString());
    }

    @Test
    public void filledObjectToStringNumber(){
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonNumber(1));
        JsonString converted = test.asString();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledObjectToStringBool(){
        JsonElement test = new JsonObject().setProperty("Key 1", JsonBoolean.getInstance(false));
        JsonString converted = test.asString();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledObjectToStringNull(){
        JsonElement test = new JsonObject().setProperty("Key 1", JsonNull.getInstance());
        JsonString converted = test.asString();
        assertEquals("null", converted.toString());
    }

    /*
    @Test
    public void filledObjectToStringNullAlternative(){
        JsonElement test = new JsonObject().addProperty("Key 1", null);
        assertThrows(Exception.class, test::asString);
    }
    */

    @Test
    public void filledObjectMultipleToString(){
        JsonElement test = new JsonObject().setProperty("Key 1", JsonNull.getInstance()).setProperty("Key 2", new JsonString("Irrelevant"));
        JsonString converted = test.asString();
        assertEquals("null", converted.toString());
    }

    @Test
    public void filledObjectToNull(){ //What should happen here? Error or just turn it to null regardless?
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("null"));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    @Test
    public void filledObjectToNullNonMatch(){ //What should happen here? Error or just turn it to null regardless?
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("irrelevant"));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    /*
    @Test
    public void filledObjectToNullNonMatchAlternative(){ //If we wanted it to throw an error instead.
        JsonElement test = new JsonObject().addProperty("Key 1", "irrelevant");
        assertThrows(Exception.class, test::asNull);
    }

     */

    @Test
    public void filledObjectToArray(){
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("value"));
        JsonArray converted = test.asArray();
        assertEquals("[\"value\"]", converted.toString());
    }

    @Test
    public void filledObjectMultipleToArray(){
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("value")).setProperty("Key 2", new JsonNumber(2));
        JsonArray converted = test.asArray();
        assertEquals("[\"value\",2]", converted.toString());
    }

    @Test
    public void filledObjectMultipleToArrayAlternate(){ //Alternate version depending on what makes sense
        JsonElement test = new JsonObject().setProperty("Key 1", new JsonString("value")).setProperty("Key 2", new JsonNumber(2));
        JsonArray converted = test.asArray();
        assertEquals("[\"value\",2]", converted.toString());
    }

    //3.2 Filled Number Conversion
    @Test
    public void filledNumberToObject(){
        JsonElement test = new JsonNumber(5);
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":5}", converted.toString());
    }

    @Test
    public void filledNumberToBooleanTrue(){
        JsonElement test = new JsonNumber(1);
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledNumberToBooleanFalse(){
        JsonElement test = new JsonNumber(0);
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledNumberToBooleanInvalid(){
        JsonElement test = new JsonNumber(5);
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledNumberToString(){
        JsonElement test = new JsonNumber(5);
        JsonString converted = test.asString();
        assertEquals("5", converted.toString());
    }

    @Test
    public void filledNumberToNull(){
        JsonElement test = new JsonNumber(5);
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    /*
    @Test
    public void filledNumberToNullAlternative(){
        JsonElement test = new JsonNumber(5);
        assertThrows(Exception.class, test::asNull);
    }

     */

    @Test
    public void filledNumberToArray(){
        JsonElement test = new JsonNumber(5);
        JsonArray converted = test.asArray();
        assertEquals("[5]", converted.toString());
    }




    //3.3 Filled Boolean Conversion
    @Test
    public void filledBooleanToObject(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":true}", converted.toString());
    }

    @Test
    public void filledBooleanToNumberTrue(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonNumber converted = test.asNumber();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledBooleanToNumberFalse(){
        JsonElement test = JsonBoolean.getInstance(false);
        JsonNumber converted = test.asNumber();
        assertEquals("0", converted.toString());
    }

    @Test
    public void filledBooleanToString(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonString converted = test.asString();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledBooleanToNull(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    /*
    @Test
    public void filledBooleanToNullAlternative(){
        JsonElement test = JsonBoolean.getInstance(true);
        assertThrows(Exception.class, test::asNull);
    }

     */

    @Test
    public void filledBooleanToArray(){
        JsonElement test = JsonBoolean.getInstance(true);
        JsonArray converted = test.asArray();
        assertEquals("[true]", converted.toString());
    }


    //3.4 Filled String Conversion
    @Test
    public void filledStringToNumberValid(){
        JsonElement test = new JsonString("1");
        JsonNumber converted = test.asNumber();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledStringToNumberInvalid() { //Is this the correct formatting?
        JsonElement test = new JsonString("one");
        JsonNumber converted = test.asNumber();
        assertEquals("0", converted.toString());
    }


    @Test
    public void filledStringToBooleanValid(){
        JsonElement test = new JsonString("false");
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledStringToBooleanInvalid() {
        JsonElement test = new JsonString("incorrect");
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledStringToBooleanValidNum(){ //Should numbers from an object be converted properly?
        JsonElement test = new JsonString("1");
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledStringToObject(){
        JsonElement test = new JsonString("Text");
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":\"Text\"}", converted.toString());
    }


    @Test
    public void filledStringToNull(){
        JsonElement test = new JsonString("null");
        JsonString converted = test.asString();
        assertEquals("null", converted.toString());
    }
    /*
    @Test
    public void filledStringToNullAlternative(){
        JsonElement test = new JsonString("null");
        assertThrows(Exception.class, test::asString);
    }

     */

    @Test
    public void filledStringToNullNonMatch(){ //What should happen here? Error or just turn it to null regardless?
        JsonElement test = new JsonString("not null");
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    @Test
    public void filledStringToArray(){
        JsonElement test = new JsonString("Text");
        JsonArray converted = test.asArray();
        assertEquals("[\"Text\"]", converted.toString());
    }

    //3.5 Filled Array Conversion
    //Unsure about how the conversions should apply to this. Should they at all?

    //3.5.1 Array with number(s)
    @Test
    public void filledArrayNumberToNumber(){
        JsonElement test = new JsonArray().addElement(new JsonNumber(5));
        JsonNumber converted = test.asNumber();
        assertEquals("5", converted.toString());
    }

    @Test
    public void filledArrayNumberToString(){
        JsonElement test = new JsonArray().addElement(new JsonNumber(5));
        JsonString converted = test.asString();
        assertEquals("5", converted.toString());
    }

    @Test
    public void filledArrayNumberToBooleanValid(){
        JsonElement test = new JsonArray().addElement(new JsonNumber(0));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledArrayNumberToBooleanInvalid(){
        JsonElement test = new JsonArray().addElement(new JsonNumber(5));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledArrayNumberToNull(){
        JsonElement test = new JsonArray().addElement(new JsonNumber(5));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    @Test
    public void filledArrayNumberToObject(){
        JsonElement test = new JsonArray().addElement(new JsonNumber(1));
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":1}", converted.toString());
    }

    //3.5.2 String Array
    @Test
    public void filledArrayStringToNumber(){
        JsonElement test = new JsonArray().addElement(new JsonString("5"));
        JsonNumber converted = test.asNumber();
        assertEquals("5", converted.toString());
    }

    @Test
    public void filledArrayStringToNumberInvalid(){
        JsonElement test = new JsonArray().addElement(new JsonString("Twenty"));
        JsonNumber converted = test.asNumber();
        assertEquals("0", converted.toString());
    }

    @Test
    public void filledArrayStringToBoolean(){
        JsonElement test = new JsonArray().addElement(new JsonString("true"));
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledArrayStringToBooleanNumber(){
        JsonElement test = new JsonArray().addElement(new JsonString("0"));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledArrayStringToBooleanInvalid(){
        JsonElement test = new JsonArray().addElement(new JsonString("Fake"));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledArrayStringToObject(){
        JsonElement test = new JsonArray().addElement(new JsonString("Value"));
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":\"Value\"}", converted.toString());
    }

    @Test
    public void filledArrayStringToNull(){
        JsonElement test = new JsonArray().addElement(new JsonString("null"));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    //3.5.3 Boolean Array
    @Test
    public void filledArrayBooleanToNumber(){
        JsonElement test = new JsonArray().addElement(JsonBoolean.getInstance(true));
        JsonNumber converted = test.asNumber();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledArrayBooleanToString(){
        JsonElement test = new JsonArray().addElement(JsonBoolean.getInstance(true));
        JsonString converted = test.asString();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledArrayBooleanToObject(){
        JsonElement test = new JsonArray().addElement(JsonBoolean.getInstance(true));
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":true}", converted.toString());
    }

    @Test
    public void filledArrayBooleanToNull(){ //Value is irrelevant
        JsonElement test = new JsonArray().addElement(JsonBoolean.getInstance(false));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    //3.5.4 Null Array.
    @Test
    public void filledArrayNullToNumber(){
        JsonElement test = new JsonArray().addElement(JsonNull.getInstance());
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    @Test
    public void filledArrayNullToBoolean(){
        JsonElement test = new JsonArray().addElement(JsonNull.getInstance());
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    @Test
    public void filledArrayNullToString(){
        JsonElement test = new JsonArray().addElement(JsonNull.getInstance());
        JsonString converted = test.asString();
        assertEquals("null", converted.toString());
    }

    @Test
    public void filledArrayNullToObject(){
        JsonElement test = new JsonArray().addElement(JsonNull.getInstance());
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":null}", converted.toString());
    }


    //3.5.5 Object Array
    //3.5.5.1 Object contains number
    @Test
    public void filledArrayObjectNumberToNumber(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonNumber(1)));
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":{\"Value\":1}}", converted.toString());
    }

    @Test
    public void filledArrayObjectNumberToString(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonNumber(1)));
        JsonString converted = test.asString();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledArrayObjectNumberToBoolean(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonNumber(0)));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledArrayObjectNumberToBooleanInvalid(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonNumber(15)));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledArrayObjectNumberToNull(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonNumber(1)));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }
    //----------
    @Test
    public void filledArrayObjectStringToNumber(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonString("5")));
        JsonNumber converted = test.asNumber();
        assertEquals("5", converted.toString());
    }

    @Test
    public void filledArrayObjectStringToNumberInvalid(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonString("tenth")));
        JsonNumber converted = test.asNumber();
        assertEquals("0", converted.toString());
    }

    @Test
    public void filledArrayObjectStringToString(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonString("Text")));
        JsonString converted = test.asString();
        assertEquals("Text", converted.toString());
    }

    @Test
    public void filledArrayObjectStringToBoolean(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonString("false")));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledArrayObjectStringToBooleanInvalid(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonString("Text")));
        JsonBoolean converted = test.asBoolean();
        assertEquals("false", converted.toString());
    }

    @Test
    public void filledArrayObjectStringToNull(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonString("Text")));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }
    //----------
    @Test
    public void filledArrayObjectBooleanToNumber(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", JsonBoolean.getInstance(true)));
        JsonNumber converted = test.asNumber();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledArrayObjectBooleanToString(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", JsonBoolean.getInstance(true)));
        JsonString converted = test.asString();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledArrayObjectBooleanToBoolean(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", JsonBoolean.getInstance(true)));
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledArrayObjectBooleanToNull(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", JsonBoolean.getInstance(true)));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    //----------
    @Test
    public void filledArrayObjectNullToNumber(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", JsonNull.getInstance()));
        JsonNumber converted = test.asNumber();
        assertEquals("0", converted.toString());
    }

    @Test
    public void filledArrayObjectNullToString(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", JsonNull.getInstance()));
        JsonString converted = test.asString();
        assertEquals("null", converted.toString());
    }

    @Test
    public void filledArrayObjectNullToBoolean(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", JsonNull.getInstance()));
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledArrayObjectNullToNull(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", JsonNull.getInstance()));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }


    //----------Converting object with inner object should fail in all cases, except maybe converting it into another array? Else just return itself?
    @Test
    public void filledArrayObjectObjectToNumber(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonObject().setProperty("ValueInner", new JsonNumber(1))));
        JsonNumber converted = test.asNumber();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledArrayObjectObjectToString(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonObject().setProperty("ValueInner", new JsonNumber(1))));
        JsonString converted = test.asString();
        assertEquals("1", converted.toString());
    }

    @Test
    public void filledArrayObjectObjectToBoolean(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonObject().setProperty("ValueInner", new JsonNumber(1))));
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
    }

    @Test
    public void filledArrayObjectObjectToNull(){
        JsonElement test = new JsonArray().addElement(new JsonObject().setProperty("Value", new JsonObject().setProperty("ValueInner", new JsonNumber(1))));
        JsonNull converted = test.asNull();
        assertEquals("null", converted.toString());
    }

    /*
    @Test
    public void filledArrayObjectObjectToArray(){ //Does this make sense?
        JsonElement test = new JsonArray().addElement(new JsonObject().addProperty("Value", new JsonObject().addProperty("ValueInner", 1)));
        JsonArray converted = test.asArray();
        assertEquals("[{\"ValueInner\": 1}]", converted.toString());
    }
    */
    //----------

    //3.5.6 Array Array
    //I think this one simply returns itself? Might need to expand if test case can be thought of.


    //3.6 Null Conversion
    //Null is a special case as it always has its value set to null.
    @Test
    public void convertNullToNumber(){
        JsonElement test = JsonNull.getInstance();
        JsonNumber converted = test.asNumber();
        assertTrue(converted.isNumber());
    }

    @Test
    public void convertNullToArray(){
        JsonElement test = JsonNull.getInstance();
        JsonArray converted = test.asArray();
        assertTrue(converted.isArray());
    }

    @Test
    public void convertNullValueToArray(){
        JsonElement test = JsonNull.getInstance();
        JsonArray converted = test.asArray();
        assertEquals("[null]", converted.toString());
    }

    @Test
    public void convertNullToBoolean(){
        JsonElement test = JsonNull.getInstance();
        JsonBoolean converted = test.asBoolean();
        assertEquals("true", converted.toString());
        //assertThrows(Exception.class, test::asBoolean);
    }

    @Test
    public void convertNullToSelf(){
        JsonElement test = JsonNull.getInstance();
        JsonNull converted = test.asNull();
        assertTrue(converted.isNull());
    }

    @Test
    public void convertNullToString(){
        JsonElement test = JsonNull.getInstance();
        JsonString converted = test.asString();
        assertTrue(converted.isString());
    }

    @Test
    public void convertNullValueToString(){
        JsonElement test = JsonNull.getInstance();
        JsonString converted = test.asString();
        assertEquals("null", converted.toString());
    }

    @Test
    public void convertNullToObject(){
        JsonElement test = JsonNull.getInstance();
        JsonObject converted = test.asObject();
        assertTrue(converted.isObject());
    }

    @Test
    public void convertNullValueToObject(){
        JsonElement test = JsonNull.getInstance();
        JsonObject converted = test.asObject();
        assertEquals("{\"Value\":null}", converted.toString());
    }
}
