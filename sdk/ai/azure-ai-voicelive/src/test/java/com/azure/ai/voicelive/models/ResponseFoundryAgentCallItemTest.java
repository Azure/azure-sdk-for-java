// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ResponseFoundryAgentCallItem}.
 */
class ResponseFoundryAgentCallItemTest {

    @Test
    void testResponseFoundryAgentCallItemDeserialization() {
        // Arrange
        String json = "{\"type\":\"foundry_agent_call\",\"id\":\"item123\",\"object\":\"realtime.item\","
            + "\"name\":\"test-agent\",\"call_id\":\"call456\",\"arguments\":\"{\\\"param\\\":\\\"value\\\"}\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ResponseFoundryAgentCallItem item = data.toObject(ResponseFoundryAgentCallItem.class);

        // Assert
        assertNotNull(item);
        assertEquals(ItemType.FOUNDRY_AGENT_CALL, item.getType());
        assertEquals("item123", item.getId());
        assertEquals(ResponseItemObject.REALTIME_ITEM, item.getObject());
        assertEquals("test-agent", item.getName());
        assertEquals("call456", item.getCallId());
        assertEquals("{\"param\":\"value\"}", item.getArguments());
    }

    @Test
    void testResponseFoundryAgentCallItemWithAllFields() {
        // Arrange
        String json = "{\"type\":\"foundry_agent_call\",\"id\":\"item789\",\"object\":\"realtime.item\","
            + "\"name\":\"my-agent\",\"call_id\":\"call012\",\"arguments\":\"{}\","
            + "\"agent_response_id\":\"resp345\",\"output\":\"Agent response text\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ResponseFoundryAgentCallItem item = data.toObject(ResponseFoundryAgentCallItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("my-agent", item.getName());
        assertEquals("call012", item.getCallId());
        assertEquals("{}", item.getArguments());
        assertEquals("resp345", item.getAgentResponseId());
        assertEquals("Agent response text", item.getOutput());
    }

    @Test
    void testResponseFoundryAgentCallItemWithError() {
        // Arrange
        String json = "{\"type\":\"foundry_agent_call\",\"id\":\"item999\",\"object\":\"realtime.item\","
            + "\"name\":\"error-agent\",\"call_id\":\"call888\",\"arguments\":\"{}\","
            + "\"error\":{\"code\":\"error_code\",\"message\":\"Something went wrong\"}}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ResponseFoundryAgentCallItem item = data.toObject(ResponseFoundryAgentCallItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("error-agent", item.getName());
        assertNotNull(item.getError());
    }

    @Test
    void testItemTypeFoundryAgentCall() {
        // Assert
        assertEquals("foundry_agent_call", ItemType.FOUNDRY_AGENT_CALL.toString());
        assertEquals(ItemType.FOUNDRY_AGENT_CALL, ItemType.fromString("foundry_agent_call"));
    }

    @Test
    void testResponseFoundryAgentCallItemJsonRoundTrip() {
        // Arrange
        String json = "{\"type\":\"foundry_agent_call\",\"id\":\"item111\",\"object\":\"realtime.item\","
            + "\"name\":\"roundtrip-agent\",\"call_id\":\"call222\",\"arguments\":\"{\\\"test\\\":true}\"}";
        BinaryData originalData = BinaryData.fromString(json);
        ResponseFoundryAgentCallItem item = originalData.toObject(ResponseFoundryAgentCallItem.class);

        // Act
        BinaryData serialized = BinaryData.fromObject(item);
        ResponseFoundryAgentCallItem deserialized = serialized.toObject(ResponseFoundryAgentCallItem.class);

        // Assert
        assertEquals(item.getType(), deserialized.getType());
        assertEquals(item.getId(), deserialized.getId());
        assertEquals(item.getObject(), deserialized.getObject());
        assertEquals(item.getName(), deserialized.getName());
        assertEquals(item.getCallId(), deserialized.getCallId());
        assertEquals(item.getArguments(), deserialized.getArguments());
    }

    @Test
    void testResponseFoundryAgentCallItemMinimalFields() {
        // Arrange - minimal required fields
        String json = "{\"type\":\"foundry_agent_call\",\"name\":\"minimal-agent\","
            + "\"call_id\":\"minimal-call\",\"arguments\":\"{}\"}";
        BinaryData data = BinaryData.fromString(json);

        // Act
        ResponseFoundryAgentCallItem item = data.toObject(ResponseFoundryAgentCallItem.class);

        // Assert
        assertNotNull(item);
        assertEquals(ItemType.FOUNDRY_AGENT_CALL, item.getType());
        assertEquals("minimal-agent", item.getName());
        assertEquals("minimal-call", item.getCallId());
        assertEquals("{}", item.getArguments());
        assertNull(item.getAgentResponseId());
        assertNull(item.getOutput());
        assertNull(item.getError());
    }
}
