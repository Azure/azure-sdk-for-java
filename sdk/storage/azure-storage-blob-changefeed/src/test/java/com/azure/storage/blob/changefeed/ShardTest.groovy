package com.azure.storage.blob.changefeed

import com.azure.core.http.rest.PagedFlux
import com.azure.core.http.rest.PagedResponse
import com.azure.core.http.rest.PagedResponseBase
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor

import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.ListBlobsOptions
import org.mockito.ArgumentCaptor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Unroll

import java.util.function.Supplier

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class ShardTest extends HelperSpec {

    BlobContainerAsyncClient mockContainer
    ChunkFactory mockChunkFactory
    Chunk mockChunk0
    Chunk mockChunk1
    Chunk mockChunk2

    String shardPath = "shardPath"
    ChangefeedCursor segmentCursor

    def setup() {
        mockContainer = mock(BlobContainerAsyncClient.class)
        mockChunkFactory = mock(ChunkFactory.class)
        mockChunk0 = mock(Chunk.class)
        mockChunk1 = mock(Chunk.class)
        mockChunk2 = mock(Chunk.class)

        Supplier<Mono<PagedResponse<BlobItem>>> chunkSupplier = new Supplier<Mono<PagedResponse<BlobItem>>>() {
            @Override
            Mono<PagedResponse<BlobItem>> get() {
                return Mono.just(new PagedResponseBase<>(
                    null, 200, null,
                   [new BlobItem().setName("chunk0"), new BlobItem().setName("chunk1"), new BlobItem().setName("chunk2")],
                    null, null))
            }
        }
        PagedFlux<BlobItem> mockPagedFlux = new PagedFlux<>(chunkSupplier)

        segmentCursor = new ChangefeedCursor("endTime", "segmentTime", shardPath, null, 0, 0)

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
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(0), segmentCursor.toChunkCursor(chunkPath).toEventCursor(1234, 0)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(1), segmentCursor.toChunkCursor(chunkPath).toEventCursor(1234, 1)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(2), segmentCursor.toChunkCursor(chunkPath).toEventCursor(1234, 2)))
        return mockEventWrappers
    }

    /* Tests no user cursor. */
    def "getEvents min"() {
        when:
        ShardFactory shardFactory = new ShardFactory(mockChunkFactory)
        Shard shard = shardFactory.getShard(mockContainer, shardPath, segmentCursor, null)

        def sv = StepVerifier.create(shard.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk0", 1234, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk0", 1234, 1) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk0", 1234, 2) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk1", 1234, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk1", 1234, 1) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk1", 1234, 2) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk2", 1234, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk2", 1234, 1) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk2", 1234, 2) })
            .verifyComplete()

        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer).listBlobs(options.capture()) || true
        options.getValue().getPrefix() == shardPath
        verify(mockChunkFactory).getChunk(mockContainer, "chunk0", segmentCursor.toChunkCursor("chunk0"), 0, 0) || true
        verify(mockChunkFactory).getChunk(mockContainer, "chunk1", segmentCursor.toChunkCursor("chunk1"), 0, 0) || true
        verify(mockChunkFactory).getChunk(mockContainer, "chunk2", segmentCursor.toChunkCursor("chunk2"), 0, 0) || true
        verify(mockChunk0).getEvents() || true
        verify(mockChunk1).getEvents() || true
        verify(mockChunk2).getEvents() || true
    }

    /* Tests user cursor. */
    @Unroll
    def "getEvents cursor"() {
        setup:
        ChangefeedCursor userCursor = new ChangefeedCursor("endTime", "segmentTime", shardPath, chunkPath, blockOffset, objectBlockIndex)
        when:
        ShardFactory shardFactory = new ShardFactory(mockChunkFactory)
        Shard shard = shardFactory.getShard(mockContainer, shardPath, segmentCursor, userCursor)

        def sv = StepVerifier.create(shard.getEvents().index())

        then:
        if (chunkPath == "chunk0") {
            sv = sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk0", 1234, 0) })
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk0", 1234, 1) })
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk0", 1234, 2) })
        }
        if (chunkPath == "chunk0" || chunkPath == "chunk1") {
            sv = sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk1", 1234, 0) })
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk1", 1234, 1) })
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk1", 1234, 2) })
        }
        if (chunkPath == "chunk0" || chunkPath == "chunk1" || chunkPath == "chunk2") {
            sv = sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk2", 1234, 0) })
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk2", 1234, 1) })
                .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), "chunk2", 1234, 2) })
        }
        sv.verifyComplete()

        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer).listBlobs(options.capture()) || true
        options.getValue().getPrefix() == shardPath

        if (chunkPath == "chunk0") {
            verify(mockChunkFactory).getChunk(mockContainer, "chunk0", segmentCursor.toChunkCursor("chunk0"), blockOffset, objectBlockIndex) || true
            verify(mockChunkFactory).getChunk(mockContainer, "chunk1", segmentCursor.toChunkCursor("chunk1"), 0, 0) || true
            verify(mockChunkFactory).getChunk(mockContainer, "chunk2", segmentCursor.toChunkCursor("chunk2"), 0, 0) || true
            verify(mockChunk0).getEvents() || true
            verify(mockChunk1).getEvents() || true
            verify(mockChunk2).getEvents() || true
        } else if (chunkPath == "chunk1") {
            verify(mockChunkFactory).getChunk(mockContainer, "chunk1", segmentCursor.toChunkCursor("chunk1"), blockOffset, objectBlockIndex) || true
            verify(mockChunkFactory).getChunk(mockContainer, "chunk2", segmentCursor.toChunkCursor("chunk2"), 0, 0) || true
            verify(mockChunk1).getEvents() || true
            verify(mockChunk2).getEvents() || true
        } else if (chunkPath == "chunk2") {
            verify(mockChunkFactory).getChunk(mockContainer, "chunk2", segmentCursor.toChunkCursor("chunk2"), blockOffset, objectBlockIndex) || true
            verify(mockChunk2).getEvents() || true
        }

        where:
        chunkPath | blockOffset | objectBlockIndex || _
        "chunk0"  | 1234        | 10               || _
        "chunk1"  | 5678        | 5                || _
        "chunk2"  | 435         | 9                || _
    }

    boolean verifyWrapper(BlobChangefeedEventWrapper wrapper, long index, String chunkPath, long blockOffset, long blockIndex) {
        boolean verify = true
        /* Make sure the cursor associated with the event is also correct. */
        verify &= wrapper.getCursor().getBlockOffset() == blockOffset
        verify &= wrapper.getCursor().getObjectBlockIndex() == blockIndex
        verify &= wrapper.getCursor().getChunkPath() == chunkPath
        /* Make sure the event in the wrapper is what was expected. */
        verify &= wrapper.getEvent().equals(mockEvents.get(index%3 as int))
        return verify
    }

}
