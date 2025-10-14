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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JsonBoolean}.
 */
public class JsonBooleanTests {
    @Test
    public void kindCheck() {
        JsonElement element = JsonBoolean.getInstance(true);
        assertTrue(element.isBoolean());
        assertFalse(element.isArray());
        assertFalse(element.isObject());
        assertFalse(element.isString());
        assertFalse(element.isNumber());
        assertFalse(element.isNull());
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "false" })
    public void fromJson(String json) throws IOException {
        try (JsonReader reader = JsonReader.fromString(json)) {
            JsonBoolean jsonBoolean = JsonBoolean.fromJson(reader);
            assertEquals(json, jsonBoolean.toJsonString());
            if ("true".equals(json)) {
                assertTrue(jsonBoolean.getValue());
                assertSame(JsonBoolean.getInstance(true), jsonBoolean);
            } else {
                assertFalse(jsonBoolean.getValue());
                assertSame(JsonBoolean.getInstance(false), jsonBoolean);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void toJson(boolean value) throws IOException {
        JsonBoolean jsonBoolean = JsonBoolean.getInstance(value);
        try (StringBuilderWriter writer = new StringBuilderWriter()) {
            try (JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
                jsonBoolean.toJson(jsonWriter);
            }
            assertEquals(Boolean.toString(value), writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "10", "10.0", "\"hello\"", "[]", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = JsonReader.fromString(json)) {
            assertThrows(IllegalStateException.class, () -> JsonBoolean.fromJson(reader));
        }
    }
}
