package com.azure.storage.blob.changefeed


import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class SegmentTest extends HelperSpec {

    BlobContainerAsyncClient mockContainer
    ShardFactory mockShardFactory

    String segmentPath = "segmentPath"
    ChangefeedCursor cfCursor

    def setup() {
        mockContainer = mock(BlobContainerAsyncClient.class)
        BlobAsyncClient mockBlob = mock(BlobAsyncClient.class)
        mockShardFactory = mock(ShardFactory.class)
        Shard mockShard0 = mock(Shard.class)
        Shard mockShard1 = mock(Shard.class)
        Shard mockShard2 = mock(Shard.class)

        cfCursor = new ChangefeedCursor("endTime", "segmentTime", null, null)

        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockBlob)

        when(mockBlob.download())
            .thenReturn(readFile("segment_manifest.json"))

        when(mockShardFactory.getShard(any(BlobContainerAsyncClient.class), eq('log/00/2020/03/25/0200/'), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
            .thenReturn(mockShard0)
        when(mockShardFactory.getShard(any(BlobContainerAsyncClient.class), eq('log/01/2020/03/25/0200/'), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
            .thenReturn(mockShard1)
        when(mockShardFactory.getShard(any(BlobContainerAsyncClient.class), eq('log/02/2020/03/25/0200/'), any(ChangefeedCursor.class), nullable(ShardCursor.class)))
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
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(0), cfCursor.toShardCursor(shardPath, null)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(1), cfCursor.toShardCursor(shardPath, null)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(2), cfCursor.toShardCursor(shardPath, null)))
        return mockEventWrappers
    }

    def "getEvents min"() {
        when:
        SegmentFactory segmentFactory = new SegmentFactory(mockShardFactory)
        Segment segment = segmentFactory.getSegment(mockContainer, segmentPath, cfCursor, null)

        def sv = StepVerifier.create(segment.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1())})
            .verifyComplete()
    }

    boolean verifyWrapper(BlobChangefeedEventWrapper wrapper, long index) {
        boolean verify = true
        wrapper.getCursor()

        return verify
    }





}
