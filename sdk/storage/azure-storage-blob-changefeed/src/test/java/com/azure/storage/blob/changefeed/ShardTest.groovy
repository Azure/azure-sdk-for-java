package com.azure.storage.blob.changefeed

import com.azure.core.http.rest.PagedFlux
import com.azure.core.http.rest.PagedResponse
import com.azure.core.http.rest.PagedResponseBase
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.BlobItemProperties
import com.azure.storage.blob.models.ListBlobsOptions
import org.mockito.ArgumentCaptor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.util.function.Supplier

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class ShardTest extends Specification {


    String urlHost = 'testaccount.blob.core.windows.net'
    OffsetDateTime endTime = OffsetDateTime.MAX
    String segmentPath = "idx/segments/2020/08/02/2300/meta.json"
    String currentShardPath0 = "log/00/2020/08/02/2300/"

    BlobContainerAsyncClient mockContainer
    ChunkFactory mockChunkFactory
    Chunk mockChunk0
    Chunk mockChunk1
    Chunk mockChunk2

    static String chunkPath0 = "log/00/2020/08/02/2300/00000.avro"
    static String chunkPath1 = "log/00/2020/08/02/2300/00001.avro"
    static String chunkPath2 = "log/00/2020/08/02/2300/00002.avro"

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
                   [new BlobItem().setName(chunkPath0).setProperties(new BlobItemProperties().setContentLength(Long.MAX_VALUE)), new BlobItem().setName(chunkPath1).setProperties(new BlobItemProperties().setContentLength(Long.MAX_VALUE)), new BlobItem().setName(chunkPath2).setProperties(new BlobItemProperties().setContentLength(Long.MAX_VALUE))],
                    null, null))
            }
        }
        PagedFlux<BlobItem> mockPagedFlux = new PagedFlux<>(chunkSupplier)

        segmentCursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)

        when(mockContainer.listBlobs(any(ListBlobsOptions.class)))
            .thenReturn(mockPagedFlux)

        when(mockChunkFactory.getChunk(eq(chunkPath0), anyLong(), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk0)
        when(mockChunkFactory.getChunk(eq(chunkPath1), anyLong(), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk1)
        when(mockChunkFactory.getChunk(eq(chunkPath2), anyLong(), any(ChangefeedCursor.class), anyLong(), anyLong()))
            .thenReturn(mockChunk2)

        when(mockChunk0.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers(chunkPath0)))
        when(mockChunk1.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers(chunkPath1)))
        when(mockChunk2.getEvents())
            .thenReturn(Flux.fromIterable(getMockEventWrappers(chunkPath2)))
    }

    /* Tests no user cursor. */
    def "getEvents min"() {
        when:
        ShardFactory shardFactory = new ShardFactory(mockChunkFactory, mockContainer)
        Shard shard = shardFactory.getShard(currentShardPath0, segmentCursor, null)

        def sv = StepVerifier.create(shard.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath2, 1234, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath2, 1234, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath2, 1234, 2) })
            .verifyComplete()

        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer).listBlobs(options.capture()) || true
        options.getValue().getPrefix() == currentShardPath0
        verify(mockChunkFactory).getChunk(chunkPath0, Long.MAX_VALUE, segmentCursor, 0, 0) || true
        verify(mockChunkFactory).getChunk(chunkPath1, Long.MAX_VALUE, segmentCursor, 0, 0) || true
        verify(mockChunkFactory).getChunk(chunkPath2, Long.MAX_VALUE, segmentCursor, 0, 0) || true
        verify(mockChunk0).getEvents() || true
        verify(mockChunk1).getEvents() || true
        verify(mockChunk2).getEvents() || true
    }

    /* Tests user cursor. All we want to test here is that we only call chunk.getEvents if it is equal to or after the chunk of interest. */
    @Unroll
    def "getEvents cursor"() {
        setup:
        ShardCursor userCursor = new ShardCursor(chunkPath as String, blockOffset, eventIndex)
        when:
        ShardFactory shardFactory = new ShardFactory(mockChunkFactory, mockContainer)
        Shard shard = shardFactory.getShard(currentShardPath0, segmentCursor, userCursor)

        def sv = StepVerifier.create(shard.getEvents().index())

        then:
        if (chunkPath == chunkPath0) {
            sv = sv.expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 0) })
                .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 1) })
                .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 2) })
        }
        if (chunkPath == chunkPath0 || chunkPath == chunkPath1) {
            sv = sv.expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 0) })
                .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 1) })
                .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 2) })
        }
        if (chunkPath == chunkPath0 || chunkPath == chunkPath1 || chunkPath == chunkPath2) {
            sv = sv.expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath2, 1234, 0) })
                .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath2, 1234, 1) })
                .expectNextMatches({ tuple2 -> verifyWrapper(tuple2.getT2(), tuple2.getT1(), chunkPath2, 1234, 2) })
        }
        sv.verifyComplete()

        ArgumentCaptor<ListBlobsOptions> options = ArgumentCaptor.forClass(ListBlobsOptions.class);
        verify(mockContainer).listBlobs(options.capture()) || true
        options.getValue().getPrefix() == currentShardPath0

        if (chunkPath == chunkPath0) {
            verify(mockChunkFactory).getChunk(chunkPath0, Long.MAX_VALUE, segmentCursor, blockOffset, eventIndex) || true
            verify(mockChunkFactory).getChunk(chunkPath1, Long.MAX_VALUE, segmentCursor, 0, 0) || true
            verify(mockChunkFactory).getChunk(chunkPath2, Long.MAX_VALUE, segmentCursor, 0, 0) || true
            verify(mockChunk0).getEvents() || true
            verify(mockChunk1).getEvents() || true
            verify(mockChunk2).getEvents() || true
        } else if (chunkPath == chunkPath1) {
            verify(mockChunkFactory).getChunk(chunkPath1, Long.MAX_VALUE, segmentCursor, blockOffset, eventIndex) || true
            verify(mockChunkFactory).getChunk(chunkPath2, Long.MAX_VALUE, segmentCursor, 0, 0) || true
            verify(mockChunk1).getEvents() || true
            verify(mockChunk2).getEvents() || true
        } else if (chunkPath == chunkPath2) {
            verify(mockChunkFactory).getChunk(chunkPath2, Long.MAX_VALUE, segmentCursor, blockOffset, eventIndex) || true
            verify(mockChunk2).getEvents() || true
        }

        where:
        chunkPath   | blockOffset | eventIndex || _
        chunkPath0  | 1234        | 10         || _
        chunkPath1  | 5678        | 5          || _
        chunkPath2  | 435         | 9          || _
    }

    boolean verifyWrapper(BlobChangefeedEventWrapper wrapper, long index, String chunkPath, long blockOffset, long blockIndex) {
        boolean verify = true
        verify &= wrapper.getCursor().getUrlHost() == urlHost
        verify &= wrapper.getCursor().getEndTime() == endTime
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getSegmentPath() == segmentPath
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath0
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors() != null
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().size() == 1

        /* Make sure the cursor associated with the event is also correct. */
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunkPath
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == blockOffset
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == blockIndex

        /* Make sure the event in the wrapper is what was expected. */
        verify &= wrapper.getEvent().equals(MockedChangefeedResources.getMockBlobChangefeedEvent(index%3 as int))
        return verify
    }

    List<BlobChangefeedEventWrapper> getMockEventWrappers(String chunkPath) {
        List<BlobChangefeedEventWrapper> mockEventWrappers = new LinkedList<>()
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(0), segmentCursor.toEventCursor(chunkPath, 1234, 0)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(1), segmentCursor.toEventCursor(chunkPath, 1234, 1)))
        mockEventWrappers.add(new BlobChangefeedEventWrapper(MockedChangefeedResources.getMockBlobChangefeedEvent(2), segmentCursor.toEventCursor(chunkPath, 1234, 2)))
        return mockEventWrappers
    }

}
