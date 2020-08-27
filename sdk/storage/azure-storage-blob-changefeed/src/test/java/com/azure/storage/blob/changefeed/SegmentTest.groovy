package com.azure.storage.blob.changefeed

import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.SegmentCursor
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.OffsetDateTime

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class SegmentTest extends Specification {

    BlobContainerAsyncClient mockContainer
    BlobAsyncClient mockBlob
    ShardFactory mockShardFactory
    Shard mockShard0
    Shard mockShard1
    Shard mockShard2

    static String shardPath0 = 'log/00/2020/03/25/0200/'
    static String shardPath1 = 'log/01/2020/03/25/0200/'
    static String shardPath2 = 'log/02/2020/03/25/0200/'

    String urlHost = 'testaccount.blob.core.windows.net'
    OffsetDateTime endTime = OffsetDateTime.MAX
    static String segmentPath = "idx/segments/2020/03/25/0200/meta.json"
    ChangefeedCursor cfCursor

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
        mockContainer = mock(BlobContainerAsyncClient.class)
        mockBlob = mock(BlobAsyncClient.class)
        mockShardFactory = mock(ShardFactory.class)
        mockShard0 = mock(Shard.class)
        mockShard1 = mock(Shard.class)
        mockShard2 = mock(Shard.class)

        cfCursor = new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)

        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockBlob)

        when(mockBlob.download())
            .thenReturn(MockedChangefeedResources.readFile("segment_manifest.json", getClass()))

        when(mockShardFactory.getShard(eq(shardPath0), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
            .thenReturn(mockShard0)
        when(mockShardFactory.getShard(eq(shardPath1), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
            .thenReturn(mockShard1)
        when(mockShardFactory.getShard(eq(shardPath2), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
            .thenReturn(mockShard2)

        when(mockShard0.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers(shardPath0)))
        when(mockShard1.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers(shardPath1)))
        when(mockShard2.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers(shardPath2)))
    }

    List<BlobChangefeedEventWrapper> getMockEventWrappers(String shardPath) {
        List<BlobChangefeedEventWrapper> mockEventWrappers = new LinkedList<>()
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(0), cfCursor.toShardCursor(shardPath)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(1), cfCursor.toShardCursor(shardPath)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(2), cfCursor.toShardCursor(shardPath)))
        return mockEventWrappers
    }

    def "getEvents min"() {
        when:
        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory, mockContainer)
        Segment segment = segmentFactory.getSegment(segmentPath, cfCursor, null)

        def sv = StepVerifier.create(segment.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath0)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath0)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath0)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath1)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath1)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath1)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath2)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath2)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath2)})
            .verifyComplete()

        verify(mockContainer).getBlobAsyncClient(segmentPath) || true
        verify(mockBlob).download() || true
        verify(mockShardFactory).getShard(shardPath0, cfCursor.toShardCursor(shardPath0), null) || true
        verify(mockShardFactory).getShard(shardPath1, cfCursor.toShardCursor(shardPath1), null) || true
        verify(mockShardFactory).getShard(shardPath2, cfCursor.toShardCursor(shardPath2), null) || true
        verify(mockShard0).getEvents() || true
        verify(mockShard1).getEvents() || true
        verify(mockShard2).getEvents() || true
    }

    @Unroll
    def "getEvents cursor"() {
        when:
        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory, mockContainer)
        Segment segment = segmentFactory.getSegment(segmentPath, cfCursor, userCursor)

        def sv = StepVerifier.create(segment.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath0) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath0) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath0) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath1)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath1)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath1)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath2)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath2)})
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), shardPath2)})

        sv.verifyComplete()


        verify(mockContainer).getBlobAsyncClient(segmentPath) || true
        verify(mockBlob).download() || true

        /* This is the stuff that actually matters. */
        if (caseNumber == 1) {
            verify(mockShardFactory).getShard(shardPath0, cfCursor.toShardCursor(shardPath0), new ShardCursor(shardPath0 + "00000.avro", 1257, 84)) || true
            verify(mockShard0).getEvents() || true
            verify(mockShardFactory).getShard(shardPath1, cfCursor.toShardCursor(shardPath1), null) || true
            verify(mockShard1).getEvents() || true
            verify(mockShardFactory).getShard(shardPath2, cfCursor.toShardCursor(shardPath2), null) || true
            verify(mockShard2).getEvents() || true
        }
        if (caseNumber == 2) {
            verify(mockShardFactory).getShard(shardPath0, cfCursor.toShardCursor(shardPath0), new ShardCursor(shardPath0 + "00000.avro", 2589, 3)) || true
            verify(mockShard0).getEvents() || true
            verify(mockShardFactory).getShard(shardPath1, cfCursor.toShardCursor(shardPath1), new ShardCursor(shardPath1 + "00000.avro", 345789, 8)) || true
            verify(mockShard1).getEvents() || true
            verify(mockShardFactory).getShard(shardPath2, cfCursor.toShardCursor(shardPath2), null) || true
            verify(mockShard2).getEvents() || true
        }
        if (caseNumber == 3) {
            verify(mockShardFactory).getShard(shardPath0, cfCursor.toShardCursor(shardPath0), new ShardCursor(shardPath0 + "00000.avro", 492, 67)) || true
            verify(mockShard0).getEvents() || true
            verify(mockShardFactory).getShard(shardPath1, cfCursor.toShardCursor(shardPath1), new ShardCursor(shardPath1 + "00001.avro", 1257, 84)) || true
            verify(mockShard1).getEvents() || true
            verify(mockShardFactory).getShard(shardPath2, cfCursor.toShardCursor(shardPath2), new ShardCursor(shardPath2 + "00002.avro", 5678, 6)) || true
            verify(mockShard2).getEvents() || true
        }

        where:
        caseNumber | userCursor                                                                                                                                                                                                                                                            || _
        1          | new SegmentCursor(segmentPath, null).toShardCursor(shardPath0).toEventCursor(shardPath0 + "00000.avro", 1257, 84)                                                                                                                                                           || _ /* Shard 0 should use the cursor and Shard 1 and 2 should pass in null. */
        2          | new SegmentCursor(segmentPath, null).toShardCursor(shardPath0).toEventCursor(shardPath0 + "00000.avro", 2589, 3).toShardCursor(shardPath1).toEventCursor(shardPath1 + "00000.avro", 345789, 8)                                                                              || _ /* Shard 0 and 1 should use the cursor and Shard 2 should pass in null. */
        3          | new SegmentCursor(segmentPath, null).toShardCursor(shardPath0).toEventCursor(shardPath0 + "00000.avro", 492, 67).toShardCursor(shardPath1).toEventCursor(shardPath1 + "00001.avro", 1257, 84).toShardCursor(shardPath2).toEventCursor(shardPath2 + "00002.avro", 5678, 6) || _ /* Shard 0, 1 and 2 should use the cursor. */
    }

    def "segment metadata error"() {
        setup:
        when(mockBlob.download())
            .thenReturn(Flux.just(ByteBuffer.wrap("not json metadata".getBytes())))

        when:
        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory, mockContainer)
        Segment segment = segmentFactory.getSegment(segmentPath, cfCursor, null)

        def sv = StepVerifier.create(segment.getEvents())

        then:
        sv.verifyError(UncheckedIOException.class)

        verify(mockContainer).getBlobAsyncClient(segmentPath) || true
        verify(mockBlob).download() || true
    }

    boolean verifyWrapper(BlobChangefeedEventWrapper wrapper, long index, String shardPath) {
        boolean verify = true
        verify &= wrapper.getEvent().equals(MockedChangefeedResources.getMockBlobChangefeedEvent(index%3 as int))
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getCurrentShardPath() == shardPath
        return verify
    }
}
