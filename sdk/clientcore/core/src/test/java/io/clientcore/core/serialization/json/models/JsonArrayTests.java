// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization.json.models;

import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.serialization.json.implementation.StringBuilderWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JsonArray}.
 */
public class JsonArrayTests {
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
        try (JsonReader reader = JsonReader.fromString(json)) {
            JsonArray jsonArray = JsonArray.fromJson(reader);
            assertEquals(0, jsonArray.size());
        }
    }

    @Test
    public void arrayOfBasicTypesFromJson() throws IOException {
        String json = "[\"string\",null,10,10.0,true]";
        try (JsonReader reader = JsonReader.fromString(json)) {
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
            JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
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
            JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
            jsonArray.toJson(jsonWriter);
            jsonWriter.flush();

            assertEquals("[\"string\",null,10,10.0,true]", writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "null", "10", "10.0", "\"hello\"", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = JsonReader.fromString(json)) {
            assertThrows(IllegalStateException.class, () -> JsonArray.fromJson(reader));
        }
    }
}
