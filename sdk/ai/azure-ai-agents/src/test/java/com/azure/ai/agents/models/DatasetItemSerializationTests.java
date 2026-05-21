// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.responses.ResponseOutputItem;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for DatasetItem serialization, focusing on the responseItems union type handling.
 * responseItems is a List of OutputItem (discriminated union represented as BinaryData elements).
 * Supports both standard OpenAI ResponseOutputItem types and Azure-specific tool call types.
 */
public class DatasetItemSerializationTests {

    // ===== Serialization tests =====

    /**
     * Tests serialization when responseItems is not set (null).
     */
    @Test
    public void testSerializationWithoutResponseItems() throws IOException {
        DatasetItem item = new DatasetItem("task1", "What is Azure?");

        String json = serializeToJson(item);

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"task1\""));
        assertTrue(json.contains("\"query\":\"What is Azure?\""));
        assertFalse(json.contains("\"response_items\""));
    }

    /**
     * Tests serialization with responseItems set via BinaryData using Azure tool call types (JsonSerializable).
     */
    @Test
    public void testSerializationWithAzureToolCallModel() throws IOException {
        BingGroundingToolCall bingCall
            = new BingGroundingToolCall("call_bing_001", "{\"query\":\"Azure services\"}", ToolCallStatus.COMPLETED);

        DatasetItem item = new DatasetItem("task1", "What is Azure?")
            .setResponseItems(Arrays.asList(BinaryData.fromObject(bingCall)));

        String json = serializeToJson(item);

        assertNotNull(json);
        assertTrue(json.contains("\"response_items\""));
        assertTrue(json.contains("\"type\":\"bing_grounding_call\""));
        assertTrue(json.contains("\"call_id\":\"call_bing_001\""));
        assertTrue(json.contains("\"status\":\"completed\""));
    }

    /**
     * Tests serialization with multiple Azure tool call types.
     */
    @Test
    public void testSerializationWithMultipleAzureToolCallTypes() throws IOException {
        BingGroundingToolCall bingCall
            = new BingGroundingToolCall("call_bing_001", "{\"query\":\"test\"}", ToolCallStatus.COMPLETED);
        AzureAISearchToolCall searchCall
            = new AzureAISearchToolCall("call_search_001", "{\"query\":\"find\"}", ToolCallStatus.COMPLETED);

        DatasetItem item = new DatasetItem("task1", "test query")
            .setResponseItems(Arrays.asList(BinaryData.fromObject(bingCall), BinaryData.fromObject(searchCall)));

        String json = serializeToJson(item);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"bing_grounding_call\""));
        assertTrue(json.contains("\"type\":\"azure_ai_search_call\""));
    }

    /**
     * Tests serialization with SharepointGroundingToolCall.
     */
    @Test
    public void testSerializationWithSharepointToolCall() throws IOException {
        SharepointGroundingToolCall spCall
            = new SharepointGroundingToolCall("call_sp_001", "{}", ToolCallStatus.IN_PROGRESS);

        DatasetItem item = new DatasetItem("task1", "sharepoint query")
            .setResponseItems(Arrays.asList(BinaryData.fromObject(spCall)));

        String json = serializeToJson(item);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"sharepoint_grounding_preview_call\""));
        assertTrue(json.contains("\"status\":\"in_progress\""));
    }

    // ===== Deserialization tests =====

    /**
     * Tests deserialization when responseItems is absent from JSON.
     */
    @Test
    public void testDeserializationWithoutResponseItems() throws IOException {
        String json = "{\"name\":\"task1\",\"query\":\"What is Azure?\"}";

        DatasetItem item = deserializeFromJson(json);

        assertNotNull(item);
        assertEquals("task1", item.getName());
        assertEquals("What is Azure?", item.getQuery());
        assertNull(item.getResponseItemsAsResponseOutputItems());
        assertNull(item.getResponseItems());
    }

    /**
     * Tests deserialization of responseItems containing a message output item (OpenAI type).
     */
    @Test
    public void testDeserializationWithMessageOutputItem() throws IOException {
        String json = "{\"name\":\"task1\",\"query\":\"What is Azure?\","
            + "\"response_items\":[{\"type\":\"message\",\"id\":\"msg_001\"," + "\"status\":\"completed\","
            + "\"content\":[{\"type\":\"output_text\",\"text\":\"Azure is a cloud platform.\",\"annotations\":[]}]}]}";

        DatasetItem item = deserializeFromJson(json);

        assertNotNull(item);
        List<ResponseOutputItem> responseItems = item.getResponseItemsAsResponseOutputItems();
        assertNotNull(responseItems);
        assertEquals(1, responseItems.size());
        assertTrue(responseItems.get(0).isMessage());
        assertEquals("Azure is a cloud platform.",
            responseItems.get(0).asMessage().content().get(0).asOutputText().text());
    }

    /**
     * Tests deserialization of responseItems containing a function call output item.
     */
    @Test
    public void testDeserializationWithFunctionCallOutputItem() throws IOException {
        String json = "{\"name\":\"task2\",\"query\":\"Get weather\","
            + "\"response_items\":[{\"type\":\"function_call\",\"id\":\"fc_001\"," + "\"call_id\":\"call_abc\","
            + "\"name\":\"get_weather\",\"arguments\":\"{\\\"city\\\":\\\"Seattle\\\"}\","
            + "\"status\":\"completed\"}]}";

        DatasetItem item = deserializeFromJson(json);

        assertNotNull(item);
        List<ResponseOutputItem> responseItems = item.getResponseItemsAsResponseOutputItems();
        assertNotNull(responseItems);
        assertEquals(1, responseItems.size());
        assertTrue(responseItems.get(0).isFunctionCall());
        assertEquals("get_weather", responseItems.get(0).asFunctionCall().name());
    }

    /**
     * Tests deserialization of responseItems containing an Azure-specific tool call type.
     * These can be accessed via getResponseItems() as BinaryData.
     */
    @Test
    public void testDeserializationWithAzureToolCallType() throws IOException {
        String json = "{\"name\":\"task3\",\"query\":\"Search Azure\","
            + "\"response_items\":[{\"type\":\"bing_grounding_call\"," + "\"call_id\":\"call_bing_001\","
            + "\"arguments\":\"{\\\"query\\\":\\\"Azure\\\"}\"," + "\"status\":\"completed\"}]}";

        DatasetItem item = deserializeFromJson(json);

        assertNotNull(item);
        List<BinaryData> responseItems = item.getResponseItems();
        assertNotNull(responseItems);
        assertEquals(1, responseItems.size());

        // Deserialize the BinaryData to the generated Azure model class
        BingGroundingToolCall bingCall = responseItems.get(0).toObject(BingGroundingToolCall.class);
        assertEquals("call_bing_001", bingCall.getCallId());
        assertEquals(ToolCallStatus.COMPLETED, bingCall.getStatus());
    }

    // ===== Null tests =====

    /**
     * Tests that setting responseItems to null via typed setter results in null getter.
     */
    @Test
    public void testSetResponseOutputItemsNull() {
        DatasetItem item = new DatasetItem("task1", "query");
        item.setResponseOutputItems(null);

        assertNull(item.getResponseItemsAsResponseOutputItems());
        assertNull(item.getResponseItems());
    }

    /**
     * Tests that setting responseItems to null via BinaryData setter results in null getter.
     */
    @Test
    public void testSetResponseItemsBinaryDataNull() {
        DatasetItem item = new DatasetItem("task1", "query");
        item.setResponseItems((List<BinaryData>) null);

        assertNull(item.getResponseItems());
        assertNull(item.getResponseItemsAsResponseOutputItems());
    }

    /**
     * Tests that getters return null when responseItems is not set.
     */
    @Test
    public void testGettersReturnNullWhenNotSet() {
        DatasetItem item = new DatasetItem("task1", "query");

        assertNull(item.getResponseItems());
        assertNull(item.getResponseItemsAsResponseOutputItems());
    }

    // ===== Round-trip tests =====

    /**
     * Tests round-trip with Azure tool call model (BingGroundingToolCall).
     */
    @Test
    public void testRoundTripWithAzureToolCallModel() throws IOException {
        BingGroundingToolCall bingCall
            = new BingGroundingToolCall("call_bing_001", "{\"query\":\"test\"}", ToolCallStatus.COMPLETED);

        DatasetItem original = new DatasetItem("task_rt", "Round trip query").setGroundTruth("expected answer")
            .setResponseItems(Arrays.asList(BinaryData.fromObject(bingCall)));

        String json = serializeToJson(original);
        DatasetItem deserialized = deserializeFromJson(json);

        assertEquals("task_rt", deserialized.getName());
        assertEquals("Round trip query", deserialized.getQuery());
        assertEquals("expected answer", deserialized.getGroundTruth());

        List<BinaryData> items = deserialized.getResponseItems();
        assertNotNull(items);
        assertEquals(1, items.size());

        BingGroundingToolCall roundTrippedCall = items.get(0).toObject(BingGroundingToolCall.class);
        assertEquals("call_bing_001", roundTrippedCall.getCallId());
        assertEquals(ToolCallStatus.COMPLETED, roundTrippedCall.getStatus());
    }

    /**
     * Tests round-trip with multiple response items including OpenAI types.
     */
    @Test
    public void testRoundTripWithMultipleItems() throws IOException {
        String json = "{\"name\":\"multi\",\"query\":\"test\"," + "\"response_items\":["
            + "{\"type\":\"function_call\",\"id\":\"fc_1\",\"call_id\":\"call_1\","
            + "\"name\":\"search\",\"arguments\":\"{}\",\"status\":\"completed\"},"
            + "{\"type\":\"message\",\"id\":\"msg_1\",\"status\":\"completed\","
            + "\"content\":[{\"type\":\"output_text\",\"text\":\"result\",\"annotations\":[]}]}" + "]}";

        DatasetItem item = deserializeFromJson(json);
        String reserialized = serializeToJson(item);
        DatasetItem roundTripped = deserializeFromJson(reserialized);

        List<ResponseOutputItem> items = roundTripped.getResponseItemsAsResponseOutputItems();
        assertNotNull(items);
        assertEquals(2, items.size());
        assertTrue(items.get(0).isFunctionCall());
        assertTrue(items.get(1).isMessage());
    }

    /**
     * Tests round-trip with mixed Azure and OpenAI items.
     */
    @Test
    public void testRoundTripWithMixedAzureAndOpenAIItems() throws IOException {
        String json = "{\"name\":\"mixed\",\"query\":\"test\"," + "\"response_items\":["
            + "{\"type\":\"bing_grounding_call\",\"call_id\":\"call_bing\","
            + "\"arguments\":\"{}\",\"status\":\"completed\"},"
            + "{\"type\":\"message\",\"id\":\"msg_1\",\"status\":\"completed\","
            + "\"content\":[{\"type\":\"output_text\",\"text\":\"hello\",\"annotations\":[]}]}" + "]}";

        DatasetItem item = deserializeFromJson(json);
        String reserialized = serializeToJson(item);
        DatasetItem roundTripped = deserializeFromJson(reserialized);

        List<BinaryData> items = roundTripped.getResponseItems();
        assertNotNull(items);
        assertEquals(2, items.size());

        // First item is Azure-specific tool call
        BingGroundingToolCall bingCall = items.get(0).toObject(BingGroundingToolCall.class);
        assertEquals("call_bing", bingCall.getCallId());

        // Second item is an OpenAI message
        List<ResponseOutputItem> outputItems = roundTripped.getResponseItemsAsResponseOutputItems();
        assertTrue(outputItems.get(1).isMessage());
    }

    // Helper method to serialize to JSON string
    private String serializeToJson(DatasetItem item) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            item.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    // Helper method to deserialize from JSON string
    private DatasetItem deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return DatasetItem.fromJson(jsonReader);
        }
    }
}
