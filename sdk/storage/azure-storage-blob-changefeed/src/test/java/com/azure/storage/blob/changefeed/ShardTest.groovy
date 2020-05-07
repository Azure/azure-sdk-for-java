package com.azure.storage.blob.changefeed

import com.azure.core.http.rest.PagedFlux
import com.azure.core.http.rest.PagedResponse
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.ListBlobsOptions
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

import java.util.function.Supplier

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class ShardTest extends HelperSpec {

    BlobContainerAsyncClient mockContainer
    BlobAsyncClient mockBlob
    ChunkFactory mockChunkFactory
    Chunk mockChunk0
    Chunk mockChunk1
    Chunk mockChunk2

    String shardPath = "shardPath"
    ChangefeedCursor segmentCursor


    def setup() {
        mockContainer = mock(BlobContainerAsyncClient.class)
        mockBlob = mock(BlobAsyncClient.class)
        mockChunkFactory = mock(ChunkFactory.class)
        mockChunk0 = mock(Chunk.class)
        mockChunk1 = mock(Chunk.class)
        mockChunk2 = mock(Chunk.class)

        def mockPagedResponse = mock(PagedResponse.class)
        when(mockPagedResponse.getValue())
            .thenReturn(List.of(new BlobItem().setName("chunk0"), new BlobItem().setName("chunk1"), new BlobItem().setName("chunk2")))
        def mockSupplier = mock(Supplier.class)
        when(mockSupplier.get())
            .thenReturn(Mono.just(mockPagedResponse))
        def mockPagedFlux = new PagedFlux(mockSupplier)

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


    /* Tests no user cursor. */
    def "getEvents min"() {
        setup:

        when:
        ShardFactory shardFactory = new ShardFactory(mockChunkFactory)
        Shard shard = shardFactory.getShard(mockContainer, shardPath, segmentCursor, null)

        def sv = StepVerifier.create(shard.getEvents())

        then:

        sv.expectNextCount(9)
        .verifyComplete()

    }

}
