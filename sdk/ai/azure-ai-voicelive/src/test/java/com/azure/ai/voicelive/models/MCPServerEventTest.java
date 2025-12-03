// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for MCP server event classes.
 */
class MCPServerEventTest {

    @Test
    void testServerEventMcpListToolsInProgress() throws IOException {
        // Arrange
        String json = "{" + "\"event_id\":\"evt-1\"," + "\"type\":\"mcp_list_tools.in_progress\","
            + "\"item_id\":\"item-123\"" + "}";

        // Act
        ServerEventMcpListToolsInProgress event
            = BinaryData.fromString(json).toObject(ServerEventMcpListToolsInProgress.class);

        // Assert
        assertNotNull(event);
        assertEquals("evt-1", event.getEventId());
        assertEquals(ServerEventType.MCP_LIST_TOOLS_IN_PROGRESS, event.getType());
        assertEquals("item-123", event.getItemId());
    }

    @Test
    void testServerEventMcpListToolsCompleted() throws IOException {
        // Arrange
        String json = "{" + "\"event_id\":\"evt-2\"," + "\"type\":\"mcp_list_tools.completed\","
            + "\"item_id\":\"item-456\"" + "}";

        // Act
        ServerEventMcpListToolsCompleted event
            = BinaryData.fromString(json).toObject(ServerEventMcpListToolsCompleted.class);

        // Assert
        assertNotNull(event);
        assertEquals("evt-2", event.getEventId());
        assertEquals(ServerEventType.MCP_LIST_TOOLS_COMPLETED, event.getType());
        assertEquals("item-456", event.getItemId());
    }

    @Test
    void testServerEventMcpListToolsFailed() throws IOException {
        // Arrange
        String json = "{" + "\"event_id\":\"evt-3\"," + "\"type\":\"mcp_list_tools.failed\","
            + "\"item_id\":\"item-789\"" + "}";

        // Act
        ServerEventMcpListToolsFailed event = BinaryData.fromString(json).toObject(ServerEventMcpListToolsFailed.class);

        // Assert
        assertNotNull(event);
        assertEquals("evt-3", event.getEventId());
        assertEquals(ServerEventType.MCP_LIST_TOOLS_FAILED, event.getType());
        assertEquals("item-789", event.getItemId());
    }

    @Test
    void testServerEventResponseMcpCallArgumentsDelta() throws IOException {
        // Arrange
        String json = "{" + "\"event_id\":\"evt-4\"," + "\"type\":\"response.mcp_call_arguments.delta\","
            + "\"item_id\":\"call-item-1\"," + "\"response_id\":\"resp-1\"," + "\"output_index\":0,"
            + "\"delta\":\"partial-data\"" + "}";

        // Act
        ServerEventResponseMcpCallArgumentsDelta event
            = BinaryData.fromString(json).toObject(ServerEventResponseMcpCallArgumentsDelta.class);

        // Assert
        assertNotNull(event);
        assertEquals("evt-4", event.getEventId());
        assertEquals(ServerEventType.RESPONSE_MCP_CALL_ARGUMENTS_DELTA, event.getType());
        assertEquals("call-item-1", event.getItemId());
        assertEquals("resp-1", event.getResponseId());
        assertEquals(0, event.getOutputIndex());
        assertEquals("partial-data", event.getDelta());
    }

    @Test
    void testServerEventResponseMcpCallArgumentsDeltaWithObfuscation() throws IOException {
        // Arrange
        String json = "{" + "\"event_id\":\"evt-5\"," + "\"type\":\"response.mcp_call_arguments.delta\","
            + "\"item_id\":\"call-item-2\"," + "\"response_id\":\"resp-2\"," + "\"output_index\":1,"
            + "\"delta\":\"value\"," + "\"obfuscation\":\"***\"" + "}";

        // Act
        ServerEventResponseMcpCallArgumentsDelta event
            = BinaryData.fromString(json).toObject(ServerEventResponseMcpCallArgumentsDelta.class);

        // Assert
        assertNotNull(event);
        assertEquals("value", event.getDelta());
        assertEquals("***", event.getObfuscation());
    }

    @Test
    void testServerEventResponseMcpCallArgumentsDone() throws IOException {
        // Arrange
        String json = "{" + "\"event_id\":\"evt-6\"," + "\"type\":\"response.mcp_call_arguments.done\","
            + "\"item_id\":\"call-item-3\"," + "\"response_id\":\"resp-3\"," + "\"output_index\":0,"
            + "\"arguments\":\"{\\\"param1\\\":\\\"value1\\\",\\\"param2\\\":42}\"" + "}";

        // Act
        ServerEventResponseMcpCallArgumentsDone event
            = BinaryData.fromString(json).toObject(ServerEventResponseMcpCallArgumentsDone.class);

        // Assert
        assertNotNull(event);
        assertEquals("evt-6", event.getEventId());
        assertEquals(ServerEventType.RESPONSE_MCP_CALL_ARGUMENTS_DONE, event.getType());
        assertEquals("call-item-3", event.getItemId());
        assertEquals("resp-3", event.getResponseId());
        assertEquals(0, event.getOutputIndex());
        assertEquals("{\"param1\":\"value1\",\"param2\":42}", event.getArguments());
    }

    @Test
    void testJsonRoundTripForInProgress() throws IOException {
        // Arrange
        String originalJson = "{" + "\"event_id\":\"round-trip-1\"," + "\"type\":\"mcp_list_tools.in_progress\","
            + "\"item_id\":\"item-rt\"" + "}";

        // Act
        ServerEventMcpListToolsInProgress event
            = BinaryData.fromString(originalJson).toObject(ServerEventMcpListToolsInProgress.class);
        String serializedJson = BinaryData.fromObject(event).toString();

        // Assert
        assertNotNull(serializedJson);
        ServerEventMcpListToolsInProgress deserializedEvent
            = BinaryData.fromString(serializedJson).toObject(ServerEventMcpListToolsInProgress.class);
        assertEquals(event.getEventId(), deserializedEvent.getEventId());
        assertEquals(event.getItemId(), deserializedEvent.getItemId());
        assertEquals(event.getType(), deserializedEvent.getType());
    }

    @Test
    void testJsonRoundTripForArgumentsDelta() throws IOException {
        // Arrange
        String originalJson = "{" + "\"event_id\":\"round-trip-2\"," + "\"type\":\"response.mcp_call_arguments.delta\","
            + "\"item_id\":\"item-delta\"," + "\"response_id\":\"resp-delta\"," + "\"output_index\":2,"
            + "\"delta\":\"test-delta\"" + "}";

        // Act
        ServerEventResponseMcpCallArgumentsDelta event
            = BinaryData.fromString(originalJson).toObject(ServerEventResponseMcpCallArgumentsDelta.class);
        String serializedJson = BinaryData.fromObject(event).toString();

        // Assert
        assertNotNull(serializedJson);
        ServerEventResponseMcpCallArgumentsDelta deserializedEvent
            = BinaryData.fromString(serializedJson).toObject(ServerEventResponseMcpCallArgumentsDelta.class);
        assertEquals(event.getEventId(), deserializedEvent.getEventId());
        assertEquals(event.getItemId(), deserializedEvent.getItemId());
        assertEquals(event.getDelta(), deserializedEvent.getDelta());
    }

    @Test
    void testComplexArgumentsInDone() throws IOException {
        // Arrange
        String complexArgs = "{\"nested\":{\"array\":[1,2,3]},\"string\":\"value\",\"bool\":true}";
        String json = "{" + "\"event_id\":\"evt-complex\"," + "\"type\":\"response.mcp_call_arguments.done\","
            + "\"item_id\":\"item-complex\"," + "\"response_id\":\"resp-complex\"," + "\"output_index\":0,"
            + "\"arguments\":\"" + complexArgs.replace("\"", "\\\"") + "\"" + "}";

        // Act
        ServerEventResponseMcpCallArgumentsDone event
            = BinaryData.fromString(json).toObject(ServerEventResponseMcpCallArgumentsDone.class);

        // Assert
        assertEquals(complexArgs, event.getArguments());
    }

    @Test
    void testMultipleDeltaEvents() throws IOException {
        // Simulate streaming multiple delta events
        String[] deltas = {
            "{\"event_id\":\"evt-7\",\"type\":\"response.mcp_call_arguments.delta\",\"item_id\":\"stream-1\",\"response_id\":\"resp-7\",\"output_index\":0,\"delta\":\"{\"}",
            "{\"event_id\":\"evt-8\",\"type\":\"response.mcp_call_arguments.delta\",\"item_id\":\"stream-1\",\"response_id\":\"resp-7\",\"output_index\":0,\"delta\":\"\\\"key\\\"\"}",
            "{\"event_id\":\"evt-9\",\"type\":\"response.mcp_call_arguments.delta\",\"item_id\":\"stream-1\",\"response_id\":\"resp-7\",\"output_index\":0,\"delta\":\":\"}",
            "{\"event_id\":\"evt-10\",\"type\":\"response.mcp_call_arguments.delta\",\"item_id\":\"stream-1\",\"response_id\":\"resp-7\",\"output_index\":0,\"delta\":\"\\\"value\\\"}\"}", };

        StringBuilder fullArguments = new StringBuilder();

        // Act
        for (String deltaJson : deltas) {
            ServerEventResponseMcpCallArgumentsDelta event
                = BinaryData.fromString(deltaJson).toObject(ServerEventResponseMcpCallArgumentsDelta.class);
            fullArguments.append(event.getDelta());
            assertEquals("stream-1", event.getItemId());
            assertEquals("resp-7", event.getResponseId());
        }

        // Assert
        assertEquals("{\"key\":\"value\"}", fullArguments.toString());
    }
}
