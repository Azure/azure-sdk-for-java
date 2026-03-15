// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for MCP call lifecycle events:
 * {@link ServerEventResponseMcpCallInProgress},
 * {@link ServerEventResponseMcpCallCompleted},
 * {@link ServerEventResponseMcpCallFailed}.
 */
class ServerEventResponseMcpCallLifecycleTest {

    @Test
    void testServerEventResponseMcpCallInProgress() {
        // Arrange
        String json
            = "{\"type\":\"response.mcp_call.in_progress\",\"event_id\":\"event123\",\"item_id\":\"item456\",\"output_index\":0}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ServerEventResponseMcpCallInProgress event = data.toObject(ServerEventResponseMcpCallInProgress.class);

        // Assert
        assertNotNull(event);
        assertEquals(ServerEventType.RESPONSE_MCP_CALL_IN_PROGRESS, event.getType());
        assertEquals("event123", event.getEventId());
        assertEquals("item456", event.getItemId());
        assertEquals(0, event.getOutputIndex());
    }

    @Test
    void testServerEventResponseMcpCallCompleted() {
        // Arrange
        String json
            = "{\"type\":\"response.mcp_call.completed\",\"event_id\":\"event789\",\"item_id\":\"item012\",\"output_index\":1}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ServerEventResponseMcpCallCompleted event = data.toObject(ServerEventResponseMcpCallCompleted.class);

        // Assert
        assertNotNull(event);
        assertEquals(ServerEventType.RESPONSE_MCP_CALL_COMPLETED, event.getType());
        assertEquals("event789", event.getEventId());
        assertEquals("item012", event.getItemId());
        assertEquals(1, event.getOutputIndex());
    }

    @Test
    void testServerEventResponseMcpCallFailed() {
        // Arrange
        String json
            = "{\"type\":\"response.mcp_call.failed\",\"event_id\":\"event345\",\"item_id\":\"item678\",\"output_index\":2}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ServerEventResponseMcpCallFailed event = data.toObject(ServerEventResponseMcpCallFailed.class);

        // Assert
        assertNotNull(event);
        assertEquals(ServerEventType.RESPONSE_MCP_CALL_FAILED, event.getType());
        assertEquals("event345", event.getEventId());
        assertEquals("item678", event.getItemId());
        assertEquals(2, event.getOutputIndex());
    }

    @Test
    void testInProgressJsonRoundTrip() {
        // Arrange
        String json
            = "{\"type\":\"response.mcp_call.in_progress\",\"event_id\":\"evt1\",\"item_id\":\"itm1\",\"output_index\":0}";
        BinaryData originalData = BinaryData.fromString(json);
        ServerEventResponseMcpCallInProgress event = originalData.toObject(ServerEventResponseMcpCallInProgress.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(event);
        ServerEventResponseMcpCallInProgress deserialized
            = serialized.toObject(ServerEventResponseMcpCallInProgress.class);

        // Assert
        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
    }

    @Test
    void testCompletedJsonRoundTrip() {
        // Arrange
        String json
            = "{\"type\":\"response.mcp_call.completed\",\"event_id\":\"evt2\",\"item_id\":\"itm2\",\"output_index\":3}";
        BinaryData originalData = BinaryData.fromString(json);
        ServerEventResponseMcpCallCompleted event = originalData.toObject(ServerEventResponseMcpCallCompleted.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(event);
        ServerEventResponseMcpCallCompleted deserialized
            = serialized.toObject(ServerEventResponseMcpCallCompleted.class);

        // Assert
        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
    }

    @Test
    void testFailedJsonRoundTrip() {
        // Arrange
        String json
            = "{\"type\":\"response.mcp_call.failed\",\"event_id\":\"evt3\",\"item_id\":\"itm3\",\"output_index\":5}";
        BinaryData originalData = BinaryData.fromString(json);
        ServerEventResponseMcpCallFailed event = originalData.toObject(ServerEventResponseMcpCallFailed.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(event);
        ServerEventResponseMcpCallFailed deserialized = serialized.toObject(ServerEventResponseMcpCallFailed.class);

        // Assert
        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
    }

    @Test
    void testMultipleOutputIndices() {
        // Test with various output indices
        for (int i = 0; i < 10; i++) {
            String json = String.format(
                "{\"type\":\"response.mcp_call.in_progress\",\"event_id\":\"evt%d\",\"item_id\":\"itm%d\",\"output_index\":%d}",
                i, i, i);
            BinaryData data = BinaryData.fromString(json);
            ServerEventResponseMcpCallInProgress event = data.toObject(ServerEventResponseMcpCallInProgress.class);

            assertEquals(i, event.getOutputIndex());
        }
    }
}
