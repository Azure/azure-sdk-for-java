// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.tree.JsonArray;
import com.azure.json.tree.JsonBoolean;
import com.azure.json.tree.JsonNull;
import com.azure.json.tree.JsonNumber;
import com.azure.json.tree.JsonObject;
import com.azure.json.tree.JsonString;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonOutputTests {
    //Tests primarily focusing on the toJson method of JsonArray and JsonObject.

    @Test
    public void objectWithStringJSON() throws IOException {
        String expected = "{\"Value1\":\"Write String\"}";
        String actual = new JsonObject().setProperty("Value1", new JsonString("Write String")).toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithIntJSON() throws IOException {
        String expected = "{\"Value1\":111}";
        String actual = new JsonObject().setProperty("Value1", new JsonNumber(111)).toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithFloatJSON() throws IOException {
        String expected = "{\"Value1\":1.23}";
        String actual = new JsonObject().setProperty("Value1", new JsonNumber(1.23)).toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithBooleanJSON() throws IOException {
        String expected = "{\"Value1\":true}";
        String actual = new JsonObject().setProperty("Value1", JsonBoolean.getInstance(true)).toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithNullJSON() throws IOException {
        String expected = "{\"Value1\":null}";
        String actual = new JsonObject().setProperty("Value1", JsonNull.getInstance()).toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithInnerObjectJSON() throws IOException {
        String expected = "{\"Value1\":{\"Value2\":\"Second Layer\"}}";
        String actual = new JsonObject()
            .setProperty("Value1", new JsonObject().setProperty("Value2", new JsonString("Second Layer")))
            .toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectTwoPropertiesJSONSame() throws IOException { //Adding two values with the same type.
        String expected = "{\"Value1\":\"First Layer\",\"Value2\":\"Second Layer\"}";
        String actual = new JsonObject().setProperty("Value1", new JsonString("First Layer"))
            .setProperty("Value2", new JsonString("Second Layer"))
            .toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectTwoPropertiesJSONDifferent() throws IOException { //Adding values to JSONObject that have different variable types
        String expected = "{\"Value1\":\"First Layer\",\"Value2\":2}";
        String actual = new JsonObject().setProperty("Value1", new JsonString("First Layer"))
            .setProperty("Value2", new JsonNumber(2))
            .toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectWithArrayObjectsJSON() throws IOException {
        String expected = "{\"Value1\":[{\"Value2\":2},{\"Value3\":3}]}";
        String actual
            = new JsonObject()
                .setProperty("Value1",
                    new JsonArray().addElement(new JsonObject().setProperty("Value2", new JsonNumber(2)))
                        .addElement(new JsonObject().setProperty("Value3", new JsonNumber(3))))
                .toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectArrayNumbersJSON() throws IOException { //Convert int values into a JsonElement type, then add to JsonObject. Not sure if this is correct way to handle it?
        String expected = "{\"intList\":[3,1,4,1,5,9]}";
        int[] values = new int[] { 3, 1, 4, 1, 5, 9 };
        JsonArray intList = new JsonArray();
        for (int a : values) {
            intList.addElement(new JsonNumber(a));
        }
        String actual = new JsonObject().setProperty("intList", intList).toJsonString();
        assertEquals(expected, actual);
    }

    @Test
    public void complexJSONIsolated() throws IOException {
        String expected = "{\"James\":\"Anderson\",\"Michael\":\"Campbell\",\"Mary\":\"Jones\",\"John\":\"Williams\"}";
        JsonObject jsonObj = new JsonObject();
        // Adding properties - showcasing addProperty.
        jsonObj.setProperty("James", new JsonString("Anderson"))
            .setProperty("Michael", new JsonString("Campbell"))
            .setProperty("Mary", new JsonString("Jones"))
            .setProperty("John", new JsonString("Williams"));
        assertEquals(expected, jsonObj.toJsonString());
    }

    @Test
    public void complexJSONIsolatedEdited() throws IOException {
        String expected = "{\"James\":\"Anderson\",\"Mary\":\"Jones\",\"John\":\"Williams\"}";
        JsonObject jsonObj = new JsonObject();
        // Adding properties - showcasing addProperty.
        jsonObj.setProperty("James", new JsonString("Anderson"))
            .setProperty("Michael", new JsonString("Campbell"))
            .setProperty("Mary", new JsonString("Jones"))
            .setProperty("John", new JsonString("Williams"));
        jsonObj.removeProperty("Michael");
        assertEquals(expected, jsonObj.toJsonString());
    }

    //    @Test
    //    //todo this needs rewriting because we cannot chain removes anymore
    //    public void complexJSONChaining() throws IOException {
    //        String expected = "{\"Mary\":\"Jones\",\"John\":\"Williams\"}";
    //        JsonObject jsonObj = new JsonObject();
    //        jsonObj.setProperty("James", new JsonString("Anderson"))
    //            .setProperty("Michael", new JsonString("Campbell"))
    //            .setProperty("Mary", new JsonString("Jones"))
    //            .removeProperty("Michael")
    //            .setProperty("John", new JsonString("Williams"))
    //            .removeProperty("James");
    //        assertEquals(expected, jsonObj.toJson());
    //    }

    @Test
    public void complexJSONNesting() throws IOException {
        String expected
            = "{\"James\":{\"Country\":\"New Zealand\",\"Surname\":\"Anderson\"},\"Michael\":{\"Country\":\"Australia\",\"Surname\":\"Campbell\"},\"Mary\":{\"Country\":\"Canada\",\"Surname\":\"Jones\"},\"John\":{\"Country\":\"Australia\",\"Surname\":\"Williams\"}}";
        JsonObject jsonObj = new JsonObject();

        jsonObj
            .setProperty("James",
                new JsonObject().setProperty("Country", new JsonString("New Zealand"))
                    .setProperty("Surname", new JsonString("Anderson")))
            .setProperty("Michael",
                new JsonObject().setProperty("Country", new JsonString("Australia"))
                    .setProperty("Surname", new JsonString("Campbell")))
            .setProperty("Mary",
                new JsonObject().setProperty("Country", new JsonString("Canada"))
                    .setProperty("Surname", new JsonString("Jones")))
            .setProperty("John", new JsonObject().setProperty("Country", new JsonString("Australia"))
                .setProperty("Surname", new JsonString("Williams")));
        assertEquals(expected, jsonObj.toJsonString());
    }

    @Test
    public void complexJSONArrays() throws IOException {
        String expected
            = "[\"New Zealand\",\"Australia\",\"United States\",\"England\",\"Ireland\",\"South Africa\",\"Canada\"]";
        List<String> countries = new ArrayList<>();
        String[] countryExamples
            = { "New Zealand", "Australia", "United States", "England", "Ireland", "South Africa", "Canada" };
        Collections.addAll(countries, countryExamples);

        JsonArray countryArray = new JsonArray();
        for (String country : countries) {
            countryArray.addElement(new JsonString(country));
        }
        assertEquals(expected, countryArray.toJsonString());
    }

    //    @Test
    // todo this test needs to be rewritten as remove can no longer be chained like this
    //    public void complexJSONArraysChaining() throws IOException {
    //        String expected = "[\"New Zealand\",\"Canada\"]";
    //        JsonArray countryArray = new JsonArray();
    //        countryArray.addElement(new JsonString("New Zealand"))
    //            .addElement(new JsonString("Australia"))
    //            .addElement(new JsonString("Canada"))
    //            .removeElement(1)
    //            .addElement(new JsonString("United States"))
    //            .removeElement(2);
    //        assertEquals(expected, countryArray.toJson());
    //    }
}
