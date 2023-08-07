package com.azure.json;

import com.sun.jdi.InvalidTypeException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class JsonTesting {

    //Section 1: Creating types and testing isObject(), isNumber() etc work as intended.
    @Test
    public void createJsonObject(){
        JsonObject test = new JsonObject();
        assertTrue(test.isObject());
    }

    @Test
    public void createJsonString(){
        JsonString test = new JsonString();
        assertTrue(test.isString());
    }

    @Test
    public void createJsonNumber(){
        JsonNumber test = new JsonNumber();
        assertTrue(test.isNumber());
    }

    @Test
    public void createJsonBoolean(){
        JsonBoolean test = new JsonBoolean();
        assertTrue(test.isBoolean());
    }

    @Test
    public void createJsonNull(){
        JsonNull test = new JsonNull();
        assertTrue(test.isNull());
    }

    @Test
    public void createJsonArray(){
        JsonArray test = new JsonArray();
        assertTrue(test.isArray());
    }

    @Test
    public void createWrongType(){ //If any of the other type checks returns true, wrongtypes = true, meaning something went wrong.
        JsonObject test = new JsonObject();
        boolean wrongTypes = test.isArray() || test.isBoolean() || test.isNull() || test.isNumber() || test.isString();
        assertFalse(wrongTypes);
    }

    //Section 2: Conversions with asArray, asObject etc.
    @Test
    public void convertEmptyObjectToArray(){ //Simply convert type without care for the values
        JsonElement test = new JsonObject();
        JsonArray converted = test.asArray();
        assertTrue(converted.isArray());
    }

    @Test
    public void convertFilledObjectToArray(){
        JsonElement test = new JsonObject().addProperty("First", "Value").addProperty("Second", 2);
        JsonArray converted = test.asArray();
        assertEquals((test.asObject().getProperty("First")), converted.getElement(0));
    }

    @Test
    public void convertJsonStringToNumber(){
        JsonElement test = new JsonString("1");
        assertEquals("1", test.asNumber().toString()); //If string, it would be ""1"";
    }

    @Test
    public void convertJsonStringToBoolean(){
        JsonElement test = new JsonString("true");
        assertEquals("true", test.asBoolean().toString());
    }

    @Test
    public void convertJsonStringToNull(){
        JsonElement test = new JsonString("Input shouldn't matter");
        assertEquals("null", test.asNull().toString());
    }

    @Test
    public void convertJsonNumberToString(){
        JsonElement test = new JsonNumber(5);
        assertEquals("\"5\"", test.asString().toString());
    }

    @Test
    public void convertJsonNumberToBoolean(){
        JsonElement test = new JsonNumber(0);
        assertEquals("false", test.asBoolean().toString());
    }

    @Test
    public void convertJsonBooleanToString(){
        JsonElement test = new JsonBoolean(true);
        assertEquals("\"true\"", test.asString().toString());
    }

    @Test
    public void convertJsonBooleanToNumber(){
        JsonElement test = new JsonBoolean(false);
        assertEquals("0", test.asNumber().toString());
    }

    @Test
    public void convertAnythingToNull(){
        JsonElement test = new JsonNumber(5);
        assertEquals("null", test.asNull().toString());
    }

    //Section 3: Sample toJson tests, basic versions
    @Test
    public void objectString(){
        String expected = "{\"Value1\": \"Write String\"}";
        String actual = new JsonObject().addProperty("Value1", "Write String").toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectInt(){
        String expected = "{\"Value1\": 111}";
        String actual = new JsonObject().addProperty("Value1", 111).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectFloat(){
        String expected = "{\"Value1\": 1.23}";
        String actual = new JsonObject().addProperty("Value1", 1.23).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectBoolean(){
        String expected = "{\"Value1\": true}";
        String actual = new JsonObject().addProperty("Value1", true).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectNull(){
        String expected = "{\"Value1\": null}";
        String actual = new JsonObject().addProperty("Value1", null).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectInObject(){
        String expected = "{\"Value1\": {\"Value2\": \"Second Layer\"}}";
        String actual = new JsonObject().addProperty("Value1", new JsonObject().addProperty("Value2", "Second Layer")).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectTwoPropertiesSame(){
        String expected = "{\"Value1\": \"First Layer\", \"Value2\": \"Second Layer\"}";
        String actual = new JsonObject()
                .addProperty("Value1", "First Layer")
                .addProperty("Value2", "Second Layer")
                .toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectTwoPropertiesDifferent(){
        String expected = "{\"Value1\": \"First Layer\", \"Value2\": 2}";
        String actual = new JsonObject()
                .addProperty("Value1", "First Layer")
                .addProperty("Value2", 2)
                .toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectArrayObjects(){
        String expected = "{\"Value1\": [{\"Value2\": 2}, {\"Value3\": 3}]}";
        String actual = new JsonObject()
                .addProperty("Value1", new JsonArray()
                        .addElement(new JsonObject().addProperty("Value2", 2))
                        .addElement(new JsonObject().addProperty("Value3", 3))
                )
                .toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectArrayNumbers(){ //Convert int values into a JsonElement type, then add to JsonObject
        String expected = "{\"intList\": [3, 1, 4, 1, 5, 9]}";
        int[] values = new int[]{3, 1, 4, 1, 5, 9};
        JsonArray intList = new JsonArray();
        for (int a: values){
            intList.addElement(new JsonNumber(a));
        }
        String actual = new JsonObject().addProperty("intList", intList).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectArrayStringsDirect(){ //I am unsure where we are supposed to be able to directly add arrays. Might be standard practice to convert them to JsonArrays beforehand.
        String expected = "{\"Value1\": [\"aaa\", \"bbb\"]}";
        String[] stringArray = new String[]{"aaa", "bbb"};
        String actual = new JsonObject().addProperty("Value1", stringArray).toJson();
        assertEquals(expected, actual);
    }

    //Section 4: Modify existing Entries
    @Test
    public void objectSetProperty() throws InvalidTypeException {
        JsonElement test = new JsonObject().addProperty("First", "Keyword");
        test.asObject().setProperty("First", "Replaced");
        assertEquals("\"Replaced\"", test.asObject().getProperty("First").toString());
    }

    @Test
    public void arraySetElement(){
        JsonElement test = new JsonArray().addElement(new JsonString("Keyword"));
        test.asArray().setElement(0, new JsonString("Replaced"));
        assertEquals("\"Replaced\"", test.asArray().getElement(0).toString());
    }
}


