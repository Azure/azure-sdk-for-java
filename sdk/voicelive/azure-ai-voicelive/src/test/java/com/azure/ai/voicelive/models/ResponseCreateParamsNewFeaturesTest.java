// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for new features in {@link ResponseCreateParams} and {@link SessionResponse}:
 * - ReasoningEffort configuration
 * - Metadata support
 */
class ResponseCreateParamsNewFeaturesTest {

    @Test
    void testResponseCreateParamsReasoningEffort() {
        // Arrange
        ResponseCreateParams params = new ResponseCreateParams();

        // Act
        ResponseCreateParams result = params.setReasoningEffort(ReasoningEffort.HIGH);

        // Assert
        assertSame(params, result);
        assertEquals(ReasoningEffort.HIGH, params.getReasoningEffort());
    }

    @Test
    void testResponseCreateParamsMetadata() {
        // Arrange
        ResponseCreateParams params = new ResponseCreateParams();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user_id", "user123");
        metadata.put("session_type", "customer_support");

        // Act
        ResponseCreateParams result = params.setMetadata(metadata);

        // Assert
        assertSame(params, result);
        assertNotNull(params.getMetadata());
        assertEquals(2, params.getMetadata().size());
        assertEquals("user123", params.getMetadata().get("user_id"));
        assertEquals("customer_support", params.getMetadata().get("session_type"));
    }

    @Test
    void testResponseCreateParamsJsonSerialization() {
        // Arrange
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");

        ResponseCreateParams params
            = new ResponseCreateParams().setReasoningEffort(ReasoningEffort.MEDIUM).setMetadata(metadata);

        // Act
        BinaryData serialized = BinaryData.fromObject(params);
        ResponseCreateParams deserialized = serialized.toObject(ResponseCreateParams.class);

        // Assert
        assertEquals(params.getReasoningEffort(), deserialized.getReasoningEffort());
        assertEquals(params.getMetadata(), deserialized.getMetadata());
    }

    @Test
    void testResponseCreateParamsJsonDeserialization() {
        // Arrange
        String json = "{\"reasoning_effort\":\"low\",\"metadata\":{\"test_key\":\"test_value\"}}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ResponseCreateParams params = data.toObject(ResponseCreateParams.class);

        // Assert
        assertNotNull(params);
        assertEquals(ReasoningEffort.LOW, params.getReasoningEffort());
        assertNotNull(params.getMetadata());
        assertEquals("test_value", params.getMetadata().get("test_key"));
    }

    @Test
    void testSessionResponseMetadata() {
        // Arrange
        String json = "{\"id\":\"resp123\",\"status\":\"completed\","
            + "\"metadata\":{\"user\":\"user456\",\"context\":\"test\"}}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        SessionResponse response = data.toObject(SessionResponse.class);

        // Assert
        assertNotNull(response);
        assertEquals("resp123", response.getId());
        assertNotNull(response.getMetadata());
        assertEquals(2, response.getMetadata().size());
        assertEquals("user456", response.getMetadata().get("user"));
        assertEquals("test", response.getMetadata().get("context"));
    }

    @Test
    void testResponseCreateParamsMethodChaining() {
        // Arrange
        Map<String, String> metadata = new HashMap<>();
        metadata.put("chain_key", "chain_value");

        // Act
        ResponseCreateParams params = new ResponseCreateParams().setCommit(true)
            .setCancelPrevious(false)
            .setReasoningEffort(ReasoningEffort.XHIGH)
            .setMetadata(metadata)
            .setTemperature(0.9);

        // Assert
        assertEquals(true, params.isCommit());
        assertEquals(false, params.isCancelPrevious());
        assertEquals(ReasoningEffort.XHIGH, params.getReasoningEffort());
        assertEquals(metadata, params.getMetadata());
        assertEquals(0.9, params.getTemperature());
    }

    @Test
    void testEmptyMetadata() {
        // Arrange
        ResponseCreateParams params = new ResponseCreateParams().setMetadata(new HashMap<>());

        // Act
        BinaryData serialized = BinaryData.fromObject(params);
        ResponseCreateParams deserialized = serialized.toObject(ResponseCreateParams.class);

        // Assert
        assertNotNull(deserialized.getMetadata());
        assertEquals(0, deserialized.getMetadata().size());
    }
}
