// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.SegmentCursor;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SegmentTests {
    private BlobContainerAsyncClient mockContainer;
    private BlobAsyncClient mockBlob;
    private ShardFactory mockShardFactory;
    private Shard mockShard0;
    private Shard mockShard1;
    private Shard mockShard2;
    private ChangefeedCursor cfCursor;

    private static final String SHARD_PATH0 = "log/00/2020/03/25/0200/";
    private static final String SHARD_PATH1 = "log/01/2020/03/25/0200/";
    private static final String SHARD_PATH2 = "log/02/2020/03/25/0200/";

    private static final String URL_HOST = "testaccount.blob.core.windows.net";
    private static final OffsetDateTime END_TIME = OffsetDateTime.MAX;
    private static final String SEGMENT_PATH = "idx/segments/2020/03/25/0200/meta.json";

    @BeforeEach
    public void setup() {
        mockContainer = mock(BlobContainerAsyncClient.class);
        mockBlob = mock(BlobAsyncClient.class);
        mockShardFactory = mock(ShardFactory.class);
        mockShard0 = mock(Shard.class);
        mockShard1 = mock(Shard.class);
        mockShard2 = mock(Shard.class);

        cfCursor = new ChangefeedCursor(URL_HOST, END_TIME).toSegmentCursor(SEGMENT_PATH, null);

        when(mockContainer.getBlobAsyncClient(anyString())).thenReturn(mockBlob);

        when(mockBlob.download()).thenReturn(MockedChangefeedResources.readFile("segment_manifest.json", getClass()));

        when(mockShardFactory.getShard(eq(SHARD_PATH0), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
            .thenReturn(mockShard0);
        when(mockShardFactory.getShard(eq(SHARD_PATH1), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
            .thenReturn(mockShard1);
        when(mockShardFactory.getShard(eq(SHARD_PATH2), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
            .thenReturn(mockShard2);

        when(mockShard0.getEvents()).thenReturn(Flux.fromIterable(getMockEventWrappers(SHARD_PATH0)));
        when(mockShard1.getEvents()).thenReturn(Flux.fromIterable(getMockEventWrappers(SHARD_PATH1)));
        when(mockShard2.getEvents()).thenReturn(Flux.fromIterable(getMockEventWrappers(SHARD_PATH2)));
    }

    private List<BlobChangefeedEventWrapper> getMockEventWrappers(String shardPath) {
        List<BlobChangefeedEventWrapper> mockEventWrappers = new ArrayList<>(3);
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(0),
            cfCursor.toShardCursor(shardPath)));
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(1),
            cfCursor.toShardCursor(shardPath)));
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(2),
            cfCursor.toShardCursor(shardPath)));
        return mockEventWrappers;
    }

    @Test
    public void getEventsMin() {
        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory, mockContainer);
        Segment segment = segmentFactory.getSegment(SEGMENT_PATH, cfCursor, null);

        StepVerifier.create(segment.getEvents().index())
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH2))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH2))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH2))
            .verifyComplete();

        verify(mockContainer).getBlobAsyncClient(SEGMENT_PATH);
        verify(mockBlob).download();
        verify(mockShardFactory).getShard(SHARD_PATH0, cfCursor.toShardCursor(SHARD_PATH0), null);
        verify(mockShardFactory).getShard(SHARD_PATH1, cfCursor.toShardCursor(SHARD_PATH1), null);
        verify(mockShardFactory).getShard(SHARD_PATH2, cfCursor.toShardCursor(SHARD_PATH2), null);
        verify(mockShard0).getEvents();
        verify(mockShard1).getEvents();
        verify(mockShard2).getEvents();
    }

    @ParameterizedTest
    @MethodSource("getEventsCursorSupplier")
    public void getEventsCursor(int caseNumber, SegmentCursor userCursor) {
        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory, mockContainer);
        Segment segment = segmentFactory.getSegment(SEGMENT_PATH, cfCursor, userCursor);

        StepVerifier.create(segment.getEvents().index())
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH0))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH1))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH2))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH2))
            .assertNext(tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), SHARD_PATH2))
            .verifyComplete();

        verify(mockContainer).getBlobAsyncClient(SEGMENT_PATH);
        verify(mockBlob).download();

        /* This is the stuff that actually matters. */
        if (caseNumber == 1) {
            verify(mockShardFactory).getShard(SHARD_PATH0, cfCursor.toShardCursor(SHARD_PATH0),
                new ShardCursor(SHARD_PATH0 + "00000.avro", 1257, 84));
            verify(mockShard0).getEvents();
            verify(mockShardFactory).getShard(SHARD_PATH1, cfCursor.toShardCursor(SHARD_PATH1), null);
            verify(mockShard1).getEvents();
            verify(mockShardFactory).getShard(SHARD_PATH2, cfCursor.toShardCursor(SHARD_PATH2), null);
            verify(mockShard2).getEvents();
        }
        if (caseNumber == 2) {
            verify(mockShardFactory).getShard(SHARD_PATH0, cfCursor.toShardCursor(SHARD_PATH0),
                new ShardCursor(SHARD_PATH0 + "00000.avro", 2589, 3));
            verify(mockShard0).getEvents();
            verify(mockShardFactory).getShard(SHARD_PATH1, cfCursor.toShardCursor(SHARD_PATH1),
                new ShardCursor(SHARD_PATH1 + "00000.avro", 345789, 8));
            verify(mockShard1).getEvents();
            verify(mockShardFactory).getShard(SHARD_PATH2, cfCursor.toShardCursor(SHARD_PATH2), null);
            verify(mockShard2).getEvents();
        }
        if (caseNumber == 3) {
            verify(mockShardFactory).getShard(SHARD_PATH0, cfCursor.toShardCursor(SHARD_PATH0),
                new ShardCursor(SHARD_PATH0 + "00000.avro", 492, 67));
            verify(mockShard0).getEvents();
            verify(mockShardFactory).getShard(SHARD_PATH1, cfCursor.toShardCursor(SHARD_PATH1),
                new ShardCursor(SHARD_PATH1 + "00001.avro", 1257, 84));
            verify(mockShard1).getEvents();
            verify(mockShardFactory).getShard(SHARD_PATH2, cfCursor.toShardCursor(SHARD_PATH2),
                new ShardCursor(SHARD_PATH2 + "00002.avro", 5678, 6));
            verify(mockShard2).getEvents();
        }
    }

    private static Stream<Arguments> getEventsCursorSupplier() {
        // caseNumber | userCursor
        return Stream.of(
            /* Shard 0 should use the cursor and Shard 1 and 2 should pass in null. */
            Arguments.of(1,
                new SegmentCursor(SEGMENT_PATH, null).toShardCursor(SHARD_PATH0)
                    .toEventCursor(SHARD_PATH0 + "00000.avro", 1257, 84)),

            /* Shard 0 and 1 should use the cursor and Shard 2 should pass in null. */
            Arguments.of(2,
                new SegmentCursor(SEGMENT_PATH, null).toShardCursor(SHARD_PATH0)
                    .toEventCursor(SHARD_PATH0 + "00000.avro", 2589, 3)
                    .toShardCursor(SHARD_PATH1)
                    .toEventCursor(SHARD_PATH1 + "00000.avro", 345789, 8)),

            /* Shard 0, 1 and 2 should use the cursor. */
            Arguments.of(3,
                new SegmentCursor(SEGMENT_PATH, null).toShardCursor(SHARD_PATH0)
                    .toEventCursor(SHARD_PATH0 + "00000.avro", 492, 67)
                    .toShardCursor(SHARD_PATH1)
                    .toEventCursor(SHARD_PATH1 + "00001.avro", 1257, 84)
                    .toShardCursor(SHARD_PATH2)
                    .toEventCursor(SHARD_PATH2 + "00002.avro", 5678, 6)));
    }

    @Test
    public void segmentMetadataError() {
        when(mockBlob.download()).thenReturn(Flux.just(ByteBuffer.wrap("not json metadata".getBytes())));

        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory, mockContainer);
        Segment segment = segmentFactory.getSegment(SEGMENT_PATH, cfCursor, null);

        StepVerifier.create(segment.getEvents()).verifyError(UncheckedIOException.class);

        verify(mockContainer).getBlobAsyncClient(SEGMENT_PATH);
        verify(mockBlob).download();
    }

    private static void verifyWrapper(BlobChangefeedEventWrapper wrapper, long index, String shardPath) {
        assertEquals(MockedChangefeedResources.getMockBlobChangefeedEvent((int) (index % 3)), wrapper.getEvent());
        assertEquals(shardPath, wrapper.getCursor().getCurrentSegmentCursor().getCurrentShardPath());
    }
}
