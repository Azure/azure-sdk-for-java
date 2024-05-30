// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.contract.models;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.StringBuilderWriter;
import com.azure.json.models.JsonElement;
import com.azure.json.models.JsonString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the contract of {@link JsonString}.
 * <p>
 * All implementations of {@link JsonProvider} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 */
public abstract class JsonStringContractTests {
    /**
     * Creates an instance {@link JsonProvider} that will be used by a test.
     *
     * @return The {@link JsonProvider} that a test will use.
     */
    protected abstract JsonProvider getJsonProvider();

    @Test
    public void kindCheck() {
        JsonElement element = new JsonString("");
        assertTrue(element.isString());
        assertFalse(element.isArray());
        assertFalse(element.isObject());
        assertFalse(element.isNumber());
        assertFalse(element.isBoolean());
        assertFalse(element.isNull());
    }

    @ParameterizedTest
    @ValueSource(strings = { "\"\"", "\"hello\"" })
    public void fromJson(String json) throws IOException {
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            JsonString jsonString = JsonString.fromJson(reader);
            assertEquals(json, jsonString.toJsonString());
            assertEquals(json.substring(1, json.length() - 1), jsonString.getValue());
        }
    }

    @Test
    public void toJsonStringCachesValue() throws IOException {
        JsonString jsonString = new JsonString("hello");
        String json = jsonString.toJsonString();
        assertEquals("\"hello\"", json);
        assertSame(json, jsonString.toJsonString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "hello" })
    public void toJson(String value) throws IOException {
        JsonString jsonString = new JsonString(value);
        try (StringBuilderWriter writer = new StringBuilderWriter()) {
            try (JsonWriter jsonWriter = getJsonProvider().createWriter(writer, new JsonOptions())) {
                jsonString.toJson(jsonWriter);
            }
            assertEquals("\"" + value + "\"", writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "null", "1", "1.0", "[]", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            assertThrows(IllegalStateException.class, () -> JsonString.fromJson(reader));
        }
    }
}
