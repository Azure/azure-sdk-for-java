// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ResponseMCPCallItem}.
 */
class ResponseMCPCallItemTest {

    @Test
    void testFromJsonWithRequiredFields() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"item-1\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_call\","
            + "\"arguments\":\"{\\\"param\\\":\\\"value\\\"}\"," + "\"server_label\":\"test-server\","
            + "\"name\":\"test-tool\"" + "}";

        // Act
        ResponseMCPCallItem item = BinaryData.fromString(json).toObject(ResponseMCPCallItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("item-1", item.getId());
        assertEquals(ItemType.MCP_CALL, item.getType());
        assertEquals("{\"param\":\"value\"}", item.getArguments());
        assertEquals("test-server", item.getServerLabel());
        assertEquals("test-tool", item.getName());
    }

    @Test
    void testFromJsonWithAllFields() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"item-2\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_call\","
            + "\"arguments\":\"{}\"," + "\"server_label\":\"my-server\"," + "\"name\":\"my-tool\","
            + "\"output\":\"success\"," + "\"approval_request_id\":\"approval-123\","
            + "\"error\":{\"message\":\"error occurred\"}" + "}";

        // Act
        ResponseMCPCallItem item = BinaryData.fromString(json).toObject(ResponseMCPCallItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("item-2", item.getId());
        assertEquals("{}", item.getArguments());
        assertEquals("my-server", item.getServerLabel());
        assertEquals("my-tool", item.getName());
        assertEquals("success", item.getOutput());
        assertEquals("approval-123", item.getApprovalRequestId());
        assertNotNull(item.getError());
    }

    @Test
    void testTypeIsAlwaysMcpCall() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"item-3\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_call\","
            + "\"arguments\":\"{}\"," + "\"server_label\":\"server\"," + "\"name\":\"tool\"" + "}";

        // Act
        ResponseMCPCallItem item = BinaryData.fromString(json).toObject(ResponseMCPCallItem.class);

        // Assert
        assertEquals(ItemType.MCP_CALL, item.getType());
    }

    @Test
    void testWithNullOptionalFields() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"item-4\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_call\","
            + "\"arguments\":\"{\\\"test\\\":true}\"," + "\"server_label\":\"label\"," + "\"name\":\"toolname\"" + "}";

        // Act
        ResponseMCPCallItem item = BinaryData.fromString(json).toObject(ResponseMCPCallItem.class);

        // Assert
        assertNotNull(item);
        assertNull(item.getOutput());
        assertNull(item.getApprovalRequestId());
        assertNull(item.getError());
    }

    @Test
    void testJsonRoundTrip() throws IOException {
        // Arrange
        String originalJson = "{" + "\"id\":\"test-id\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_call\","
            + "\"arguments\":\"{\\\"key\\\":\\\"value\\\"}\"," + "\"server_label\":\"test-server\","
            + "\"name\":\"test-tool\"," + "\"output\":\"result\"" + "}";

        // Act
        ResponseMCPCallItem item = BinaryData.fromString(originalJson).toObject(ResponseMCPCallItem.class);
        String serializedJson = BinaryData.fromObject(item).toString();

        // Assert
        assertNotNull(serializedJson);
        // Deserialize again to verify round-trip
        ResponseMCPCallItem deserializedItem
            = BinaryData.fromString(serializedJson).toObject(ResponseMCPCallItem.class);
        assertEquals(item.getId(), deserializedItem.getId());
        assertEquals(item.getArguments(), deserializedItem.getArguments());
        assertEquals(item.getServerLabel(), deserializedItem.getServerLabel());
        assertEquals(item.getName(), deserializedItem.getName());
    }

    @Test
    void testComplexArgumentsJson() throws IOException {
        // Arrange
        String complexArgs = "{\"nested\":{\"array\":[1,2,3],\"string\":\"value\"},\"number\":42}";
        String json = "{" + "\"id\":\"item-5\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_call\","
            + "\"arguments\":\"" + complexArgs.replace("\"", "\\\"") + "\"," + "\"server_label\":\"server\","
            + "\"name\":\"complex-tool\"" + "}";

        // Act
        ResponseMCPCallItem item = BinaryData.fromString(json).toObject(ResponseMCPCallItem.class);

        // Assert
        assertNotNull(item.getArguments());
        assertEquals(complexArgs, item.getArguments());
    }
}
