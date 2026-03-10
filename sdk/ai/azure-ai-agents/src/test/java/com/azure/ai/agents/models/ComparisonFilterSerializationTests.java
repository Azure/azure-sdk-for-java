// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for ComparisonFilter serialization, focusing on the value union type handling.
 * The value property is a union type: string | number | boolean.
 */
public class ComparisonFilterSerializationTests {

    /**
     * Tests serialization with a String value.
     */
    @Test
    public void testSerializationWithStringValue() throws IOException {
        ComparisonFilter filter = new ComparisonFilter(ComparisonFilterType.EQ, "status", "active");

        String json = serializeToJson(filter);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"eq\""));
        assertTrue(json.contains("\"key\":\"status\""));
        assertTrue(json.contains("\"value\""));
        assertTrue(json.contains("active"));
    }

    /**
     * Tests serialization with a numeric value.
     */
    @Test
    public void testSerializationWithNumericValue() throws IOException {
        ComparisonFilter filter = new ComparisonFilter(ComparisonFilterType.GT, "score", 95.5);

        String json = serializeToJson(filter);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"gt\""));
        assertTrue(json.contains("\"key\":\"score\""));
        assertTrue(json.contains("\"value\""));
        assertTrue(json.contains("95.5"));
    }

    /**
     * Tests serialization with a boolean value.
     */
    @Test
    public void testSerializationWithBooleanValue() throws IOException {
        ComparisonFilter filter = new ComparisonFilter(ComparisonFilterType.EQ, "enabled", true);

        String json = serializeToJson(filter);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"eq\""));
        assertTrue(json.contains("\"key\":\"enabled\""));
        assertTrue(json.contains("\"value\""));
        assertTrue(json.contains("true"));
    }

    /**
     * Tests serialization with a false boolean value.
     */
    @Test
    public void testSerializationWithFalseBooleanValue() throws IOException {
        ComparisonFilter filter = new ComparisonFilter(ComparisonFilterType.NE, "deleted", false);

        String json = serializeToJson(filter);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"ne\""));
        assertTrue(json.contains("\"key\":\"deleted\""));
        assertTrue(json.contains("false"));
    }

    /**
     * Tests serialization with an integer-like numeric value.
     */
    @Test
    public void testSerializationWithIntegerValue() throws IOException {
        ComparisonFilter filter = new ComparisonFilter(ComparisonFilterType.GTE, "count", 10.0);

        String json = serializeToJson(filter);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"gte\""));
        assertTrue(json.contains("\"key\":\"count\""));
        assertTrue(json.contains("10"));
    }

    /**
     * Tests deserialization with a String value.
     */
    @Test
    public void testDeserializationWithStringValue() throws IOException {
        String json = "{\"type\":\"eq\",\"key\":\"status\",\"value\":\"active\"}";

        ComparisonFilter filter = deserializeFromJson(json);

        assertNotNull(filter);
        assertEquals(ComparisonFilterType.EQ, filter.getType());
        assertEquals("status", filter.getKey());
        assertEquals("active", filter.getValueAsString());
    }

    /**
     * Tests deserialization with a numeric value.
     */
    @Test
    public void testDeserializationWithNumericValue() throws IOException {
        String json = "{\"type\":\"gt\",\"key\":\"score\",\"value\":95.5}";

        ComparisonFilter filter = deserializeFromJson(json);

        assertNotNull(filter);
        assertEquals(ComparisonFilterType.GT, filter.getType());
        assertEquals("score", filter.getKey());
        assertEquals(95.5, filter.getValueAsNumber());
    }

    /**
     * Tests deserialization with a boolean value.
     */
    @Test
    public void testDeserializationWithBooleanValue() throws IOException {
        String json = "{\"type\":\"eq\",\"key\":\"enabled\",\"value\":true}";

        ComparisonFilter filter = deserializeFromJson(json);

        assertNotNull(filter);
        assertEquals(ComparisonFilterType.EQ, filter.getType());
        assertEquals("enabled", filter.getKey());
        assertEquals(true, filter.getValueAsBoolean());
    }

    /**
     * Tests deserialization with a false boolean value.
     */
    @Test
    public void testDeserializationWithFalseBooleanValue() throws IOException {
        String json = "{\"type\":\"ne\",\"key\":\"deleted\",\"value\":false}";

        ComparisonFilter filter = deserializeFromJson(json);

        assertNotNull(filter);
        assertEquals(false, filter.getValueAsBoolean());
    }

    /**
     * Tests deserialization with an integer value.
     */
    @Test
    public void testDeserializationWithIntegerValue() throws IOException {
        String json = "{\"type\":\"gte\",\"key\":\"count\",\"value\":10}";

        ComparisonFilter filter = deserializeFromJson(json);

        assertNotNull(filter);
        assertNotNull(filter.getValueAsNumber());
        assertEquals(10.0, filter.getValueAsNumber());
    }

    /**
     * Tests round-trip with a String value.
     */
    @Test
    public void testRoundTripWithStringValue() throws IOException {
        ComparisonFilter original = new ComparisonFilter(ComparisonFilterType.EQ, "name", "test");

        String json = serializeToJson(original);
        ComparisonFilter deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getKey(), deserialized.getKey());
        assertEquals("test", deserialized.getValueAsString());
    }

    /**
     * Tests round-trip with a numeric value.
     */
    @Test
    public void testRoundTripWithNumericValue() throws IOException {
        ComparisonFilter original = new ComparisonFilter(ComparisonFilterType.LT, "price", 29.99);

        String json = serializeToJson(original);
        ComparisonFilter deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getKey(), deserialized.getKey());
        assertEquals(29.99, deserialized.getValueAsNumber());
    }

    /**
     * Tests round-trip with a boolean value.
     */
    @Test
    public void testRoundTripWithBooleanValue() throws IOException {
        ComparisonFilter original = new ComparisonFilter(ComparisonFilterType.EQ, "active", true);

        String json = serializeToJson(original);
        ComparisonFilter deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getKey(), deserialized.getKey());
        assertEquals(true, deserialized.getValueAsBoolean());
    }

    /**
     * Tests all comparison filter types with different value types.
     */
    @Test
    public void testLteFilterWithNumericValue() throws IOException {
        ComparisonFilter filter = new ComparisonFilter(ComparisonFilterType.LTE, "age", 65.0);

        String json = serializeToJson(filter);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"lte\""));
        assertTrue(json.contains("\"key\":\"age\""));
    }

    /**
     * Tests getValueAsString returns null when value is null.
     */
    @Test
    public void testGetValueAsStringReturnsNullWhenNull() throws IOException {
        String json = "{\"type\":\"eq\",\"key\":\"test\",\"value\":null}";

        ComparisonFilter filter = deserializeFromJson(json);

        assertNotNull(filter);
        assertNull(filter.getValueAsString());
        assertNull(filter.getValueAsNumber());
        assertNull(filter.getValueAsBoolean());
    }

    // Helper method to serialize to JSON string
    private String serializeToJson(ComparisonFilter filter) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            filter.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    // Helper method to deserialize from JSON string
    private ComparisonFilter deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return ComparisonFilter.fromJson(jsonReader);
        }
    }
}
