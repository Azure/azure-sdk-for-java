package com.azure.storage.blob.changefeed

import com.azure.core.http.rest.PagedFlux
import com.azure.core.http.rest.PagedResponse
import com.azure.core.http.rest.PagedResponseBase
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.ListBlobsOptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.util.function.Supplier

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class ShardTest extends HelperSpec {

    BlobContainerAsyncClient mockContainer
    ChunkFactory mockChunkFactory

    String shardPath = "shardPath"
    ChangefeedCursor segmentCursor


    def setup() {
        mockContainer = mock(BlobContainerAsyncClient.class)
        mockChunkFactory = mock(ChunkFactory.class)
        Chunk mockChunk0 = mock(Chunk.class)
        Chunk mockChunk1 = mock(Chunk.class)
        Chunk mockChunk2 = mock(Chunk.class)

        Supplier<Mono<PagedResponse<BlobItem>>> supplier = new Supplier<Mono<PagedResponse<BlobItem>>>() {
            @Override
            Mono<PagedResponse<BlobItem>> get() {
                return Mono.just(new PagedResponseBase<>(
                    null, 200, null,
                    List.of(new BlobItem().setName("chunk0"), new BlobItem().setName("chunk1"), new BlobItem().setName("chunk2")),
                    null, null))
            }
        }
        PagedFlux<BlobItem> mockPagedFlux = new PagedFlux<>( supplier )

        Map<String, ShardCursor> shardCursors = new HashMap<>()
        shardCursors.put(shardPath, null)
        segmentCursor = new ChangefeedCursor("endTime", "segmentTime", shardCursors, shardPath)

        when(mockContainer.listBlobs(any(ListBlobsOptions.class)))
            .thenReturn(mockPagedFlux)

        when(mockChunkFactory.getChunk(any(BlobContainerAsyncClient.class), eq("chunk0"), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk0)
        when(mockChunkFactory.getChunk(any(BlobContainerAsyncClient.class), eq("chunk1"), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk1)
        when(mockChunkFactory.getChunk(any(BlobContainerAsyncClient.class), eq("chunk2"), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk2)

        when(mockChunk0.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers("chunk0")))
        when(mockChunk1.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers("chunk1")))
        when(mockChunk2.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers("chunk2")))
    }

    List<BlobChangefeedEventWrapper> getMockEventWrappers(String chunkPath) {
        List<BlobChangefeedEventWrapper> mockEventWrappers = new LinkedList<>()
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(0), segmentCursor.toEventCursor(chunkPath, 1234, 0)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(1), segmentCursor.toEventCursor(chunkPath, 1234, 1)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(2), segmentCursor.toEventCursor(chunkPath, 1234, 2)))
        return mockEventWrappers
    }

    /*TODO (gapra) : Improve these tests to check events. */

    /* Tests no user cursor. */
    def "getEvents min"() {
        when:
        ShardFactory shardFactory = new ShardFactory(mockChunkFactory)
        Shard shard = shardFactory.getShard(mockContainer, shardPath, segmentCursor, null)

        def sv = StepVerifier.create(shard.getEvents())

        then:
        sv.expectNextCount(9)
        .verifyComplete()
    }

    /* Tests user cursor. */
    @Unroll
    def "getEvents cursor"() {
        setup:
        ShardCursor userShardCursor = new ShardCursor(chunkPath, blockOffset, objectBlockIndex)
        when:
        ShardFactory shardFactory = new ShardFactory(mockChunkFactory)
        Shard shard = shardFactory.getShard(mockContainer, shardPath, segmentCursor, userShardCursor)

        def sv = StepVerifier.create(shard.getEvents())

        then:
        sv.expectNextCount(offset)
            .verifyComplete()

        where:
        chunkPath | blockOffset | objectBlockIndex || offset
        "chunk0"  | 0           | 0                || 9
        "chunk1"  | 0           | 0                || 6
        "chunk2"  | 0           | 0                || 3
    }

}
