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
 * Unit tests for {@link ResponseMCPApprovalRequestItem}.
 */
class ResponseMCPApprovalRequestItemTest {

    @Test
    void testFromJsonWithRequiredFields() throws IOException {
        // Arrange
        String json
            = "{" + "\"id\":\"approval-req-1\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_approval_request\","
                + "\"name\":\"sensitive-tool\"," + "\"server_label\":\"production-server\"" + "}";

        // Act
        ResponseMCPApprovalRequestItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalRequestItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("approval-req-1", item.getId());
        assertEquals(ItemType.MCP_APPROVAL_REQUEST, item.getType());
        assertEquals("sensitive-tool", item.getName());
        assertEquals("production-server", item.getServerLabel());
    }

    @Test
    void testFromJsonWithArguments() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"approval-req-2\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_request\"," + "\"name\":\"delete-tool\"," + "\"server_label\":\"admin-server\","
            + "\"arguments\":\"{\\\"resource_id\\\":\\\"res-123\\\"}\"" + "}";

        // Act
        ResponseMCPApprovalRequestItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalRequestItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("approval-req-2", item.getId());
        assertEquals("delete-tool", item.getName());
        assertEquals("admin-server", item.getServerLabel());
        assertEquals("{\"resource_id\":\"res-123\"}", item.getArguments());
    }

    @Test
    void testTypeIsAlwaysMcpApprovalRequest() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"approval-3\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_request\"," + "\"name\":\"tool\"," + "\"server_label\":\"server\"" + "}";

        // Act
        ResponseMCPApprovalRequestItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalRequestItem.class);

        // Assert
        assertEquals(ItemType.MCP_APPROVAL_REQUEST, item.getType());
    }

    @Test
    void testWithNullArguments() throws IOException {
        // Arrange
        String json
            = "{" + "\"id\":\"approval-4\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_approval_request\","
                + "\"name\":\"no-arg-tool\"," + "\"server_label\":\"test-server\"" + "}";

        // Act
        ResponseMCPApprovalRequestItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalRequestItem.class);

        // Assert
        assertNotNull(item);
        assertNull(item.getArguments());
    }

    @Test
    void testJsonRoundTrip() throws IOException {
        // Arrange
        String originalJson = "{" + "\"id\":\"round-trip-id\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_request\"," + "\"name\":\"backup-tool\"," + "\"server_label\":\"backup-server\","
            + "\"arguments\":\"{\\\"backup_type\\\":\\\"full\\\"}\"" + "}";

        // Act
        ResponseMCPApprovalRequestItem item
            = BinaryData.fromString(originalJson).toObject(ResponseMCPApprovalRequestItem.class);
        String serializedJson = BinaryData.fromObject(item).toString();

        // Assert
        assertNotNull(serializedJson);
        ResponseMCPApprovalRequestItem deserializedItem
            = BinaryData.fromString(serializedJson).toObject(ResponseMCPApprovalRequestItem.class);
        assertEquals(item.getId(), deserializedItem.getId());
        assertEquals(item.getName(), deserializedItem.getName());
        assertEquals(item.getServerLabel(), deserializedItem.getServerLabel());
        assertEquals(item.getArguments(), deserializedItem.getArguments());
    }

    @Test
    void testComplexToolArguments() throws IOException {
        // Arrange
        String complexArgs = "{\"operation\":\"delete\",\"resources\":[\"id1\",\"id2\"],\"force\":true}";
        String json = "{" + "\"id\":\"complex-approval\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_request\"," + "\"name\":\"batch-delete\"," + "\"server_label\":\"data-server\","
            + "\"arguments\":\"" + complexArgs.replace("\"", "\\\"") + "\"" + "}";

        // Act
        ResponseMCPApprovalRequestItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalRequestItem.class);

        // Assert
        assertNotNull(item.getArguments());
        assertEquals(complexArgs, item.getArguments());
    }
}
