// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization.json.models;

import io.clientcore.core.serialization.json.JsonOptions;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonWriter;
import io.clientcore.core.serialization.json.implementation.StringBuilderWriter;
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
 * Tests {@link JsonString}.
 */
public class JsonStringTests {
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
        try (JsonReader reader = JsonReader.fromString(json)) {
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
            try (JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
                jsonString.toJson(jsonWriter);
            }
            assertEquals("\"" + value + "\"", writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "null", "1", "1.0", "[]", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = JsonReader.fromString(json, new JsonOptions())) {
            assertThrows(IllegalStateException.class, () -> JsonString.fromJson(reader));
        }
    }
}
