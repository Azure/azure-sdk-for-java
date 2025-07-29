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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JsonObject}.
 */
public class JsonObjectTests {
    @Test
    public void kindCheck() {
        JsonElement element = new JsonObject();
        assertTrue(element.isObject());
        assertFalse(element.isArray());
        assertFalse(element.isNumber());
        assertFalse(element.isString());
        assertFalse(element.isBoolean());
        assertFalse(element.isNull());
    }

    @Test
    public void emptyObjectFromJson() throws IOException {
        String json = "{}";
        try (JsonReader reader = JsonReader.fromString(json)) {
            JsonObject jsonObject = JsonObject.fromJson(reader);
            assertEquals(0, jsonObject.size());
        }
    }

    @Test
    public void objectOfBasicTypesFromJson() throws IOException {
        String json = "{\"string\":\"string\",\"null\":null,\"integer\":10,\"float\":10.0,\"boolean\":true}";
        try (JsonReader reader = JsonReader.fromString(json)) {
            JsonObject jsonObject = JsonObject.fromJson(reader);
            assertEquals(5, jsonObject.size());

            JsonElement stringElement = jsonObject.getProperty("string");
            assertTrue(stringElement.isString());
            assertEquals("\"string\"", stringElement.toJsonString());

            JsonElement nullElement = jsonObject.getProperty("null");
            assertTrue(nullElement.isNull());

            JsonElement intElement = jsonObject.getProperty("integer");
            assertTrue(intElement.isNumber());
            assertEquals("10", intElement.toJsonString());

            JsonElement doubleElement = jsonObject.getProperty("float");
            assertTrue(doubleElement.isNumber());
            assertEquals("10.0", doubleElement.toJsonString());

            JsonElement booleanElement = jsonObject.getProperty("boolean");
            assertTrue(booleanElement.isBoolean());
            assertEquals("true", booleanElement.toJsonString());

            assertEquals(json, jsonObject.toJsonString());

            assertEquals("\"string\"", jsonObject.removeProperty("string").toJsonString());
            assertEquals(4, jsonObject.size());
            assertNull(jsonObject.removeProperty("string"));
            assertEquals("{\"null\":null,\"integer\":10,\"float\":10.0,\"boolean\":true}", jsonObject.toJsonString());
        }
    }

    @Test
    public void emptyObjectToJson() throws IOException {
        JsonObject jsonObject = new JsonObject();
        assertEquals(0, jsonObject.size());

        try (StringBuilderWriter writer = new StringBuilderWriter();
            JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
            jsonObject.toJson(jsonWriter);
            jsonWriter.flush();

            assertEquals("{}", writer.toString());
        }
    }

    @Test
    public void objectOfBasicTypesToJson() throws IOException {
        JsonObject jsonObject = new JsonObject().setProperty("string", new JsonString("string"))
            .setProperty("null", JsonNull.getInstance())
            .setProperty("integer", new JsonNumber(10))
            .setProperty("float", new JsonNumber(10.0D))
            .setProperty("boolean", JsonBoolean.getInstance(true));

        assertEquals(5, jsonObject.size());

        try (StringBuilderWriter writer = new StringBuilderWriter();
            JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
            jsonObject.toJson(jsonWriter);
            jsonWriter.flush();

            assertEquals("{\"string\":\"string\",\"null\":null,\"integer\":10,\"float\":10.0,\"boolean\":true}",
                writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "null", "1", "1.0", "\"hello\"", "[]" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = JsonReader.fromString(json)) {
            assertThrows(IllegalStateException.class, () -> JsonObject.fromJson(reader));
        }
    }
}
