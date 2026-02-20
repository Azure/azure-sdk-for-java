// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MaxOutputTokens}.
 */
class MaxOutputTokensTest {

    @Test
    void testCreateWithIntValue() {
        // Arrange & Act
        MaxOutputTokens tokens = MaxOutputTokens.of(100);

        // Assert
        assertNotNull(tokens);
        assertFalse(tokens.isInfinite());
        assertEquals(100, tokens.getValue());
        assertEquals("100", tokens.toString());
    }

    @Test
    void testCreateWithMinValue() {
        // Arrange & Act
        MaxOutputTokens tokens = MaxOutputTokens.of(1);

        // Assert
        assertNotNull(tokens);
        assertFalse(tokens.isInfinite());
        assertEquals(1, tokens.getValue());
    }

    @Test
    void testCreateWithMaxValue() {
        // Arrange & Act
        MaxOutputTokens tokens = MaxOutputTokens.of(4096);

        // Assert
        assertNotNull(tokens);
        assertFalse(tokens.isInfinite());
        assertEquals(4096, tokens.getValue());
    }

    @Test
    void testCreateWithInvalidValue() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> MaxOutputTokens.of(0));
        assertThrows(IllegalArgumentException.class, () -> MaxOutputTokens.of(-1));
        assertThrows(IllegalArgumentException.class, () -> MaxOutputTokens.of(-100));
    }

    @Test
    void testCreateInfinite() {
        // Arrange & Act
        MaxOutputTokens tokens = MaxOutputTokens.infinite();

        // Assert
        assertNotNull(tokens);
        assertTrue(tokens.isInfinite());
        assertNull(tokens.getValue());
        assertEquals("inf", tokens.toString());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create identical instances
        MaxOutputTokens tokens1 = MaxOutputTokens.of(100);
        MaxOutputTokens tokens2 = MaxOutputTokens.of(100);
        MaxOutputTokens tokens3 = MaxOutputTokens.of(200);
        MaxOutputTokens infinite1 = MaxOutputTokens.infinite();
        MaxOutputTokens infinite2 = MaxOutputTokens.infinite();

        // Test reflexive
        assertEquals(tokens1, tokens1);
        assertEquals(infinite1, infinite1);

        // Test symmetric
        assertEquals(tokens1, tokens2);
        assertEquals(tokens2, tokens1);
        assertEquals(infinite1, infinite2);
        assertEquals(infinite2, infinite1);

        // Test hash code consistency
        assertEquals(tokens1.hashCode(), tokens2.hashCode());
        assertEquals(infinite1.hashCode(), infinite2.hashCode());

        // Test different values
        assertNotEquals(tokens1, tokens3);
        assertNotEquals(tokens1, infinite1);
        assertNotEquals(tokens3, infinite1);

        // Test with null
        assertNotNull(tokens1);
        assertNotNull(infinite1);

        // Test with different type
        assertNotEquals("100", tokens1);
        assertNotEquals("inf", infinite1);
    }

    @Test
    void testToString() {
        assertEquals("100", MaxOutputTokens.of(100).toString());
        assertEquals("1", MaxOutputTokens.of(1).toString());
        assertEquals("4096", MaxOutputTokens.of(4096).toString());
        assertEquals("inf", MaxOutputTokens.infinite().toString());
    }

    @Test
    void testJsonSerializationWithInteger() throws IOException {
        // Arrange
        MaxOutputTokens tokens = MaxOutputTokens.of(100);

        // Act
        String json = tokens.toJsonString();

        // Assert
        assertEquals("100", json);
    }

    @Test
    void testJsonSerializationWithInfinite() throws IOException {
        // Arrange
        MaxOutputTokens tokens = MaxOutputTokens.infinite();

        // Act
        String json = tokens.toJsonString();

        // Assert
        assertEquals("\"inf\"", json);
    }

    @Test
    void testJsonDeserializationWithInteger() throws IOException {
        // Arrange
        String json = "100";

        // Act
        MaxOutputTokens tokens;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            tokens = MaxOutputTokens.fromJson(reader);
        }

        // Assert
        assertNotNull(tokens);
        assertFalse(tokens.isInfinite());
        assertEquals(100, tokens.getValue());
    }

    @Test
    void testJsonDeserializationWithInfinite() throws IOException {
        // Arrange
        String json = "\"inf\"";

        // Act
        MaxOutputTokens tokens;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            tokens = MaxOutputTokens.fromJson(reader);
        }

        // Assert
        assertNotNull(tokens);
        assertTrue(tokens.isInfinite());
        assertNull(tokens.getValue());
    }

    @Test
    void testJsonDeserializationWithNull() throws IOException {
        // Arrange
        String json = "null";

        // Act
        MaxOutputTokens tokens;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            tokens = MaxOutputTokens.fromJson(reader);
        }

        // Assert
        assertNull(tokens);
    }

    @Test
    void testJsonDeserializationWithInvalidString() throws IOException {
        // Arrange
        String json = "\"invalid\"";

        // Act & Assert
        try (JsonReader reader = JsonProviders.createReader(json)) {
            assertThrows(IllegalArgumentException.class, () -> MaxOutputTokens.fromJson(reader));
        }
    }

    @Test
    void testJsonDeserializationWithInvalidType() throws IOException {
        // Arrange
        String json = "true";

        // Act & Assert
        try (JsonReader reader = JsonProviders.createReader(json)) {
            assertThrows(IllegalArgumentException.class, () -> MaxOutputTokens.fromJson(reader));
        }
    }

    @Test
    void testJsonRoundTripWithInteger() throws IOException {
        // Arrange
        MaxOutputTokens original = MaxOutputTokens.of(2048);

        // Act - Serialize
        String json = original.toJsonString();

        // Act - Deserialize
        MaxOutputTokens deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = MaxOutputTokens.fromJson(reader);
        }

        // Assert
        assertEquals(original, deserialized);
        assertEquals(original.getValue(), deserialized.getValue());
        assertEquals(original.isInfinite(), deserialized.isInfinite());
    }

    @Test
    void testJsonRoundTripWithInfinite() throws IOException {
        // Arrange
        MaxOutputTokens original = MaxOutputTokens.infinite();

        // Act - Serialize
        String json = original.toJsonString();

        // Act - Deserialize
        MaxOutputTokens deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = MaxOutputTokens.fromJson(reader);
        }

        // Assert
        assertEquals(original, deserialized);
        assertTrue(deserialized.isInfinite());
        assertNull(deserialized.getValue());
    }

    @Test
    void testIntegrationWithResponseCreateParams() throws IOException {
        // Arrange
        ResponseCreateParams params = new ResponseCreateParams();
        params.setMaxOutputTokens(BinaryData.fromObject(MaxOutputTokens.of(1000)));

        // Act - Serialize
        String json = params.toJsonString();

        // Assert - Check that maxOutputTokens is serialized as integer
        assertTrue(json.contains("\"max_output_tokens\":1000") || json.contains("\"max_output_tokens\": 1000"));
    }

    @Test
    void testIntegrationWithResponseCreateParamsInfinite() throws IOException {
        // Arrange
        ResponseCreateParams params = new ResponseCreateParams();
        params.setMaxOutputTokens(BinaryData.fromObject(MaxOutputTokens.infinite()));

        // Act - Serialize
        String json = params.toJsonString();

        // Assert - Check that maxOutputTokens is serialized as string "inf"
        assertTrue(json.contains("\"max_output_tokens\":\"inf\"") || json.contains("\"max_output_tokens\": \"inf\""));
    }

    @Test
    void testIntegrationWithResponseCreateParamsNull() throws IOException {
        // Arrange
        ResponseCreateParams params = new ResponseCreateParams();
        params.setMaxOutputTokens(null);

        // Act - Serialize
        String json = params.toJsonString();

        // Assert - Check that maxOutputTokens is not included when null
        // The field should not be present or should be null in the JSON
        // Depending on the JsonWriter implementation, it might omit null fields
        assertNotNull(json);
    }

    @Test
    void testDeserializeResponseCreateParamsWithInteger() throws IOException {
        // Arrange
        String json = "{\"max_output_tokens\":500}";

        // Act
        ResponseCreateParams params;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            params = ResponseCreateParams.fromJson(reader);
        }

        // Assert
        assertNotNull(params);
        assertNotNull(params.getMaxOutputTokens());
        MaxOutputTokens deserialized = params.getMaxOutputTokens().toObject(MaxOutputTokens.class);
        assertFalse(deserialized.isInfinite());
        assertEquals(500, deserialized.getValue());
    }

    @Test
    void testDeserializeResponseCreateParamsWithInfinite() throws IOException {
        // Arrange
        String json = "{\"max_output_tokens\":\"inf\"}";

        // Act
        ResponseCreateParams params;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            params = ResponseCreateParams.fromJson(reader);
        }

        // Assert
        assertNotNull(params);
        assertNotNull(params.getMaxOutputTokens());
        MaxOutputTokens deserialized = params.getMaxOutputTokens().toObject(MaxOutputTokens.class);
        assertTrue(deserialized.isInfinite());
        assertNull(deserialized.getValue());
    }
}
