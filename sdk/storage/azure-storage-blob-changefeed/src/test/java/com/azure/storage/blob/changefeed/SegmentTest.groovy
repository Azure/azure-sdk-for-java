package com.azure.storage.blob.changefeed

import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.fasterxml.jackson.core.JsonParseException
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.nio.ByteBuffer

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class SegmentTest extends HelperSpec {

    BlobContainerAsyncClient mockContainer
    BlobAsyncClient mockBlob
    ShardFactory mockShardFactory
    Shard mockShard0
    Shard mockShard1
    Shard mockShard2

    String segmentPath = "segmentPath"
    ChangefeedCursor cfCursor

    def setup() {
        mockContainer = mock(BlobContainerAsyncClient.class)
        mockBlob = mock(BlobAsyncClient.class)
        mockShardFactory = mock(ShardFactory.class)
        mockShard0 = mock(Shard.class)
        mockShard1 = mock(Shard.class)
        mockShard2 = mock(Shard.class)

        cfCursor = new ChangefeedCursor("endTime", "segmentTime", null, null, 0, 0)

        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockBlob)

        when(mockBlob.download())
            .thenReturn(readFile("segment_manifest.json"))

        when(mockShardFactory.getShard(any(BlobContainerAsyncClient.class), eq('log/00/2020/03/25/0200/'), any(ChangefeedCursor.class), nullable(ChangefeedCursor.class)))
            .thenReturn(mockShard0)
        when(mockShardFactory.getShard(any(BlobContainerAsyncClient.class), eq('log/01/2020/03/25/0200/'), any(ChangefeedCursor.class), nullable(ChangefeedCursor.class)))
            .thenReturn(mockShard1)
        when(mockShardFactory.getShard(any(BlobContainerAsyncClient.class), eq('log/02/2020/03/25/0200/'), any(ChangefeedCursor.class), nullable(ChangefeedCursor.class)))
            .thenReturn(mockShard2)

        when(mockShard0.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers('log/00/2020/03/25/0200/')))
        when(mockShard1.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers('log/01/2020/03/25/0200/')))
        when(mockShard2.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers('log/02/2020/03/25/0200/')))
    }

    List<BlobChangefeedEventWrapper> getMockEventWrappers(String shardPath) {
        List<BlobChangefeedEventWrapper> mockEventWrappers = new LinkedList<>()
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(0), cfCursor.toShardCursor(shardPath)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(1), cfCursor.toShardCursor(shardPath)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(2), cfCursor.toShardCursor(shardPath)))
        return mockEventWrappers
    }

    def "getEvents min"() {
        when:
        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory)
        Segment segment = segmentFactory.getSegment(mockContainer, segmentPath, cfCursor, null)

        def sv = StepVerifier.create(segment.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/00/2020/03/25/0200/')})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/00/2020/03/25/0200/')})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/00/2020/03/25/0200/')})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/01/2020/03/25/0200/')})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/01/2020/03/25/0200/')})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/01/2020/03/25/0200/')})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/02/2020/03/25/0200/')})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/02/2020/03/25/0200/')})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/02/2020/03/25/0200/')})
            .verifyComplete()

        verify(mockContainer).getBlobAsyncClient(segmentPath) || true
        verify(mockBlob).download() || true
        verify(mockShardFactory).getShard(mockContainer, 'log/00/2020/03/25/0200/', cfCursor.toShardCursor('log/00/2020/03/25/0200/'), null) || true
        verify(mockShardFactory).getShard(mockContainer, 'log/01/2020/03/25/0200/', cfCursor.toShardCursor('log/01/2020/03/25/0200/'), null) || true
        verify(mockShardFactory).getShard(mockContainer, 'log/02/2020/03/25/0200/', cfCursor.toShardCursor('log/02/2020/03/25/0200/'), null) || true
        verify(mockShard0).getEvents() || true
        verify(mockShard1).getEvents() || true
        verify(mockShard2).getEvents() || true
    }

    @Unroll
    def "getEvents cursor"() {
        when:
        ChangefeedCursor userCursor = new ChangefeedCursor("endTime", "segmentTime", shardPath, "somechunk", 56, 2)
        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory)
        Segment segment = segmentFactory.getSegment(mockContainer, segmentPath, cfCursor, userCursor)

        def sv = StepVerifier.create(segment.getEvents().index())

        then:
        if (shardPath == 'log/00/2020/03/25/0200/') {
            sv = sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/00/2020/03/25/0200/') })
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/00/2020/03/25/0200/') })
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/00/2020/03/25/0200/') })
        }
        if (shardPath == 'log/00/2020/03/25/0200/' || shardPath == 'log/01/2020/03/25/0200/') {
            sv = sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/01/2020/03/25/0200/')})
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/01/2020/03/25/0200/')})
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/01/2020/03/25/0200/')})

        }
        if (shardPath == 'log/00/2020/03/25/0200/' || shardPath == 'log/01/2020/03/25/0200/' || shardPath == 'log/02/2020/03/25/0200/') {
            sv = sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/02/2020/03/25/0200/')})
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/02/2020/03/25/0200/')})
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 'log/02/2020/03/25/0200/')})
        }
        sv.verifyComplete()


        verify(mockContainer).getBlobAsyncClient(segmentPath) || true
        verify(mockBlob).download() || true

        if (shardPath == 'log/00/2020/03/25/0200/') {
            verify(mockShardFactory).getShard(mockContainer, 'log/00/2020/03/25/0200/', cfCursor.toShardCursor('log/00/2020/03/25/0200/'), userCursor) || true
            verify(mockShard0).getEvents() || true
        }
        if (shardPath == 'log/00/2020/03/25/0200/' || shardPath == 'log/01/2020/03/25/0200/') {
            verify(mockShardFactory).getShard(mockContainer, 'log/01/2020/03/25/0200/', cfCursor.toShardCursor('log/01/2020/03/25/0200/'), userCursor) || true
            verify(mockShard1).getEvents() || true
        }
        if (shardPath == 'log/00/2020/03/25/0200/' || shardPath == 'log/01/2020/03/25/0200/' || shardPath == 'log/02/2020/03/25/0200/') {
            verify(mockShardFactory).getShard(mockContainer, 'log/02/2020/03/25/0200/', cfCursor.toShardCursor('log/02/2020/03/25/0200/'), userCursor) || true
            verify(mockShard2).getEvents() || true
        }

        where:
        shardPath                   || _
        'log/00/2020/03/25/0200/'   || _
        'log/01/2020/03/25/0200/'   || _
        'log/02/2020/03/25/0200/'   || _
    }

    def "segment metadata error"() {
        setup:
        when(mockBlob.download())
            .thenReturn(Flux.just(ByteBuffer.wrap("not json metadata".getBytes())))

        when:
        SegmentFactory segmentFactory = new SegmentFactory()
        Segment segment = segmentFactory.getSegment(mockContainer, segmentPath, null, null)

        def sv = StepVerifier.create(segment.getEvents())

        then:
        sv.verifyError(JsonParseException.class)

        verify(mockContainer).getBlobAsyncClient(segmentPath) || true
        verify(mockBlob).download() || true
    }

    boolean verifyWrapper(BlobChangefeedEventWrapper wrapper, long index, String shardPath) {
        boolean verify = true
        verify &= wrapper.getEvent().equals(mockEvents.get(index % 3 as int))
        verify &= wrapper.getCursor().getShardPath() == shardPath
        return verify
    }
}
