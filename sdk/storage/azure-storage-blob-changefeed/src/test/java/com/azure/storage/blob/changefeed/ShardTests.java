// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ShardTests {
    private static final String URL_HOST = "testaccount.blob.core.windows.net";
    private static final OffsetDateTime END_TIME = OffsetDateTime.MAX;
    private static final String SEGMENT_PATH = "idx/segments/2020/08/02/2300/meta.json";
    private static final String CURRENT_SHARD_PATH0 = "log/00/2020/08/02/2300/";

    private static final String CHUNK_PATH0 = "log/00/2020/08/02/2300/00000.avro";
    private static final String CHUNK_PATH1 = "log/00/2020/08/02/2300/00001.avro";
    private static final String CHUNK_PATH2 = "log/00/2020/08/02/2300/00002.avro";

    private BlobContainerAsyncClient mockContainer;
    private ChunkFactory mockChunkFactory;
    private Chunk mockChunk0;
    private Chunk mockChunk1;
    private Chunk mockChunk2;
    private ChangefeedCursor segmentCursor;

    @BeforeEach
    public void setup() {
        mockContainer = mock(BlobContainerAsyncClient.class);
        mockChunkFactory = mock(ChunkFactory.class);
        mockChunk0 = mock(Chunk.class);
        mockChunk1 = mock(Chunk.class);
        mockChunk2 = mock(Chunk.class);

        Supplier<Mono<PagedResponse<BlobItem>>> chunkSupplier = () -> Mono.just(new PagedResponseBase<>(null, 200, null,
            Arrays.asList(
                new BlobItem().setName(CHUNK_PATH0)
                    .setProperties(new BlobItemProperties().setContentLength(Long.MAX_VALUE)),
                new BlobItem().setName(CHUNK_PATH1)
                    .setProperties(new BlobItemProperties().setContentLength(Long.MAX_VALUE)),
                new BlobItem().setName(CHUNK_PATH2)
                    .setProperties(new BlobItemProperties().setContentLength(Long.MAX_VALUE))),
            null, null));

        PagedFlux<BlobItem> mockPagedFlux = new PagedFlux<>(chunkSupplier);

        segmentCursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null)
            .toShardCursor(CURRENT_SHARD_PATH0);

        when(mockContainer.listBlobs(any(ListBlobsOptions.class))).thenReturn(mockPagedFlux);

        when(mockChunkFactory.getChunk(eq(CHUNK_PATH0), anyLong(), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk0);
        when(mockChunkFactory.getChunk(eq(CHUNK_PATH1), anyLong(), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk1);
        when(mockChunkFactory.getChunk(eq(CHUNK_PATH2), anyLong(), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk2);

        when(mockChunk0.getEvents()).thenReturn(Flux.fromIterable(getMockEventWrappers(CHUNK_PATH0)));
        when(mockChunk1.getEvents()).thenReturn(Flux.fromIterable(getMockEventWrappers(CHUNK_PATH1)));
        when(mockChunk2.getEvents()).thenReturn(Flux.fromIterable(getMockEventWrappers(CHUNK_PATH2)));
    }

    /* Tests no user cursor. */
    @Test
    public void getEventsMin() {
        ShardFactory shardFactory = new ShardFactory(mockChunkFactory, mockContainer);
        Shard shard = shardFactory.getShard(CURRENT_SHARD_PATH0, segmentCursor, null);

        StepVerifier.create(shard.getEvents().index())
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 2))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 2))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH2, 1234, 0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH2, 1234, 1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH2, 1234, 2))
            .verifyComplete();

        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer).listBlobs(options.capture());
        assertEquals(CURRENT_SHARD_PATH0, options.getValue().getPrefix());
        verify(mockChunkFactory).getChunk(CHUNK_PATH0, Long.MAX_VALUE, segmentCursor, 0, 0);
        verify(mockChunkFactory).getChunk(CHUNK_PATH1, Long.MAX_VALUE, segmentCursor, 0, 0);
        verify(mockChunkFactory).getChunk(CHUNK_PATH2, Long.MAX_VALUE, segmentCursor, 0, 0);
        verify(mockChunk0).getEvents();
        verify(mockChunk1).getEvents();
        verify(mockChunk2).getEvents();
    }

    /* Tests user cursor. All we want to test here is that we only call chunk.getEvents if it is equal to or after the chunk of interest. */
    @ParameterizedTest
    @MethodSource("getEventsCursorSupplier")
    public void getEventsCursor(String chunkPath, long blockOffset, long eventIndex) {
        ShardCursor userCursor = new ShardCursor(chunkPath, blockOffset, eventIndex);

        ShardFactory shardFactory = new ShardFactory(mockChunkFactory, mockContainer);
        Shard shard = shardFactory.getShard(CURRENT_SHARD_PATH0, segmentCursor, userCursor);

        StepVerifier.Step<Tuple2<Long, BlobChangefeedEventWrapper>> sv = StepVerifier.create(shard.getEvents().index());

        if (Objects.equals(chunkPath, CHUNK_PATH0)) {
            sv = sv.assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 0))
                .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 1))
                .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH0, 1234, 2));
        }
        if (Objects.equals(chunkPath, CHUNK_PATH0) || Objects.equals(chunkPath, CHUNK_PATH1)) {
            sv = sv.assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 0))
                .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 1))
                .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH1, 1234, 2));
        }
        if (Objects.equals(chunkPath, CHUNK_PATH0)
            || Objects.equals(chunkPath, CHUNK_PATH1)
            || Objects.equals(chunkPath, CHUNK_PATH2)) {
            sv = sv.assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH2, 1234, 0))
                .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH2, 1234, 1))
                .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), CHUNK_PATH2, 1234, 2));
        }

        sv.verifyComplete();

        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer).listBlobs(options.capture());
        assertEquals(CURRENT_SHARD_PATH0, options.getValue().getPrefix());

        if (Objects.equals(chunkPath, CHUNK_PATH0)) {
            verify(mockChunkFactory).getChunk(CHUNK_PATH0, Long.MAX_VALUE, segmentCursor, blockOffset, eventIndex);
            verify(mockChunkFactory).getChunk(CHUNK_PATH1, Long.MAX_VALUE, segmentCursor, 0, 0);
            verify(mockChunkFactory).getChunk(CHUNK_PATH2, Long.MAX_VALUE, segmentCursor, 0, 0);
            verify(mockChunk0).getEvents();
            verify(mockChunk1).getEvents();
            verify(mockChunk2).getEvents();
        } else if (Objects.equals(chunkPath, CHUNK_PATH1)) {
            verify(mockChunkFactory).getChunk(CHUNK_PATH1, Long.MAX_VALUE, segmentCursor, blockOffset, eventIndex);
            verify(mockChunkFactory).getChunk(CHUNK_PATH2, Long.MAX_VALUE, segmentCursor, 0, 0);
            verify(mockChunk1).getEvents();
            verify(mockChunk2).getEvents();
        } else if (Objects.equals(chunkPath, CHUNK_PATH2)) {
            verify(mockChunkFactory).getChunk(CHUNK_PATH2, Long.MAX_VALUE, segmentCursor, blockOffset, eventIndex);
            verify(mockChunk2).getEvents();
        }
    }

    private static Stream<Arguments> getEventsCursorSupplier() {
        // chunkPath | blockOffset | eventIndex
        return Stream.of(Arguments.of(CHUNK_PATH0, 1234, 10), Arguments.of(CHUNK_PATH1, 5678, 5),
            Arguments.of(CHUNK_PATH2, 435, 9));
    }

    private static void verifyWrapper(BlobChangefeedEventWrapper wrapper, long index, String chunkPath,
        long blockOffset, long blockIndex) {
        assertEquals(URL_HOST, wrapper.getCursor().getUrlHost());
        assertEquals(END_TIME, wrapper.getCursor().getEndTime());
        assertEquals(SEGMENT_PATH, wrapper.getCursor().getCurrentSegmentCursor().getSegmentPath());
        assertEquals(CURRENT_SHARD_PATH0, wrapper.getCursor().getCurrentSegmentCursor().getCurrentShardPath());
        assertNotNull(wrapper.getCursor().getCurrentSegmentCursor().getShardCursors());
        assertEquals(1, wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().size());

        /* Make sure the cursor associated with the event is also correct. */
        assertEquals(chunkPath,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath());
        assertEquals(blockOffset,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset());
        assertEquals(blockIndex,
            wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex());

        /* Make sure the event in the wrapper is what was expected. */
        assertEquals(MockedChangefeedResources.getMockBlobChangefeedEvent((int) (index % 3)), wrapper.getEvent());
    }

    List<BlobChangefeedEventWrapper> getMockEventWrappers(String chunkPath) {
        List<BlobChangefeedEventWrapper> mockEventWrappers = new ArrayList<>();
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(0),
            segmentCursor.toEventCursor(chunkPath, 1234, 0)));
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(1),
            segmentCursor.toEventCursor(chunkPath, 1234, 1)));
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(2),
            segmentCursor.toEventCursor(chunkPath, 1234, 2)));
        return mockEventWrappers;
    }
}
