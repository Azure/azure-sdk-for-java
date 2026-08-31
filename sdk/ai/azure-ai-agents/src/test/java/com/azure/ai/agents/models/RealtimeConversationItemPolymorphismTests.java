// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeConversationItemPolymorphismTests {
    private static final long CREATED_AT = 1735689600L;
    private static final OffsetDateTime EXPECTED_CREATED_AT = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test
    public void dispatchesEveryMessageRoleAndPreservesPersistenceFields() throws IOException {
        assertMessage("system", "input_text", RealtimeConversationItemMessageSystem.class);
        assertMessage("user", "input_text", RealtimeConversationItemMessageUser.class);
        assertMessage("assistant", "output_text", RealtimeConversationItemMessageAssistant.class);
    }

    @Test
    public void dispatchesEveryConversationItemType() throws IOException {
        String[] items = {
            "{\"type\":\"function_call\",\"call_id\":\"c1\",\"name\":\"f\",\"arguments\":\"{}\"," + persistence() + "}",
            "{\"type\":\"function_call_output\",\"call_id\":\"c1\",\"output\":\"ok\"," + persistence() + "}",
            "{\"type\":\"mcp_list_tools\",\"server_label\":\"s\",\"tools\":[]," + persistence() + "}",
            "{\"type\":\"mcp_call\",\"id\":\"m1\",\"server_label\":\"s\",\"name\":\"n\",\"arguments\":\"{}\","
                + persistence() + "}",
            "{\"type\":\"mcp_approval_request\",\"id\":\"m2\",\"server_label\":\"s\",\"name\":\"n\","
                + "\"arguments\":\"{}\"," + persistence() + "}",
            "{\"type\":\"mcp_approval_response\",\"id\":\"m3\",\"approval_request_id\":\"m2\",\"approve\":true,"
                + persistence() + "}" };
        Class<?>[] expected = {
            RealtimeConversationItemFunctionCall.class,
            RealtimeConversationItemFunctionCallOutput.class,
            RealtimeMCPListTools.class,
            RealtimeMCPToolCall.class,
            RealtimeMCPApprovalRequest.class,
            RealtimeMCPApprovalResponse.class };

        for (int i = 0; i < items.length; i++) {
            RealtimeConversationItem item = deserialize(items[i]);
            assertInstanceOf(expected[i], item);
            assertPersistence(item);
        }
    }

    @Test
    public void voiceResponseOutputDispatchesMessageItems() throws IOException {
        String json = "{\"output\":[" + message("system", "input_text") + "," + message("user", "input_text") + ","
            + message("assistant", "output_text") + "]}";

        VoiceResponse response;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            response = VoiceResponse.fromJson(reader);
        }

        List<RealtimeConversationItem> output = response.getOutput();
        assertEquals(3, output.size());
        assertInstanceOf(RealtimeConversationItemMessageSystem.class, output.get(0));
        assertInstanceOf(RealtimeConversationItemMessageUser.class, output.get(1));
        assertInstanceOf(RealtimeConversationItemMessageAssistant.class, output.get(2));
    }

    @Test
    public void unknownDiscriminatorsFallBackToTheirBaseTypes() throws IOException {
        RealtimeConversationItem unknownItem = deserialize("{\"type\":\"future_item\"}");
        assertSame(RealtimeConversationItem.class, unknownItem.getClass());

        RealtimeConversationItem unknownRole = deserialize("{\"type\":\"message\",\"role\":\"future_role\"}");
        assertSame(RealtimeConversationItemMessage.class, unknownRole.getClass());
    }

    @Test
    public void concreteMessageRolesCannotBeOverwrittenByPayloads() throws IOException {
        try (JsonReader reader = JsonProviders.createReader("{\"role\":\"assistant\",\"content\":[]}")) {
            assertEquals(RealtimeConversationItemMessageType.SYSTEM,
                RealtimeConversationItemMessageSystem.fromJson(reader).getRole());
        }
        try (JsonReader reader = JsonProviders.createReader("{\"role\":\"system\",\"content\":[]}")) {
            assertEquals(RealtimeConversationItemMessageType.USER,
                RealtimeConversationItemMessageUser.fromJson(reader).getRole());
        }
        try (JsonReader reader = JsonProviders.createReader("{\"role\":\"user\",\"content\":[]}")) {
            assertEquals(RealtimeConversationItemMessageType.ASSISTANT,
                RealtimeConversationItemMessageAssistant.fromJson(reader).getRole());
        }
    }

    @Test
    public void messageSerializationWritesDiscriminatorsButNotReadOnlyPersistenceFields() throws IOException {
        RealtimeConversationItemMessageUser user = new RealtimeConversationItemMessageUser(
            Collections.singletonList(new RealtimeConversationItemMessageUserContent())).setId("item");

        String json;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(stream)) {
            user.toJson(writer);
            writer.flush();
            json = stream.toString("UTF-8");
        }

        assertTrue(json.contains("\"type\":\"message\""), json);
        assertTrue(json.contains("\"role\":\"user\""), json);
        assertFalse(json.contains("created_at"), json);
        assertFalse(json.contains("response_id"), json);
    }

    private static void assertMessage(String role, String contentType, Class<?> expectedType) throws IOException {
        RealtimeConversationItem item = deserialize(message(role, contentType));
        assertInstanceOf(expectedType, item);
        assertEquals(RealtimeConversationItemType.MESSAGE, item.getType());
        assertPersistence(item);
    }

    private static String message(String role, String contentType) {
        return "{\"type\":\"message\",\"role\":\"" + role + "\",\"object\":\"realtime.item\","
            + "\"content\":[{\"type\":\"" + contentType + "\",\"text\":\"hello\"}]," + persistence() + "}";
    }

    private static String persistence() {
        return "\"created_at\":" + CREATED_AT + ",\"response_id\":\"response-1\"";
    }

    private static void assertPersistence(RealtimeConversationItem item) {
        OffsetDateTime createdAt;
        String responseId;
        if (item instanceof RealtimeConversationItemMessageSystem) {
            createdAt = ((RealtimeConversationItemMessageSystem) item).getCreatedAt();
            responseId = ((RealtimeConversationItemMessageSystem) item).getResponseId();
        } else if (item instanceof RealtimeConversationItemMessageUser) {
            createdAt = ((RealtimeConversationItemMessageUser) item).getCreatedAt();
            responseId = ((RealtimeConversationItemMessageUser) item).getResponseId();
        } else if (item instanceof RealtimeConversationItemMessageAssistant) {
            createdAt = ((RealtimeConversationItemMessageAssistant) item).getCreatedAt();
            responseId = ((RealtimeConversationItemMessageAssistant) item).getResponseId();
        } else if (item instanceof RealtimeConversationItemFunctionCall) {
            createdAt = ((RealtimeConversationItemFunctionCall) item).getCreatedAt();
            responseId = ((RealtimeConversationItemFunctionCall) item).getResponseId();
        } else if (item instanceof RealtimeConversationItemFunctionCallOutput) {
            createdAt = ((RealtimeConversationItemFunctionCallOutput) item).getCreatedAt();
            responseId = ((RealtimeConversationItemFunctionCallOutput) item).getResponseId();
        } else if (item instanceof RealtimeMCPListTools) {
            createdAt = ((RealtimeMCPListTools) item).getCreatedAt();
            responseId = ((RealtimeMCPListTools) item).getResponseId();
        } else if (item instanceof RealtimeMCPToolCall) {
            createdAt = ((RealtimeMCPToolCall) item).getCreatedAt();
            responseId = ((RealtimeMCPToolCall) item).getResponseId();
        } else if (item instanceof RealtimeMCPApprovalRequest) {
            createdAt = ((RealtimeMCPApprovalRequest) item).getCreatedAt();
            responseId = ((RealtimeMCPApprovalRequest) item).getResponseId();
        } else {
            RealtimeMCPApprovalResponse response = (RealtimeMCPApprovalResponse) item;
            createdAt = response.getCreatedAt();
            responseId = response.getResponseId();
        }
        assertNotNull(createdAt);
        assertEquals(EXPECTED_CREATED_AT, createdAt);
        assertEquals("response-1", responseId);
    }

    private static RealtimeConversationItem deserialize(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return RealtimeConversationItem.fromJson(reader);
        }
    }
}
