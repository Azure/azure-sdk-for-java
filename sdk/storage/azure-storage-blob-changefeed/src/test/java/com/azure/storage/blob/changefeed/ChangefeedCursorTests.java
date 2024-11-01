// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.SegmentCursor;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ChangefeedCursorTests {
    private static final String URL_HOST = "testaccount.blob.core.windows.net";
    private static final OffsetDateTime END_TIME = OffsetDateTime.MAX;

    private static final String SEGMENT_PATH = "idx/segments/2020/08/02/2300/meta.json";
    private static final String CURRENT_SHARD_PATH0 = "log/00/2020/08/02/2300/";
    private static final String CURRENT_SHARD_PATH1 = "log/01/2020/08/02/2300/";

    private static final String CHUNK0 = "log/00/2020/08/02/2300/00000.avro";
    private static final String CHUNK1 = "log/00/2020/08/02/2300/00001.avro";
    private static final String CHUNK2 = "log/01/2020/08/02/2300/00000.avro";

    private static final long OFFSET0 = 2434;
    private static final long OFFSET1 = 18954;

    private static final long INDEX0 = 2;
    private static final long INDEX1 = 15;

    @Test
    public void constructor() {
        ChangefeedCursor cursor = new ChangefeedCursor(URL_HOST, END_TIME);

        assertEquals(1, cursor.getCursorVersion());
        assertEquals(URL_HOST, cursor.getUrlHost());
        assertEquals(END_TIME, cursor.getEndTime());
        assertNull(cursor.getCurrentSegmentCursor());
    }

    @Test
    public void toSegmentCursor() {
        ChangefeedCursor cursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null);

        assertEquals(1, cursor.getCursorVersion());
        assertEquals(URL_HOST, cursor.getUrlHost());
        assertEquals(END_TIME, cursor.getEndTime());
        assertNotNull(cursor.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, cursor.getCurrentSegmentCursor().getSegmentPath());
        assertNotNull(cursor.getCurrentSegmentCursor().getShardCursors());
        assertEquals(0, cursor.getCurrentSegmentCursor().getShardCursors().size());
        assertNull(cursor.getCurrentSegmentCursor().getCurrentShardPath());
    }

    @Test
    public void toShardCursor() {
        ChangefeedCursor cursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0);

        assertEquals(1, cursor.getCursorVersion());
        assertEquals(URL_HOST, cursor.getUrlHost());
        assertEquals(END_TIME, cursor.getEndTime());
        assertNotNull(cursor.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, cursor.getCurrentSegmentCursor().getSegmentPath());
        assertNotNull(cursor.getCurrentSegmentCursor().getShardCursors());
        assertEquals(0, cursor.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CURRENT_SHARD_PATH0, cursor.getCurrentSegmentCursor().getCurrentShardPath());
    }

    @Test
    public void toEventCursor() {
        ChangefeedCursor cursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0)
            .toEventCursor(CHUNK0, OFFSET0, INDEX0);

        assertEquals(1, cursor.getCursorVersion());
        assertEquals(URL_HOST, cursor.getUrlHost());
        assertEquals(END_TIME, cursor.getEndTime());
        assertNotNull(cursor.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, cursor.getCurrentSegmentCursor().getSegmentPath());
        assertNotNull(cursor.getCurrentSegmentCursor().getShardCursors());
        assertEquals(1, cursor.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK0, cursor.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET0, cursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(INDEX0, cursor.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CURRENT_SHARD_PATH0, cursor.getCurrentSegmentCursor().getCurrentShardPath());
    }

    @Test
    public void stateIsSavedInCursorAcrossEvents() {
        /* Note only state we care about is across events. */
        ChangefeedCursor changefeedCursor = new ChangefeedCursor(URL_HOST, END_TIME);

        ChangefeedCursor segmentCursor = changefeedCursor.toSegmentCursor(SEGMENT_PATH, null);

        ChangefeedCursor shardCursor0 = segmentCursor.toShardCursor(CURRENT_SHARD_PATH0);

        ChangefeedCursor eventCursor0 = shardCursor0.toEventCursor(CHUNK0, OFFSET0, 0);
        ChangefeedCursor eventCursor1 = shardCursor0.toEventCursor(CHUNK0, OFFSET0, 1);
        ChangefeedCursor eventCursor2
            = shardCursor0.toEventCursor(CHUNK1, OFFSET1, 0); /* Make sure it still works across chunks. */

        ChangefeedCursor shardCursor1 = segmentCursor.toShardCursor(CURRENT_SHARD_PATH1);

        ChangefeedCursor eventCursor3 = shardCursor1.toEventCursor(CHUNK2, OFFSET1, 0);
        ChangefeedCursor eventCursor4 = shardCursor1.toEventCursor(CHUNK2, OFFSET1, 1);
        ChangefeedCursor eventCursor5 = shardCursor1.toEventCursor(CHUNK2, OFFSET1, 2);

        // Changefeed cursor.
        assertEquals(1, changefeedCursor.getCursorVersion());
        assertEquals(URL_HOST, changefeedCursor.getUrlHost());
        assertEquals(END_TIME, changefeedCursor.getEndTime());
        assertNull(changefeedCursor.getCurrentSegmentCursor());

        // Segment cursor (the shard cursors list should be equivalent to the last event cursor.)
        assertEquals(1, segmentCursor.getCursorVersion());
        assertEquals(URL_HOST, segmentCursor.getUrlHost());
        assertEquals(END_TIME, segmentCursor.getEndTime());
        assertNotNull(segmentCursor.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, segmentCursor.getCurrentSegmentCursor().getSegmentPath());
        assertNull(segmentCursor.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(segmentCursor.getCurrentSegmentCursor().getShardCursors());
        assertEquals(2, segmentCursor.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK1, segmentCursor.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET1, segmentCursor.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(0, segmentCursor.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CHUNK2, segmentCursor.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath());
        assertEquals(OFFSET1, segmentCursor.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset());
        assertEquals(2, segmentCursor.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex());

        // Shard cursor 0 (Should be equivalent to last event cursor with correct shard cursor populated)
        assertEquals(1, shardCursor0.getCursorVersion());
        assertEquals(URL_HOST, shardCursor0.getUrlHost());
        assertEquals(END_TIME, shardCursor0.getEndTime());
        assertNotNull(shardCursor0.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, shardCursor0.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH0, shardCursor0.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(shardCursor0.getCurrentSegmentCursor().getShardCursors());
        assertEquals(2, shardCursor0.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK1, shardCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET1, shardCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(0, shardCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CHUNK2, shardCursor0.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath());
        assertEquals(OFFSET1, shardCursor0.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset());
        assertEquals(2, shardCursor0.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex());

        // Shard cursor 1 (Should be equivalent to last event cursor with correct shard cursor populated)
        assertEquals(1, shardCursor1.getCursorVersion());
        assertEquals(URL_HOST, shardCursor1.getUrlHost());
        assertEquals(END_TIME, shardCursor1.getEndTime());
        assertNotNull(shardCursor1.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, shardCursor1.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH1, shardCursor1.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(shardCursor1.getCurrentSegmentCursor().getShardCursors());
        assertEquals(2, shardCursor1.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK1, shardCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET1, shardCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(0, shardCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CHUNK2, shardCursor1.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath());
        assertEquals(OFFSET1, shardCursor1.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset());
        assertEquals(2, shardCursor1.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex());

        // Event cursor 0
        assertEquals(1, eventCursor0.getCursorVersion());
        assertEquals(URL_HOST, eventCursor0.getUrlHost());
        assertEquals(END_TIME, eventCursor0.getEndTime());
        assertNotNull(eventCursor0.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, eventCursor0.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH0, eventCursor0.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(eventCursor0.getCurrentSegmentCursor().getShardCursors());
        assertEquals(1, eventCursor0.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK0, eventCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET0, eventCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(0, eventCursor0.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());

        // Event cursor 1
        assertEquals(1, eventCursor1.getCursorVersion());
        assertEquals(URL_HOST, eventCursor1.getUrlHost());
        assertEquals(END_TIME, eventCursor1.getEndTime());
        assertNotNull(eventCursor1.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, eventCursor1.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH0, eventCursor1.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(eventCursor1.getCurrentSegmentCursor().getShardCursors());
        assertEquals(1, eventCursor1.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK0, eventCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET0, eventCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(1, eventCursor1.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());

        // Event cursor 2
        assertEquals(1, eventCursor2.getCursorVersion());
        assertEquals(URL_HOST, eventCursor2.getUrlHost());
        assertEquals(END_TIME, eventCursor2.getEndTime());
        assertNotNull(eventCursor2.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, eventCursor2.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH0, eventCursor2.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(eventCursor2.getCurrentSegmentCursor().getShardCursors());
        assertEquals(1, eventCursor2.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK1, eventCursor2.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET1, eventCursor2.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(0, eventCursor2.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());

        // Event cursor 3
        assertEquals(1, eventCursor3.getCursorVersion());
        assertEquals(URL_HOST, eventCursor3.getUrlHost());
        assertEquals(END_TIME, eventCursor3.getEndTime());
        assertNotNull(eventCursor3.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, eventCursor3.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH1, eventCursor3.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(eventCursor3.getCurrentSegmentCursor().getShardCursors());
        assertEquals(2, eventCursor3.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK1, eventCursor3.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET1, eventCursor3.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(0, eventCursor3.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CHUNK2, eventCursor3.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath());
        assertEquals(OFFSET1, eventCursor3.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset());
        assertEquals(0, eventCursor3.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex());

        // Event cursor 4
        assertEquals(1, eventCursor4.getCursorVersion());
        assertEquals(URL_HOST, eventCursor4.getUrlHost());
        assertEquals(END_TIME, eventCursor4.getEndTime());
        assertNotNull(eventCursor4.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, eventCursor4.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH1, eventCursor4.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(eventCursor4.getCurrentSegmentCursor().getShardCursors());
        assertEquals(2, eventCursor4.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK1, eventCursor4.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET1, eventCursor4.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(0, eventCursor4.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CHUNK2, eventCursor4.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath());
        assertEquals(OFFSET1, eventCursor4.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset());
        assertEquals(1, eventCursor4.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex());

        // Event cursor 5
        assertEquals(1, eventCursor5.getCursorVersion());
        assertEquals(URL_HOST, eventCursor5.getUrlHost());
        assertEquals(END_TIME, eventCursor5.getEndTime());
        assertNotNull(eventCursor5.getCurrentSegmentCursor());
        assertEquals(SEGMENT_PATH, eventCursor5.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH1, eventCursor5.getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(eventCursor5.getCurrentSegmentCursor().getShardCursors());
        assertEquals(2, eventCursor5.getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK1, eventCursor5.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET1, eventCursor5.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(0, eventCursor5.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CHUNK2, eventCursor5.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath());
        assertEquals(OFFSET1, eventCursor5.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset());
        assertEquals(2, eventCursor5.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex());
    }

    @Test
    public void serialize() {
        List<ShardCursor> shardCursors
            = Arrays.asList(new ShardCursor(CHUNK0, OFFSET0, INDEX0), new ShardCursor(CHUNK2, OFFSET1, INDEX1));
        SegmentCursor segmentCursor = new SegmentCursor(SEGMENT_PATH, shardCursors, CURRENT_SHARD_PATH1);

        ChangefeedCursor cursor = new ChangefeedCursor(1, URL_HOST, END_TIME, segmentCursor);

        assertEquals("{\"CursorVersion\":1,\"UrlHost\":\"testaccount.blob.core.windows.net\","
            + "\"EndTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"CurrentSegmentCursor\":{"
            + "\"ShardCursors\":[{\"CurrentChunkPath\":\"log/00/2020/08/02/2300/00000.avro\",\"BlockOffset\":2434,"
            + "\"EventIndex\":2},{\"CurrentChunkPath\":\"log/01/2020/08/02/2300/00000.avro\",\"BlockOffset\":18954,"
            + "\"EventIndex\":15}],\"CurrentShardPath\":\"log/01/2020/08/02/2300/\",\"SegmentPath\":\"idx/segments/2020/08/02/2300/meta.json\"}}",
            cursor.serialize());
    }

    @Test
    public void deserialize() {
        String cursor = "{\"CursorVersion\":1,\"UrlHost\":\"testaccount.blob.core.windows.net\","
            + "\"EndTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"CurrentSegmentCursor\":"
            + "{\"ShardCursors\":[{\"CurrentChunkPath\":\"log/00/2020/08/02/2300/00000.avro\","
            + "\"BlockOffset\":2434,\"EventIndex\":2},{\"CurrentChunkPath\":\"log/01/2020/08/02/2300/00000.avro\","
            + "\"BlockOffset\":18954,\"EventIndex\":15}],\"CurrentShardPath\":\"log/01/2020/08/02/2300/\","
            + "\"SegmentPath\":\"idx/segments/2020/08/02/2300/meta.json\"}}";

        ChangefeedCursor deserialized
            = ChangefeedCursor.deserialize(cursor, new ClientLogger(ChangefeedCursorTests.class));

        assertEquals(1, deserialized.getCursorVersion());
        assertEquals(URL_HOST, deserialized.getUrlHost());
        assertEquals(END_TIME, deserialized.getEndTime());
        assertEquals(SEGMENT_PATH, deserialized.getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH1, deserialized.getCurrentSegmentCursor().getCurrentShardPath());
        assertEquals(CHUNK0, deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(OFFSET0, deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(INDEX0, deserialized.getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CHUNK2, deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath());
        assertEquals(OFFSET1, deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset());
        assertEquals(INDEX1, deserialized.getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex());
    }
}
