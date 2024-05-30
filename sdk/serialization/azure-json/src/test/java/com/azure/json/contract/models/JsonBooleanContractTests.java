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
 * Tests the contract of {@link JsonBoolean}.
 * <p>
 * All implementations of {@link JsonProvider} must create a subclass of this test class and pass all tests as they're
 * written to be considered an acceptable implementation.
 */
public abstract class JsonBooleanContractTests {
    /**
     * Creates an instance {@link JsonProvider} that will be used by a test.
     *
     * @return The {@link JsonProvider} that a test will use.
     */
    protected abstract JsonProvider getJsonProvider();

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
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
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
            try (JsonWriter jsonWriter = getJsonProvider().createWriter(writer, new JsonOptions())) {
                jsonBoolean.toJson(jsonWriter);
            }
            assertEquals(Boolean.toString(value), writer.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "10", "10.0", "\"hello\"", "[]", "{}" })
    public void invalidFromJsonStartingPoints(String json) throws IOException {
        try (JsonReader reader = getJsonProvider().createReader(json, new JsonOptions())) {
            assertThrows(IllegalStateException.class, () -> JsonBoolean.fromJson(reader));
        }
    }
}
