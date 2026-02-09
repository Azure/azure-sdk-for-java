// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for Foundry agent call lifecycle events:
 * {@link ServerEventResponseFoundryAgentCallArgumentsDelta},
 * {@link ServerEventResponseFoundryAgentCallArgumentsDone},
 * {@link ServerEventResponseFoundryAgentCallInProgress},
 * {@link ServerEventResponseFoundryAgentCallCompleted},
 * {@link ServerEventResponseFoundryAgentCallFailed}.
 */
class ServerEventResponseFoundryAgentCallLifecycleTest {

    @Test
    void testServerEventResponseFoundryAgentCallArgumentsDelta() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call_arguments.delta\",\"event_id\":\"event123\","
            + "\"item_id\":\"item456\",\"response_id\":\"resp789\",\"output_index\":0,\"delta\":\"partial_args\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ServerEventResponseFoundryAgentCallArgumentsDelta event
            = data.toObject(ServerEventResponseFoundryAgentCallArgumentsDelta.class);

        // Assert
        assertNotNull(event);
        assertEquals(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_ARGUMENTS_DELTA, event.getType());
        assertEquals("event123", event.getEventId());
        assertEquals("item456", event.getItemId());
        assertEquals("resp789", event.getResponseId());
        assertEquals(0, event.getOutputIndex());
        assertEquals("partial_args", event.getDelta());
    }

    @Test
    void testServerEventResponseFoundryAgentCallArgumentsDone() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call_arguments.done\",\"event_id\":\"event456\","
            + "\"item_id\":\"item789\",\"response_id\":\"resp012\",\"output_index\":1,\"arguments\":\"{\\\"key\\\":\\\"value\\\"}\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ServerEventResponseFoundryAgentCallArgumentsDone event
            = data.toObject(ServerEventResponseFoundryAgentCallArgumentsDone.class);

        // Assert
        assertNotNull(event);
        assertEquals(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_ARGUMENTS_DONE, event.getType());
        assertEquals("event456", event.getEventId());
        assertEquals("item789", event.getItemId());
        assertEquals("resp012", event.getResponseId());
        assertEquals(1, event.getOutputIndex());
        assertEquals("{\"key\":\"value\"}", event.getArguments());
    }

    @Test
    void testServerEventResponseFoundryAgentCallInProgress() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call.in_progress\",\"event_id\":\"event789\","
            + "\"item_id\":\"item012\",\"output_index\":2,\"agent_response_id\":\"agent_resp_123\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ServerEventResponseFoundryAgentCallInProgress event
            = data.toObject(ServerEventResponseFoundryAgentCallInProgress.class);

        // Assert
        assertNotNull(event);
        assertEquals(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_IN_PROGRESS, event.getType());
        assertEquals("event789", event.getEventId());
        assertEquals("item012", event.getItemId());
        assertEquals(2, event.getOutputIndex());
        assertEquals("agent_resp_123", event.getAgentResponseId());
    }

    @Test
    void testServerEventResponseFoundryAgentCallCompleted() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call.completed\",\"event_id\":\"event012\","
            + "\"item_id\":\"item345\",\"output_index\":3}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ServerEventResponseFoundryAgentCallCompleted event
            = data.toObject(ServerEventResponseFoundryAgentCallCompleted.class);

        // Assert
        assertNotNull(event);
        assertEquals(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_COMPLETED, event.getType());
        assertEquals("event012", event.getEventId());
        assertEquals("item345", event.getItemId());
        assertEquals(3, event.getOutputIndex());
    }

    @Test
    void testServerEventResponseFoundryAgentCallFailed() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call.failed\",\"event_id\":\"event345\","
            + "\"item_id\":\"item678\",\"output_index\":4}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ServerEventResponseFoundryAgentCallFailed event
            = data.toObject(ServerEventResponseFoundryAgentCallFailed.class);

        // Assert
        assertNotNull(event);
        assertEquals(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_FAILED, event.getType());
        assertEquals("event345", event.getEventId());
        assertEquals("item678", event.getItemId());
        assertEquals(4, event.getOutputIndex());
    }

    @Test
    void testArgumentsDeltaJsonRoundTrip() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call_arguments.delta\",\"event_id\":\"evt1\","
            + "\"item_id\":\"itm1\",\"response_id\":\"resp1\",\"output_index\":0,\"delta\":\"test_delta\"}";
        BinaryData originalData = BinaryData.fromString(json);
        ServerEventResponseFoundryAgentCallArgumentsDelta event
            = originalData.toObject(ServerEventResponseFoundryAgentCallArgumentsDelta.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(event);
        ServerEventResponseFoundryAgentCallArgumentsDelta deserialized
            = serialized.toObject(ServerEventResponseFoundryAgentCallArgumentsDelta.class);

        // Assert
        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getResponseId(), deserialized.getResponseId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
        assertEquals(event.getDelta(), deserialized.getDelta());
    }

    @Test
    void testArgumentsDoneJsonRoundTrip() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call_arguments.done\",\"event_id\":\"evt2\","
            + "\"item_id\":\"itm2\",\"response_id\":\"resp2\",\"output_index\":1,\"arguments\":\"{}\"}";
        BinaryData originalData = BinaryData.fromString(json);
        ServerEventResponseFoundryAgentCallArgumentsDone event
            = originalData.toObject(ServerEventResponseFoundryAgentCallArgumentsDone.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(event);
        ServerEventResponseFoundryAgentCallArgumentsDone deserialized
            = serialized.toObject(ServerEventResponseFoundryAgentCallArgumentsDone.class);

        // Assert
        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getResponseId(), deserialized.getResponseId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
        assertEquals(event.getArguments(), deserialized.getArguments());
    }

    @Test
    void testInProgressJsonRoundTrip() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call.in_progress\",\"event_id\":\"evt3\","
            + "\"item_id\":\"itm3\",\"output_index\":2,\"agent_response_id\":\"ar3\"}";
        BinaryData originalData = BinaryData.fromString(json);
        ServerEventResponseFoundryAgentCallInProgress event
            = originalData.toObject(ServerEventResponseFoundryAgentCallInProgress.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(event);
        ServerEventResponseFoundryAgentCallInProgress deserialized
            = serialized.toObject(ServerEventResponseFoundryAgentCallInProgress.class);

        // Assert
        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
        assertEquals(event.getAgentResponseId(), deserialized.getAgentResponseId());
    }

    @Test
    void testCompletedJsonRoundTrip() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call.completed\",\"event_id\":\"evt4\","
            + "\"item_id\":\"itm4\",\"output_index\":3}";
        BinaryData originalData = BinaryData.fromString(json);
        ServerEventResponseFoundryAgentCallCompleted event
            = originalData.toObject(ServerEventResponseFoundryAgentCallCompleted.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(event);
        ServerEventResponseFoundryAgentCallCompleted deserialized
            = serialized.toObject(ServerEventResponseFoundryAgentCallCompleted.class);

        // Assert
        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
    }

    @Test
    void testFailedJsonRoundTrip() {
        // Arrange
        String json = "{\"type\":\"response.foundry_agent_call.failed\",\"event_id\":\"evt5\","
            + "\"item_id\":\"itm5\",\"output_index\":4}";
        BinaryData originalData = BinaryData.fromString(json);
        ServerEventResponseFoundryAgentCallFailed event
            = originalData.toObject(ServerEventResponseFoundryAgentCallFailed.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(event);
        ServerEventResponseFoundryAgentCallFailed deserialized
            = serialized.toObject(ServerEventResponseFoundryAgentCallFailed.class);

        // Assert
        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getEventId(), deserialized.getEventId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
    }

    @Test
    void testServerEventTypeFoundryAgentCallValues() {
        // Assert all Foundry agent call event types exist
        assertNotNull(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_ARGUMENTS_DELTA);
        assertNotNull(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_ARGUMENTS_DONE);
        assertNotNull(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_IN_PROGRESS);
        assertNotNull(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_COMPLETED);
        assertNotNull(ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_FAILED);

        // Assert correct string values
        assertEquals("response.foundry_agent_call_arguments.delta",
            ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_ARGUMENTS_DELTA.toString());
        assertEquals("response.foundry_agent_call_arguments.done",
            ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_ARGUMENTS_DONE.toString());
        assertEquals("response.foundry_agent_call.in_progress",
            ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_IN_PROGRESS.toString());
        assertEquals("response.foundry_agent_call.completed",
            ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_COMPLETED.toString());
        assertEquals("response.foundry_agent_call.failed",
            ServerEventType.RESPONSE_FOUNDRY_AGENT_CALL_FAILED.toString());
    }
}
