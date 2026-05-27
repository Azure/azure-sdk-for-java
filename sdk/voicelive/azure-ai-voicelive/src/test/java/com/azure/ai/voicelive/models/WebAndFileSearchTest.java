// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the new web-search and file-search response items and their lifecycle server events.
 */
class WebAndFileSearchTest {

    @Test
    void testItemTypeNewValues() {
        assertNotNull(ItemType.WEB_SEARCH_CALL);
        assertNotNull(ItemType.FILE_SEARCH_CALL);
        assertEquals("web_search_call", ItemType.WEB_SEARCH_CALL.toString());
        assertEquals("file_search_call", ItemType.FILE_SEARCH_CALL.toString());
    }

    @Test
    void testWebSearchCallItemDeserialization() {
        String json = "{\"type\":\"web_search_call\",\"id\":\"ws_1\",\"status\":\"in_progress\"}";

        ResponseWebSearchCallItem item = BinaryData.fromString(json).toObject(ResponseWebSearchCallItem.class);

        assertNotNull(item);
        assertEquals(ItemType.WEB_SEARCH_CALL, item.getType());
        assertEquals("ws_1", item.getId());
        assertEquals(ResponseWebSearchCallItemStatus.IN_PROGRESS, item.getStatus());
    }

    @Test
    void testWebSearchCallItemRoundTrip() {
        String json = "{\"type\":\"web_search_call\",\"id\":\"ws_2\",\"status\":\"completed\"}";
        ResponseWebSearchCallItem item = BinaryData.fromString(json).toObject(ResponseWebSearchCallItem.class);

        BinaryData serialized = BinaryData.fromObject(item);
        ResponseWebSearchCallItem deserialized = serialized.toObject(ResponseWebSearchCallItem.class);

        assertEquals(item.getId(), deserialized.getId());
        assertEquals(item.getType(), deserialized.getType());
        assertEquals(item.getStatus(), deserialized.getStatus());
        assertTrue(serialized.toString().contains("\"type\":\"web_search_call\""));
    }

    @Test
    void testWebSearchCallItemPolymorphicViaSessionResponseItem() {
        String json = "{\"type\":\"web_search_call\",\"id\":\"ws_3\",\"status\":\"searching\"}";

        SessionResponseItem item = BinaryData.fromString(json).toObject(SessionResponseItem.class);

        assertTrue(item instanceof ResponseWebSearchCallItem,
            "Expected ResponseWebSearchCallItem, got " + item.getClass());
        assertEquals(ItemType.WEB_SEARCH_CALL, item.getType());
        assertEquals(ResponseWebSearchCallItemStatus.SEARCHING, ((ResponseWebSearchCallItem) item).getStatus());
    }

    @Test
    void testFileSearchCallItemDeserialization() {
        String json = "{\"type\":\"file_search_call\",\"id\":\"fs_1\",\"status\":\"in_progress\","
            + "\"queries\":[\"alpha\",\"beta\"],"
            + "\"results\":[{\"file_id\":\"f1\",\"filename\":\"a.txt\",\"score\":0.92,\"text\":\"hello\","
            + "\"attributes\":{\"k1\":\"v1\"}}]}";

        ResponseFileSearchCallItem item = BinaryData.fromString(json).toObject(ResponseFileSearchCallItem.class);

        assertNotNull(item);
        assertEquals(ItemType.FILE_SEARCH_CALL, item.getType());
        assertEquals("fs_1", item.getId());
        assertEquals(ResponseFileSearchCallItemStatus.IN_PROGRESS, item.getStatus());

        List<String> queries = item.getQueries();
        assertNotNull(queries);
        assertEquals(2, queries.size());
        assertEquals("alpha", queries.get(0));
        assertEquals("beta", queries.get(1));

        List<FileSearchResult> results = item.getResults();
        assertNotNull(results);
        assertEquals(1, results.size());
        FileSearchResult r = results.get(0);
        assertEquals("f1", r.getFileId());
        assertEquals("a.txt", r.getFilename());
        assertEquals(0.92, r.getScore());
        assertEquals("hello", r.getText());
        assertNotNull(r.getAttributes());
        assertEquals("v1", r.getAttributes().get("k1"));
    }

    @Test
    void testFileSearchCallItemRoundTrip() {
        String json = "{\"type\":\"file_search_call\",\"id\":\"fs_2\",\"status\":\"completed\","
            + "\"queries\":[\"q\"],\"results\":[]}";
        ResponseFileSearchCallItem item = BinaryData.fromString(json).toObject(ResponseFileSearchCallItem.class);

        ResponseFileSearchCallItem deserialized
            = BinaryData.fromObject(item).toObject(ResponseFileSearchCallItem.class);

        assertEquals(item.getId(), deserialized.getId());
        assertEquals(item.getType(), deserialized.getType());
        assertEquals(item.getStatus(), deserialized.getStatus());
        assertEquals(item.getQueries(), deserialized.getQueries());
    }

    @Test
    void testFileSearchCallItemPolymorphicViaSessionResponseItem() {
        String json = "{\"type\":\"file_search_call\",\"id\":\"fs_3\",\"status\":\"searching\"}";

        SessionResponseItem item = BinaryData.fromString(json).toObject(SessionResponseItem.class);

        assertTrue(item instanceof ResponseFileSearchCallItem,
            "Expected ResponseFileSearchCallItem, got " + item.getClass());
        assertEquals(ResponseFileSearchCallItemStatus.SEARCHING, ((ResponseFileSearchCallItem) item).getStatus());
    }

    @Test
    void testFileSearchResultDeserializationWithNullScore() {
        String json = "{\"file_id\":\"fid\",\"filename\":\"name.txt\",\"text\":\"t\"}";

        FileSearchResult result = BinaryData.fromString(json).toObject(FileSearchResult.class);

        assertEquals("fid", result.getFileId());
        assertEquals("name.txt", result.getFilename());
        assertEquals("t", result.getText());
        assertNull(result.getScore());
        assertNull(result.getAttributes());
    }

    @Test
    void testWebSearchCallItemStatusValues() {
        assertEquals(ResponseWebSearchCallItemStatus.IN_PROGRESS,
            ResponseWebSearchCallItemStatus.fromString("in_progress"));
        assertEquals(ResponseWebSearchCallItemStatus.SEARCHING,
            ResponseWebSearchCallItemStatus.fromString("searching"));
        assertEquals(ResponseWebSearchCallItemStatus.COMPLETED,
            ResponseWebSearchCallItemStatus.fromString("completed"));
    }

    @Test
    void testFileSearchCallItemStatusValues() {
        assertEquals(ResponseFileSearchCallItemStatus.IN_PROGRESS,
            ResponseFileSearchCallItemStatus.fromString("in_progress"));
        assertEquals(ResponseFileSearchCallItemStatus.SEARCHING,
            ResponseFileSearchCallItemStatus.fromString("searching"));
        assertEquals(ResponseFileSearchCallItemStatus.COMPLETED,
            ResponseFileSearchCallItemStatus.fromString("completed"));
    }

    // -------- Lifecycle server events --------

    @Test
    void testWebSearchCallLifecycleEventTypes() {
        assertEquals("response.web_search_call.searching",
            ServerEventType.RESPONSE_WEB_SEARCH_CALL_SEARCHING.toString());
        assertEquals("response.web_search_call.in_progress",
            ServerEventType.RESPONSE_WEB_SEARCH_CALL_IN_PROGRESS.toString());
        assertEquals("response.web_search_call.completed",
            ServerEventType.RESPONSE_WEB_SEARCH_CALL_COMPLETED.toString());
    }

    @Test
    void testFileSearchCallLifecycleEventTypes() {
        assertEquals("response.file_search_call.searching",
            ServerEventType.RESPONSE_FILE_SEARCH_CALL_SEARCHING.toString());
        assertEquals("response.file_search_call.in_progress",
            ServerEventType.RESPONSE_FILE_SEARCH_CALL_IN_PROGRESS.toString());
        assertEquals("response.file_search_call.completed",
            ServerEventType.RESPONSE_FILE_SEARCH_CALL_COMPLETED.toString());
    }

    @Test
    void testWebSearchCallSearchingDeserialization() {
        String json = "{\"type\":\"response.web_search_call.searching\",\"event_id\":\"e1\","
            + "\"response_id\":\"r1\",\"item_id\":\"i1\",\"output_index\":0,\"sequence_number\":2}";

        ServerEventResponseWebSearchCallSearching event
            = BinaryData.fromString(json).toObject(ServerEventResponseWebSearchCallSearching.class);

        assertEquals(ServerEventType.RESPONSE_WEB_SEARCH_CALL_SEARCHING, event.getType());
        assertEquals("e1", event.getEventId());
        assertEquals("r1", event.getResponseId());
        assertEquals("i1", event.getItemId());
        assertEquals(0, event.getOutputIndex());
        assertEquals(2, event.getSequenceNumber());
    }

    @Test
    void testFileSearchCallCompletedDeserialization() {
        String json = "{\"type\":\"response.file_search_call.completed\",\"event_id\":\"e2\","
            + "\"response_id\":\"r2\",\"item_id\":\"i2\",\"output_index\":1,\"sequence_number\":5}";

        ServerEventResponseFileSearchCallCompleted event
            = BinaryData.fromString(json).toObject(ServerEventResponseFileSearchCallCompleted.class);

        assertEquals(ServerEventType.RESPONSE_FILE_SEARCH_CALL_COMPLETED, event.getType());
        assertEquals("e2", event.getEventId());
        assertEquals("r2", event.getResponseId());
        assertEquals("i2", event.getItemId());
        assertEquals(1, event.getOutputIndex());
        assertEquals(5, event.getSequenceNumber());
    }

    @Test
    void testFileSearchCallInProgressRoundTrip() {
        String json = "{\"type\":\"response.file_search_call.in_progress\",\"event_id\":\"e3\","
            + "\"response_id\":\"r3\",\"item_id\":\"i3\",\"output_index\":0,\"sequence_number\":1}";
        ServerEventResponseFileSearchCallInProgress event
            = BinaryData.fromString(json).toObject(ServerEventResponseFileSearchCallInProgress.class);

        ServerEventResponseFileSearchCallInProgress deserialized
            = BinaryData.fromObject(event).toObject(ServerEventResponseFileSearchCallInProgress.class);

        assertEquals(event.getType(), deserialized.getType());
        assertEquals(event.getResponseId(), deserialized.getResponseId());
        assertEquals(event.getItemId(), deserialized.getItemId());
        assertEquals(event.getOutputIndex(), deserialized.getOutputIndex());
        assertEquals(event.getSequenceNumber(), deserialized.getSequenceNumber());
    }

    @Test
    void testWebSearchCallCompletedPolymorphicViaSessionUpdate() {
        String json = "{\"type\":\"response.web_search_call.completed\",\"event_id\":\"e4\","
            + "\"response_id\":\"r4\",\"item_id\":\"i4\",\"output_index\":2,\"sequence_number\":7}";

        SessionUpdate update = BinaryData.fromString(json).toObject(SessionUpdate.class);

        assertTrue(update instanceof ServerEventResponseWebSearchCallCompleted,
            "Expected ServerEventResponseWebSearchCallCompleted, got " + update.getClass());
        ServerEventResponseWebSearchCallCompleted typed = (ServerEventResponseWebSearchCallCompleted) update;
        assertEquals("r4", typed.getResponseId());
        assertEquals(7, typed.getSequenceNumber());
    }
}
