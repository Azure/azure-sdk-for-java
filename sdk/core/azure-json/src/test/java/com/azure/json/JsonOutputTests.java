package com.azure.json;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonOutputTests {
    //Tests primarily focusing on the toJson method of JsonArray and JsonObject.

    @Test
    public void objectWithStringJSON(){
        String expected = "{\"Value1\":\"Write String\"}";
        String actual = new JsonObject().addProperty("Value1", new JsonString("Write String")).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithIntJSON(){
        String expected = "{\"Value1\":111}";
        String actual = new JsonObject().addProperty("Value1", new JsonNumber(111)).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithFloatJSON(){
        String expected = "{\"Value1\":1.23}";
        String actual = new JsonObject().addProperty("Value1", new JsonNumber(1.23)).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithBooleanJSON(){
        String expected = "{\"Value1\":true}";
        String actual = new JsonObject().addProperty("Value1", new JsonBoolean(true)).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithNullJSON(){
        String expected = "{\"Value1\":null}";
        String actual = new JsonObject().addProperty("Value1", JsonNull.getInstance()).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithInnerObjectJSON(){
        String expected = "{\"Value1\":{\"Value2\":\"Second Layer\"}}";
        String actual = new JsonObject().addProperty("Value1", new JsonObject().addProperty("Value2", new JsonString("Second Layer"))).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectTwoPropertiesJSONSame(){ //Adding two values with the same type.
        String expected = "{\"Value1\":\"First Layer\",\"Value2\":\"Second Layer\"}";
        String actual = new JsonObject()
            .addProperty("Value1", new JsonString("First Layer"))
            .addProperty("Value2", new JsonString("Second Layer"))
            .toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectTwoPropertiesJSONDifferent(){ //Adding values to JSONObject that have different variable types
        String expected = "{\"Value1\":\"First Layer\",\"Value2\":2}";
        String actual = new JsonObject()
            .addProperty("Value1", new JsonString("First Layer"))
            .addProperty("Value2", new JsonNumber(2))
            .toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithArrayObjectsJSON(){
        String expected = "{\"Value1\":[{\"Value2\":2},{\"Value3\":3}]}";
        String actual = new JsonObject()
            .addProperty("Value1", new JsonArray()
                .addElement(new JsonObject().addProperty("Value2", new JsonNumber(2)))
                .addElement(new JsonObject().addProperty("Value3", new JsonNumber(3)))
            )
            .toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void objectArrayNumbersJSON(){ //Convert int values into a JsonElement type, then add to JsonObject. Not sure if this is correct way to handle it?
        String expected = "{\"intList\":[3,1,4,1,5,9]}";
        int[] values = new int[]{3, 1, 4, 1, 5, 9};
        JsonArray intList = new JsonArray();
        for (int a: values){
            intList.addElement(new JsonNumber(a));
        }
        String actual = new JsonObject().addProperty("intList", intList).toJson();
        assertEquals(expected, actual);
    }

    @Test
    public void complexJSONIsolated(){
        String expected = "{\"James\":\"Anderson\",\"Michael\":\"Campbell\",\"Mary\":\"Jones\",\"John\":\"Williams\"}";
        JsonObject jsonObj = new JsonObject();
        // Adding properties - showcasing addProperty.
        jsonObj.addProperty("James", new JsonString("Anderson"))
            .addProperty("Michael", new JsonString("Campbell"))
            .addProperty("Mary", new JsonString("Jones"))
            .addProperty("John", new JsonString("Williams"));
        assertEquals(expected, jsonObj.toJson());
    }

    @Test
    public void complexJSONIsolatedEdited() throws IOException {
        String expected = "{\"James\":\"Anderson\",\"Mary\":\"Jones\",\"John\":\"Williams\"}";
        JsonObject jsonObj = new JsonObject();
        // Adding properties - showcasing addProperty.
        jsonObj.addProperty("James", new JsonString("Anderson"))
            .addProperty("Michael", new JsonString("Campbell"))
            .addProperty("Mary", new JsonString("Jones"))
            .addProperty("John", new JsonString("Williams"));
        jsonObj.removeProperty("Michael");
        assertEquals(expected, jsonObj.toJson());
    }

    @Test
    public void complexJSONChaining() throws IOException {
        String expected = "{\"Mary\":\"Jones\",\"John\":\"Williams\"}";
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("James", new JsonString("Anderson"))
            .addProperty("Michael", new JsonString("Campbell"))
            .addProperty("Mary", new JsonString("Jones"))
            .removeProperty("Michael")
            .addProperty("John", new JsonString("Williams"))
            .removeProperty("James");
        assertEquals(expected, jsonObj.toJson());
    }

    @Test
    public void complexJSONNesting(){
        String expected = "{\"James\":{\"Country\":\"New Zealand\",\"Surname\":\"Anderson\"},\"Michael\":{\"Country\":\"Australia\",\"Surname\":\"Campbell\"},\"Mary\":{\"Country\":\"Canada\",\"Surname\":\"Jones\"},\"John\":{\"Country\":\"Australia\",\"Surname\":\"Williams\"}}";
        JsonObject jsonObj = new JsonObject();

        jsonObj.addProperty(
            "James",
            new JsonObject().addProperty("Country", new JsonString("New Zealand")).addProperty("Surname", new JsonString("Anderson"))
        ).addProperty(
            "Michael",
            new JsonObject().addProperty("Country", new JsonString("Australia")).addProperty("Surname", new JsonString("Campbell"))
        ).addProperty(
            "Mary",
            new JsonObject().addProperty("Country", new JsonString("Canada")).addProperty("Surname", new JsonString("Jones"))
        ).addProperty(
            "John",
            new JsonObject().addProperty("Country", new JsonString("Australia")).addProperty("Surname", new JsonString("Williams"))
        );
        assertEquals(expected, jsonObj.toJson());
    }

    @Test
    public void complexJSONArrays() throws IOException {
        String expected = "[\"New Zealand\",\"Australia\",\"United States\",\"England\",\"Ireland\",\"South Africa\",\"Canada\"]";
        List<String> countries = new ArrayList<>();
        String[] countryExamples = { "New Zealand", "Australia", "United States", "England", "Ireland", "South Africa", "Canada" };
        Collections.addAll(countries, countryExamples);

        JsonArray countryArray = new JsonArray();
        for (String country : countries) {
            countryArray.addElement(
                new JsonString(country));
        }
        assertEquals(expected, countryArray.toJson());
    }

    @Test
    public void complexJSONArraysChaining() throws IOException {
        String expected = "[\"New Zealand\",\"Canada\"]";
        JsonArray countryArray = new JsonArray();
        countryArray.addElement(new JsonString("New Zealand"))
            .addElement(new JsonString("Australia"))
            .addElement(new JsonString("Canada"))
            .removeElement(1)
            .addElement(new JsonString("United States"))
            .removeElement(2);
        assertEquals(expected, countryArray.toJson());
    }
}
