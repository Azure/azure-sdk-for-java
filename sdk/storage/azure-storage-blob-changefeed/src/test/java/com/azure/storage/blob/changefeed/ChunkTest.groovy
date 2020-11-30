package com.azure.storage.blob.changefeed

import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData
import com.azure.storage.internal.avro.implementation.AvroObject
import com.azure.storage.internal.avro.implementation.AvroReader
import com.azure.storage.internal.avro.implementation.AvroReaderFactory
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.OffsetDateTime

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class ChunkTest extends Specification {

    BlobContainerAsyncClient mockContainer
    BlobAsyncClient mockBlob
    AvroReaderFactory mockAvroReaderFactory
    AvroReader mockAvroReader
    BlobChunkedDownloaderFactory mockBlobLazyDownloaderFactory
    BlobChunkedDownloader mockBlobLazyDownloader

    String urlHost = 'testaccount.blob.core.windows.net'
    OffsetDateTime endTime = OffsetDateTime.MAX
    String segmentPath = "idx/segments/2020/08/02/2300/meta.json"
    String currentShardPath0 = "log/00/2020/08/02/2300/"
    String currentShardPath1 = "log/01/2020/08/02/2300/"
    String chunkPath0 = "log/00/2020/08/02/2300/00000.avro"
    String chunkPath1 = "log/00/2020/08/02/2300/00001.avro"
    String chunkPath2 = "log/01/2020/08/02/2300/00000.avro"

    List<BlobChangefeedEvent> mockEvents
    List<Map<String, Object>> mockRecords
    List<AvroObject> mockAvroObjects

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
        setupEvents()

        mockContainer = mock(BlobContainerAsyncClient.class)
        mockBlob = mock(BlobAsyncClient.class)
        mockAvroReaderFactory = mock(AvroReaderFactory.class)
        mockAvroReader = mock(AvroReader.class)
        mockBlobLazyDownloaderFactory = mock(BlobChunkedDownloaderFactory.class)
        mockBlobLazyDownloader = mock(BlobChunkedDownloader.class)

        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockBlob)
        when(mockBlobLazyDownloaderFactory.getBlobLazyDownloader(anyString(), anyLong(), anyLong()))
            .thenReturn(mockBlobLazyDownloader)
        /* The data returned by the lazy downloader does not matter since we're mocking avroObjects.  */
        when(mockBlobLazyDownloader.download())
            .thenReturn(Flux.empty())
        when(mockAvroReader.read())
            .thenReturn(Flux.fromIterable(mockAvroObjects))
    }

    /* Tests no user cursor. These tests check that the event cursor is properly populated. */
    def "getEvents min shard 0 chunk 0"() {
        setup:
        /* Cursor on shard 0, chunk 0 - basically when you first encounter a chunk in a shard. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)
        when(mockAvroReaderFactory.getAvroReader(any(Flux.class)))
            .thenReturn(mockAvroReader)

        when:
        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory)
        Chunk chunk = factory.getChunk(chunkPath0, Long.MAX_VALUE, chunkCursor, 0, 0)
        def sv = StepVerifier.create(chunk.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 3) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 5678, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 5678, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 5678, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 5678, 3) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 9101, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 9101, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 10000, 0) })
            .verifyComplete()

        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(chunkPath0, ChunkFactory.DEFAULT_BODY_SIZE, 0) || true
        verify(mockBlobLazyDownloader).download() || true
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty()) || true
        verify(mockAvroReader).read() || true
    }

    def "getEvents min shard 0 chunk 1"() {
        setup:
        /* Cursor on shard 0, chunk 1 - basically when you encounter a chunk in a shard after having already encountered a different chunk. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)
            .toEventCursor(chunkPath0, 9109, 1)
        when(mockAvroReaderFactory.getAvroReader(any(Flux.class)))
            .thenReturn(mockAvroReader)

        when:
        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory)
        Chunk chunk = factory.getChunk(chunkPath1, Long.MAX_VALUE, chunkCursor, 0, 0)
        def sv = StepVerifier.create(chunk.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 1234, 3) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 5678, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 5678, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 5678, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 5678, 3) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 9101, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 9101, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath1, 10000, 0) })
            .verifyComplete()

        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(chunkPath1, ChunkFactory.DEFAULT_BODY_SIZE, 0) || true
        verify(mockBlobLazyDownloader).download() || true
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty()) || true
        verify(mockAvroReader).read() || true
    }

    def "getEvents min shard 1 chunk 0"() {
        setup:
        /* Cursor on shard 1, chunk 0 - basically when you encounter a chunk in a shard after having already encountered a different shard. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)
            .toEventCursor(chunkPath0, 9109, 1)
            .toShardCursor(currentShardPath1)
        when(mockAvroReaderFactory.getAvroReader(any(Flux.class)))
            .thenReturn(mockAvroReader)

        when:
        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory)
        Chunk chunk = factory.getChunk(chunkPath2, Long.MAX_VALUE, chunkCursor, 0, 0)
        def sv = StepVerifier.create(chunk.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 1234, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 1234, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 1234, 3) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 5678, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 5678, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 5678, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 5678, 3) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 9101, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 9101, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard1(tuple2.getT2(), tuple2.getT1(), 10000, 0) })
            .verifyComplete()

        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(chunkPath2, ChunkFactory.DEFAULT_BODY_SIZE, 0) || true
        verify(mockBlobLazyDownloader).download() || true
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty()) || true
        verify(mockAvroReader).read() || true
    }

    /* Tests user cursor. Tests that a chunk can properly read events with the user cursor information. */
    @Unroll
    def "getEvents cursor"() {
        setup:
        /* Default chunk cursor on shard 0. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)
        when(mockAvroReaderFactory.getAvroReader(any(Flux.class), any(Flux.class), anyLong(), anyLong()))
            .thenReturn(mockAvroReader)
        when(mockBlobLazyDownloaderFactory.getBlobLazyDownloader(anyString(), anyLong()))
            .thenReturn(mockBlobLazyDownloader)

        when:
        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory)
        Chunk chunk = factory.getChunk(chunkPath0, Long.MAX_VALUE, chunkCursor, blockOffset, eventIndex)
        def sv = StepVerifier.create(chunk.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 1234, 3) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 5678, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 5678, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 5678, 2) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 5678, 3) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 9101, 0) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 9101, 1) })
            .expectNextMatches({ tuple2 -> verifyWrapperShard0(tuple2.getT2(), tuple2.getT1(), chunkPath0, 10000, 0) })
            .verifyComplete()

        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(chunkPath0, ChunkFactory.DEFAULT_HEADER_SIZE) || true
        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(chunkPath0, ChunkFactory.DEFAULT_BODY_SIZE, blockOffset) || true
        verify(mockBlobLazyDownloader, times(2)).download() || true
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty(), Flux.empty(), blockOffset, eventIndex) || true
        verify(mockAvroReader).read() || true

        where:
        blockOffset | eventIndex || _
        1234        | 0          || _
        1234        | 1          || _
        1234        | 2          || _
        1234        | 3          || _
        5678        | 0          || _
        5678        | 1          || _
        5678        | 2          || _
        5678        | 3          || _
        9101        | 0          || _
        9101        | 1          || _
    }

    def "getEvents invalid blockOffset"() {
        setup:
        /* Default chunk cursor on shard 0. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)

        Long chunkLength = 1000
        Long blockOffset = 1001

        Long eventIndex = 3

        when:
        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory)
        factory.getChunk(chunkPath0, chunkLength, chunkCursor, blockOffset, eventIndex)

        then:
        def e = thrown(IllegalArgumentException.class)
        e.getMessage() == "Cursor contains a blockOffset that is invalid."
    }

    def "getEvents blockOffset at end"() {
        setup:
        /* Default chunk cursor on shard 0. */
        ChangefeedCursor chunkCursor = new ChangefeedCursor(urlHost, endTime)
            .toSegmentCursor(segmentPath, null)
            .toShardCursor(currentShardPath0)

        Long chunkLength = 1001
        Long blockOffset = 1001

        Long eventIndex = 3

        when:
        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory)
        Chunk chunk = factory.getChunk(chunkPath0, chunkLength, chunkCursor, blockOffset, eventIndex)
        def sv = StepVerifier.create(chunk.getEvents().index())

        then:
        sv.expectComplete() /* Chunk should complete with 0 events. */
    }

    boolean verifyWrapperShard0(BlobChangefeedEventWrapper wrapper, long index, String chunkPath, long blockOffset, long blockIndex) {
        boolean verify = true
        verify &= wrapper.getCursor().getUrlHost() == urlHost
        verify &= wrapper.getCursor().getEndTime() == endTime
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getSegmentPath() == segmentPath
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath0
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors() != null
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().size() == 1
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunkPath
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == blockOffset
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == blockIndex
        /* Make sure the event in the wrapper is what was expected. */
        verify &= wrapper.getEvent().equals(mockEvents.get(index as int))
        return verify
    }

    boolean verifyWrapperShard1(BlobChangefeedEventWrapper wrapper, long index, long blockOffset, long blockIndex) {
        boolean verify = true
        verify &= wrapper.getCursor().getEndTime() == endTime
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getSegmentPath() == segmentPath
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getCurrentShardPath() == currentShardPath1
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors() != null
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().size() == 2
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getCurrentChunkPath() == chunkPath0
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getBlockOffset() == 9109
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(0).getEventIndex() == 1
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(1).getCurrentChunkPath() == chunkPath2
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(1).getBlockOffset() == blockOffset
        verify &= wrapper.getCursor().getCurrentSegmentCursor().getShardCursors().get(1).getEventIndex() == blockIndex
        /* Make sure the event in the wrapper is what was expected. */
        verify &= wrapper.getEvent().equals(mockEvents.get(index as int))
        return verify
    }

    def setupEvents() {
        mockEvents = new LinkedList<>()
        mockRecords = new LinkedList<>()
        mockAvroObjects = new LinkedList<>()
        for (int i = 0; i < 10; i++) {
            BlobChangefeedEvent event = MockedChangefeedResources.getMockBlobChangefeedEvent(i)
            mockEvents.add(event)
            /* These are the records emitted in the AvroParser. */
            /* This tests that the BlobChangefeedEvent objects are populated correctly from a record. */
            mockRecords.add(getMockChangefeedEventRecord(mockEvents.get(i)))
        }
        /* These are the wrapped records -> AvroObjects emitted by the AvroReader. */
        mockAvroObjects.add(new AvroObject(1234, 0, 1234, 1, mockRecords.get(0)))
        mockAvroObjects.add(new AvroObject(1234, 1, 1234, 2, mockRecords.get(1)))
        mockAvroObjects.add(new AvroObject(1234, 2, 1234, 3, mockRecords.get(2)))
        mockAvroObjects.add(new AvroObject(1234, 3, 5678, 0, mockRecords.get(3)))
        mockAvroObjects.add(new AvroObject(5678, 0, 5678, 1, mockRecords.get(4)))
        mockAvroObjects.add(new AvroObject(5678, 1, 5678, 2, mockRecords.get(5)))
        mockAvroObjects.add(new AvroObject(5678, 2, 5678, 3, mockRecords.get(6)))
        mockAvroObjects.add(new AvroObject(5678, 3, 9101, 0, mockRecords.get(7)))
        mockAvroObjects.add(new AvroObject(9101, 0, 9101, 1, mockRecords.get(8)))
        mockAvroObjects.add(new AvroObject(9101, 1, 10000, 0, mockRecords.get(9)))
    }

    Map<String, Object> getMockChangefeedEventRecord(BlobChangefeedEvent event) {
        Map<String, Object> cfEvent = new HashMap<>()
        cfEvent.put('$record', "BlobChangeEvent")
        cfEvent.put("schemaVersion", 1)
        cfEvent.put("topic", event.getTopic())
        cfEvent.put("subject", event.getSubject())
        cfEvent.put("eventType", event.getEventType().toString())
        cfEvent.put("eventTime", event.getEventTime().toString())
        cfEvent.put("id", event.getId())
        cfEvent.put("data", getMockChangefeedEventDataRecord(event.getData()))
        cfEvent.put("dataVersion", event.getDataVersion())
        cfEvent.put("metadataVersion", event.getMetadataVersion())
        return cfEvent
    }

    Map<String, Object> getMockChangefeedEventDataRecord(BlobChangefeedEventData data) {
        Map<String, Object> cfEventData = new HashMap<>()
        cfEventData.put('$record', "BlobChangeEventData")
        cfEventData.put("api", data.getApi().toString())
        cfEventData.put("clientRequestId", data.getClientRequestId())
        cfEventData.put("requestId", data.getRequestId())
        cfEventData.put("etag", data.getETag())
        cfEventData.put("contentType", data.getContentType())
        cfEventData.put("contentLength", data.getContentLength())
        cfEventData.put("contentOffset", data.getContentOffset())
        cfEventData.put("blobType", data.getBlobType().toString())
        cfEventData.put("destinationUrl", data.getDestinationUrl())
        cfEventData.put("sourceUrl", data.getSourceUrl())
        cfEventData.put("url", data.getBlobUrl())
        cfEventData.put("sequencer", data.getSequencer())
        cfEventData.put("recursive", data.isRecursive())
        return cfEventData
    }
}
