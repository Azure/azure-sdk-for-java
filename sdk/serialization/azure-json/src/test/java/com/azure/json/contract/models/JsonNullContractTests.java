// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.json.contract.models;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.StringBuilderWriter;
import com.azure.json.models.JsonElement;
import com.azure.json.models.JsonNull;
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
 * Tests the contract of {@link JsonNull}.
 * <p>
 * All implementations of {@link JsonProvider} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 */
public abstract class JsonNullContractTests {
    /**
     * Creates an instance {@link JsonProvider} that will be used by a test.
     *
     * @return The {@link JsonProvider} that a test will use.
     */
    protected abstract JsonProvider getJsonProvider();

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
        try (JsonReader reader = getJsonProvider().createReader("null", new JsonOptions())) {
            JsonNull jsonNull = JsonNull.fromJson(reader);
            assertEquals("null", jsonNull.toJsonString());
            assertSame(JsonNull.getInstance(), jsonNull);
        }
    }

    @Test
    public void toJson() throws IOException {
        JsonNull jsonNull = JsonNull.getInstance();
        try (StringBuilderWriter writer = new StringBuilderWriter()) {
            try (JsonWriter jsonWriter = getJsonProvider().createWriter(writer, new JsonOptions())) {
                jsonNull.toJson(jsonWriter);
            }
            assertEquals("null", writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "true", "10", "10.0", "\"hello\"", "[]", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            assertThrows(IllegalStateException.class, () -> JsonNull.fromJson(reader));
        }
    }
}
