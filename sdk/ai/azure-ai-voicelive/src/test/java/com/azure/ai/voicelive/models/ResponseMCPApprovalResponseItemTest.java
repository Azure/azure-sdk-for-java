// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ResponseMCPApprovalResponseItem}.
 */
class ResponseMCPApprovalResponseItemTest {

    @Test
    void testFromJsonWithApprove() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"response-1\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_response\"," + "\"approval_request_id\":\"req-123\"," + "\"approve\":true" + "}";

        // Act
        ResponseMCPApprovalResponseItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalResponseItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("response-1", item.getId());
        assertEquals(ItemType.MCP_APPROVAL_RESPONSE, item.getType());
        assertEquals("req-123", item.getApprovalRequestId());
        assertTrue(item.isApprove());
    }

    @Test
    void testFromJsonWithDeny() throws IOException {
        // Arrange
        String json
            = "{" + "\"id\":\"response-2\"," + "\"object\":\"session.item\"," + "\"type\":\"mcp_approval_response\","
                + "\"approval_request_id\":\"req-456\"," + "\"approve\":false" + "}";

        // Act
        ResponseMCPApprovalResponseItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalResponseItem.class);

        // Assert
        assertNotNull(item);
        assertEquals("response-2", item.getId());
        assertEquals("req-456", item.getApprovalRequestId());
        assertFalse(item.isApprove());
    }

    @Test
    void testFromJsonWithReason() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"response-3\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_response\"," + "\"approval_request_id\":\"req-789\"," + "\"approve\":false,"
            + "\"reason\":\"Security policy violation\"" + "}";

        // Act
        ResponseMCPApprovalResponseItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalResponseItem.class);

        // Assert
        assertNotNull(item);
        assertFalse(item.isApprove());
        assertEquals("Security policy violation", item.getReason());
    }

    @Test
    void testTypeIsAlwaysMcpApprovalResponse() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"response-4\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_response\"," + "\"approval_request_id\":\"req-999\"," + "\"approve\":true" + "}";

        // Act
        ResponseMCPApprovalResponseItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalResponseItem.class);

        // Assert
        assertEquals(ItemType.MCP_APPROVAL_RESPONSE, item.getType());
    }

    @Test
    void testWithNullReason() throws IOException {
        // Arrange
        String json = "{" + "\"id\":\"response-5\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_response\"," + "\"approval_request_id\":\"req-111\"," + "\"approve\":true" + "}";

        // Act
        ResponseMCPApprovalResponseItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalResponseItem.class);

        // Assert
        assertNotNull(item);
        assertNull(item.getReason());
    }

    @Test
    void testJsonRoundTrip() throws IOException {
        // Arrange
        String originalJson = "{" + "\"id\":\"round-trip\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_response\"," + "\"approval_request_id\":\"req-rt-001\"," + "\"approve\":false,"
            + "\"reason\":\"User denied the action\"" + "}";

        // Act
        ResponseMCPApprovalResponseItem item
            = BinaryData.fromString(originalJson).toObject(ResponseMCPApprovalResponseItem.class);
        String serializedJson = BinaryData.fromObject(item).toString();

        // Assert
        assertNotNull(serializedJson);
        ResponseMCPApprovalResponseItem deserializedItem
            = BinaryData.fromString(serializedJson).toObject(ResponseMCPApprovalResponseItem.class);
        assertEquals(item.getId(), deserializedItem.getId());
        assertEquals(item.getApprovalRequestId(), deserializedItem.getApprovalRequestId());
        assertEquals(item.isApprove(), deserializedItem.isApprove());
        assertEquals(item.getReason(), deserializedItem.getReason());
    }

    @Test
    void testApprovalWithDetailedReason() throws IOException {
        // Arrange
        String detailedReason = "Tool requires elevated privileges. User must confirm the action explicitly.";
        String json = "{" + "\"id\":\"detailed-response\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_response\"," + "\"approval_request_id\":\"req-detailed\"," + "\"approve\":false,"
            + "\"reason\":\"" + detailedReason + "\"" + "}";

        // Act
        ResponseMCPApprovalResponseItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalResponseItem.class);

        // Assert
        assertNotNull(item);
        assertEquals(detailedReason, item.getReason());
        assertFalse(item.isApprove());
    }

    @Test
    void testApprovalApprovedWithReason() throws IOException {
        // Arrange - approved with reason explaining why
        String json = "{" + "\"id\":\"approved-with-reason\"," + "\"object\":\"session.item\","
            + "\"type\":\"mcp_approval_response\"," + "\"approval_request_id\":\"req-approved\"," + "\"approve\":true,"
            + "\"reason\":\"User explicitly confirmed the action\"" + "}";

        // Act
        ResponseMCPApprovalResponseItem item
            = BinaryData.fromString(json).toObject(ResponseMCPApprovalResponseItem.class);

        // Assert
        assertTrue(item.isApprove());
        assertEquals("User explicitly confirmed the action", item.getReason());
    }
}
