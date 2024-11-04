// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.contract.models;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.StringBuilderWriter;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonBoolean;
import com.azure.json.models.JsonElement;
import com.azure.json.models.JsonNull;
import com.azure.json.models.JsonNumber;
import com.azure.json.models.JsonString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the contract of {@link JsonArray}.
 * <p>
 * All implementations of {@link JsonProvider} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 */
public abstract class JsonArrayContractTests {
    /**
     * Creates an instance {@link JsonProvider} that will be used by a test.
     *
     * @return The {@link JsonProvider} that a test will use.
     */
    protected abstract JsonProvider getJsonProvider();

    @Test
    public void kindCheck() {
        JsonElement element = new JsonArray();
        assertTrue(element.isArray());
        assertFalse(element.isObject());
        assertFalse(element.isString());
        assertFalse(element.isNumber());
        assertFalse(element.isBoolean());
        assertFalse(element.isNull());
    }

    @Test
    public void emptyArrayFromJson() throws IOException {
        String json = "[]";
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            JsonArray jsonArray = JsonArray.fromJson(reader);
            assertEquals(0, jsonArray.size());
        }
    }

    @Test
    public void arrayOfBasicTypesFromJson() throws IOException {
        String json = "[\"string\",null,10,10.0,true]";
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            JsonArray jsonArray = JsonArray.fromJson(reader);

            JsonElement stringElement = jsonArray.getElement(0);
            assertTrue(stringElement.isString());
            assertEquals("\"string\"", stringElement.toJsonString());

            JsonElement nullElement = jsonArray.getElement(1);
            assertTrue(nullElement.isNull());

            JsonElement intElement = jsonArray.getElement(2);
            assertTrue(intElement.isNumber());
            assertEquals("10", intElement.toJsonString());

            JsonElement doubleElement = jsonArray.getElement(3);
            assertTrue(doubleElement.isNumber());
            assertEquals("10.0", doubleElement.toJsonString());

            JsonElement booleanElement = jsonArray.getElement(4);
            assertTrue(booleanElement.isBoolean());
            assertEquals("true", booleanElement.toJsonString());
        }
    }

    @Test
    public void emptyArrayToJson() throws IOException {
        JsonArray jsonArray = new JsonArray();
        assertEquals(0, jsonArray.size());

        try (StringBuilderWriter writer = new StringBuilderWriter();
            JsonWriter jsonWriter = getJsonProvider().createWriter(writer, new JsonOptions())) {
            jsonArray.toJson(jsonWriter);
            jsonWriter.flush();

            assertEquals("[]", writer.toString());
        }
    }

    @Test
    public void arrayOfBasicTypesToJson() throws IOException {
        JsonArray jsonArray = new JsonArray().addElement(new JsonString("string"))
            .addElement(JsonNull.getInstance())
            .addElement(new JsonNumber(10))
            .addElement(new JsonNumber(10.0D))
            .addElement(JsonBoolean.getInstance(true));

        assertEquals(5, jsonArray.size());

        try (StringBuilderWriter writer = new StringBuilderWriter();
            JsonWriter jsonWriter = getJsonProvider().createWriter(writer, new JsonOptions())) {
            jsonArray.toJson(jsonWriter);
            jsonWriter.flush();

            assertEquals("[\"string\",null,10,10.0,true]", writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "null", "10", "10.0", "\"hello\"", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            assertThrows(IllegalStateException.class, () -> JsonArray.fromJson(reader));
        }
    }
}
