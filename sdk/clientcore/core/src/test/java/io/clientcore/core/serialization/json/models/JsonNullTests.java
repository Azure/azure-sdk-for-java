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
 * Tests {@link JsonNull}.
 */
public class JsonNullTests {
    @Test
    public void kindCheck() {
        JsonElement element = JsonNull.getInstance();
        assertTrue(element.isNull());
        assertFalse(element.isArray());
        assertFalse(element.isObject());
        assertFalse(element.isString());
        assertFalse(element.isNumber());
        assertFalse(element.isBoolean());
    }

    @Test
    public void fromJson() throws IOException {
        try (JsonReader reader = JsonReader.fromString("null")) {
            JsonNull jsonNull = JsonNull.fromJson(reader);
            assertEquals("null", jsonNull.toJsonString());
            assertSame(JsonNull.getInstance(), jsonNull);
        }
    }

    @Test
    public void toJson() throws IOException {
        JsonNull jsonNull = JsonNull.getInstance();
        try (StringBuilderWriter writer = new StringBuilderWriter()) {
            try (JsonWriter jsonWriter = JsonWriter.toWriter(writer)) {
                jsonNull.toJson(jsonWriter);
            }
            assertEquals("null", writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "10", "10.0", "\"hello\"", "[]", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = JsonReader.fromString(json)) {
            assertThrows(IllegalStateException.class, () -> JsonNull.fromJson(reader));
        }
    }
}
