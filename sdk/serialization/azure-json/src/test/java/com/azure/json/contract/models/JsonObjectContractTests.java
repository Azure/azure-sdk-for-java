// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.contract.models;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.StringBuilderWriter;
import com.azure.json.models.JsonBoolean;
import com.azure.json.models.JsonElement;
import com.azure.json.models.JsonNull;
import com.azure.json.models.JsonNumber;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;
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
 * Tests the contract of {@link JsonObject}.
 * <p>
 * All implementations of {@link JsonProvider} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 */
public abstract class JsonObjectContractTests {
    /**
     * Creates an instance {@link JsonProvider} that will be used by a test.
     *
     * @return The {@link JsonProvider} that a test will use.
     */
    protected abstract JsonProvider getJsonProvider();

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
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            JsonObject jsonObject = JsonObject.fromJson(reader);
            assertEquals(0, jsonObject.size());
        }
    }

    @Test
    public void objectOfBasicTypesFromJson() throws IOException {
        String json = "{\"string\":\"string\",\"null\":null,\"integer\":10,\"float\":10.0,\"boolean\":true}";
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
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
            JsonWriter jsonWriter = getJsonProvider().createWriter(writer, new JsonOptions())) {
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
            JsonWriter jsonWriter = getJsonProvider().createWriter(writer, new JsonOptions())) {
            jsonObject.toJson(jsonWriter);
            jsonWriter.flush();

            assertEquals("{\"string\":\"string\",\"null\":null,\"integer\":10,\"float\":10.0,\"boolean\":true}",
                writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "null", "1", "1.0", "\"hello\"", "[]" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            assertThrows(IllegalStateException.class, () -> JsonObject.fromJson(reader));
        }
    }
}
