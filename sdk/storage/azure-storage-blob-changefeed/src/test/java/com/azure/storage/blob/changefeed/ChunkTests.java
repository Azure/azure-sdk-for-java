// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData;
import com.azure.storage.internal.avro.implementation.AvroObject;
import com.azure.storage.internal.avro.implementation.AvroReader;
import com.azure.storage.internal.avro.implementation.AvroReaderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChunkTests {
    private AvroReaderFactory mockAvroReaderFactory;
    private AvroReader mockAvroReader;
    private BlobChunkedDownloaderFactory mockBlobLazyDownloaderFactory;
    private BlobChunkedDownloader mockBlobLazyDownloader;

    private static final String URL_HOST = "testaccount.blob.core.windows.net";
    private static final OffsetDateTime END_TIME = OffsetDateTime.MAX;
    private static final String SEGMENT_PATH = "idx/segments/2020/08/02/2300/meta.json";
    private static final String CURRENT_SHARD_PATH0 = "log/00/2020/08/02/2300/";
    private static final String CURRENT_SHARD_PATH1 = "log/01/2020/08/02/2300/";
    private static final String CHUNK_PATH0 = "log/00/2020/08/02/2300/00000.avro";
    private static final String CHUNK_PATH1 = "log/00/2020/08/02/2300/00001.avro";
    private static final String CHUNK_PATH2 = "log/01/2020/08/02/2300/00000.avro";

    private List<BlobChangefeedEvent> mockEvents;
    private List<AvroObject> mockAvroObjects;

    @BeforeEach
    public void setup() {
        setupEvents();

        mockAvroReaderFactory = mock(AvroReaderFactory.class);
        mockAvroReader = mock(AvroReader.class);
        mockBlobLazyDownloaderFactory = mock(BlobChunkedDownloaderFactory.class);
        mockBlobLazyDownloader = mock(BlobChunkedDownloader.class);

        when(mockBlobLazyDownloaderFactory.getBlobLazyDownloader(anyString(), anyLong(), anyLong()))
            .thenReturn(mockBlobLazyDownloader);
        /* The data returned by the lazy downloader does not matter since we're mocking avroObjects.  */
        when(mockBlobLazyDownloader.download()).thenReturn(Flux.empty());
        when(mockAvroReader.read()).thenReturn(Flux.fromIterable(mockAvroObjects));
    }

    /* Tests no user cursor. These tests check that the event cursor is properly populated. */
    @Test
    public void getEventsMinShard0Chunk0() {
        /* Cursor on shard 0, chunk 0 - basically when you first encounter a chunk in a shard. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0);
        when(mockAvroReaderFactory.getAvroReader(any())).thenReturn(mockAvroReader);

        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory);
        Chunk chunk = factory.getChunk(CHUNK_PATH0, Long.MAX_VALUE, chunkCursor, 0, 0);

        StepVerifier.create(chunk.getEvents().index())
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 2))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 3))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 5678, 0))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 5678, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 5678, 2))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 5678, 3))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 9101, 0))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 9101, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 10000, 0))
            .verifyComplete();

        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(CHUNK_PATH0, ChunkFactory.DEFAULT_BODY_SIZE, 0);
        verify(mockBlobLazyDownloader).download();
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty());
        verify(mockAvroReader).read();
    }

    @Test
    public void getEventsMinShard0Chunk1() {
        /* Cursor on shard 0, chunk 1 - basically when you encounter a chunk in a shard after having already encountered a different chunk. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0)
            .toEventCursor(CHUNK_PATH0, 9109, 1);
        when(mockAvroReaderFactory.getAvroReader(any())).thenReturn(mockAvroReader);

        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory);
        Chunk chunk = factory.getChunk(CHUNK_PATH1, Long.MAX_VALUE, chunkCursor, 0, 0);

        StepVerifier.create(chunk.getEvents().index())
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 2))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 3))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 5678, 0))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 5678, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 5678, 2))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 5678, 3))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 9101, 0))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 9101, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 10000, 0))
            .verifyComplete();

        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(CHUNK_PATH1, ChunkFactory.DEFAULT_BODY_SIZE, 0);
        verify(mockBlobLazyDownloader).download();
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty());
        verify(mockAvroReader).read();
    }

    @Test
    public void getEventsMinShard1Chunk0() {
        /* Cursor on shard 1, chunk 0 - basically when you encounter a chunk in a shard after having already encountered a different shard. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0)
            .toEventCursor(CHUNK_PATH0, 9109, 1)
            .toShardCursor(CURRENT_SHARD_PATH1);
        when(mockAvroReaderFactory.getAvroReader(any())).thenReturn(mockAvroReader);

        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory);
        Chunk chunk = factory.getChunk(CHUNK_PATH2, Long.MAX_VALUE, chunkCursor, 0, 0);

        StepVerifier.create(chunk.getEvents().index())
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 1234, 1))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 1234, 2))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 1234, 3))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 5678, 0))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 5678, 1))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 5678, 2))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 5678, 3))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 9101, 0))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 9101, 1))
            .assertNext(tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 10000, 0))
            .verifyComplete();

        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(CHUNK_PATH2, ChunkFactory.DEFAULT_BODY_SIZE, 0);
        verify(mockBlobLazyDownloader).download();
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty());
        verify(mockAvroReader).read();
    }

    /* Tests user cursor. Tests that a chunk can properly read events with the user cursor information. */
    @ParameterizedTest
    @CsvSource({ "1234,0", "1234,1", "1234,2", "1234,3", "5678,0", "5678,1", "5678,2", "5678,3", "9101,0", "9101,1" })
    public void getEventsCursor(long blockOffset, long eventIndex) {
        /* Default chunk cursor on shard 0. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0);
        when(mockAvroReaderFactory.getAvroReader(any(), any(), anyLong(), anyLong())).thenReturn(mockAvroReader);
        when(mockBlobLazyDownloaderFactory.getBlobLazyDownloader(anyString(), anyLong()))
            .thenReturn(mockBlobLazyDownloader);

        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory);
        Chunk chunk = factory.getChunk(CHUNK_PATH0, Long.MAX_VALUE, chunkCursor, blockOffset, eventIndex);

        StepVerifier.create(chunk.getEvents().index())
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 2))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 3))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 5678, 0))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 5678, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 5678, 2))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 5678, 3))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 9101, 0))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 9101, 1))
            .assertNext(tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 10000, 0))
            .verifyComplete();

        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(CHUNK_PATH0, ChunkFactory.DEFAULT_HEADER_SIZE);
        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(CHUNK_PATH0, ChunkFactory.DEFAULT_BODY_SIZE,
            blockOffset);
        verify(mockBlobLazyDownloader, times(2)).download();
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty(), Flux.empty(), blockOffset, eventIndex);
        verify(mockAvroReader).read();
    }

    @Test
    public void getEventsInvalidBlockOffset() {
        /* Default chunk cursor on shard 0. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0);

        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory);

        RuntimeException e = assertThrows(IllegalArgumentException.class,
            () -> factory.getChunk(CHUNK_PATH0, 1000, chunkCursor, 1001, 3));
        assertEquals(e.getMessage(), "Cursor contains a blockOffset that is invalid.");
    }

    @Test
    public void getEventsBlockOffsetAtEnd() {
        /* Default chunk cursor on shard 0. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0);

        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory);
        Chunk chunk = factory.getChunk(CHUNK_PATH0, 1001, chunkCursor, 1001, 3);

        StepVerifier.create(chunk.getEvents().index()).verifyComplete(); /* Chunk should complete with 0 events. */
    }

    private void verifyWrapperShard0(BlobChangefeedEventWrapper wrapper, long index, String chunkPath, long blockOffset,
        long blockIndex) {
        assertEquals(URL_HOST, wrapper.getCursor().getUrlHost());
        assertEquals(END_TIME, wrapper.getCursor().getEndTime());
        assertEquals(SEGMENT_PATH, wrapper.getCursor().getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH0, wrapper.getCursor().getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(wrapper.getCursor().getCurrentSegmentCursor().getShardCursors());
        assertEquals(1, wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(chunkPath,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(blockOffset,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(blockIndex,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        /* Make sure the event in the wrapper is what was expected. */
        assertEquals(mockEvents.get((int) index), wrapper.getEvent());
    }

    private void verifyWrapperShard1(BlobChangefeedEventWrapper wrapper, long index, long blockOffset,
        long blockIndex) {
        assertEquals(END_TIME, wrapper.getCursor().getEndTime());
        assertEquals(SEGMENT_PATH, wrapper.getCursor().getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH1, wrapper.getCursor().getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(wrapper.getCursor().getCurrentSegmentCursor().getShardCursors());
        assertEquals(2, wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().size());
        assertEquals(CHUNK_PATH0,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(9109, wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(1, wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());
        assertEquals(CHUNK_PATH2,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath());
        assertEquals(blockOffset,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset());
        assertEquals(blockIndex,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex());
        /* Make sure the event in the wrapper is what was expected. */
        assertEquals(mockEvents.get((int) index), wrapper.getEvent());
    }

    private void setupEvents() {
        mockEvents = new ArrayList<>();
        List<Map<String, Object>> mockRecords = new ArrayList<>();
        mockAvroObjects = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BlobChangefeedEvent event = MockedChangefeedResources.getMockBlobChangefeedEvent(i);
            mockEvents.add(event);
            /* These are the records emitted in the AvroParser. */
            /* This tests that the BlobChangefeedEvent objects are populated correctly from a record. */
            mockRecords.add(getMockChangefeedEventRecord(mockEvents.get(i)));
        }
        /* These are the wrapped records -> AvroObjects emitted by the AvroReader. */
        mockAvroObjects.add(new AvroObject(1234, 0, 1234, 1, mockRecords.get(0)));
        mockAvroObjects.add(new AvroObject(1234, 1, 1234, 2, mockRecords.get(1)));
        mockAvroObjects.add(new AvroObject(1234, 2, 1234, 3, mockRecords.get(2)));
        mockAvroObjects.add(new AvroObject(1234, 3, 5678, 0, mockRecords.get(3)));
        mockAvroObjects.add(new AvroObject(5678, 0, 5678, 1, mockRecords.get(4)));
        mockAvroObjects.add(new AvroObject(5678, 1, 5678, 2, mockRecords.get(5)));
        mockAvroObjects.add(new AvroObject(5678, 2, 5678, 3, mockRecords.get(6)));
        mockAvroObjects.add(new AvroObject(5678, 3, 9101, 0, mockRecords.get(7)));
        mockAvroObjects.add(new AvroObject(9101, 0, 9101, 1, mockRecords.get(8)));
        mockAvroObjects.add(new AvroObject(9101, 1, 10000, 0, mockRecords.get(9)));
    }

    private static Map<String, Object> getMockChangefeedEventRecord(BlobChangefeedEvent event) {
        Map<String, Object> cfEvent = new HashMap<>();
        cfEvent.put("$record", "BlobChangeEvent");
        cfEvent.put("schemaVersion", 1);
        cfEvent.put("topic", event.getTopic());
        cfEvent.put("subject", event.getSubject());
        cfEvent.put("eventType", event.getEventType().toString());
        cfEvent.put("eventTime", event.getEventTime().toString());
        cfEvent.put("id", event.getId());
        cfEvent.put("data", getMockChangefeedEventDataRecord(event.getData()));
        cfEvent.put("dataVersion", event.getDataVersion());
        cfEvent.put("metadataVersion", event.getMetadataVersion());
        return cfEvent;
    }

    private static Map<String, Object> getMockChangefeedEventDataRecord(BlobChangefeedEventData data) {
        Map<String, Object> cfEventData = new HashMap<>();
        cfEventData.put("$record", "BlobChangeEventData");
        cfEventData.put("api", data.getApi());
        cfEventData.put("clientRequestId", data.getClientRequestId());
        cfEventData.put("requestId", data.getRequestId());
        cfEventData.put("etag", data.getETag());
        cfEventData.put("contentType", data.getContentType());
        cfEventData.put("contentLength", data.getContentLength());
        cfEventData.put("contentOffset", data.getContentOffset());
        cfEventData.put("blobType", data.getBlobType().toString());
        cfEventData.put("destinationUrl", data.getDestinationUrl());
        cfEventData.put("sourceUrl", data.getSourceUrl());
        cfEventData.put("url", data.getBlobUrl());
        cfEventData.put("sequencer", data.getSequencer());
        cfEventData.put("recursive", data.isRecursive());
        return cfEventData;
    }
}
