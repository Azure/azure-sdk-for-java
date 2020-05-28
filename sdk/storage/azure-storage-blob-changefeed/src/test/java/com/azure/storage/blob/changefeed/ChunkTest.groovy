package com.azure.storage.blob.changefeed

import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor

import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData
import com.azure.storage.internal.avro.implementation.AvroObject
import com.azure.storage.internal.avro.implementation.AvroReader
import com.azure.storage.internal.avro.implementation.AvroReaderFactory
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

class ChunkTest extends Specification {

    BlobContainerAsyncClient mockContainer
    BlobAsyncClient mockBlob
    AvroReaderFactory mockAvroReaderFactory
    AvroReader mockAvroReader
    BlobLazyDownloaderFactory mockBlobLazyDownloaderFactory
    BlobLazyDownloader mockBlobLazyDownloader

    String chunkPath = "chunkPath"
    ChangefeedCursor chunkCursor

    List<BlobChangefeedEvent> mockEvents
    List<Map<String, Object>> mockRecords
    List<AvroObject> mockAvroObjects

    def setup() {
        setupEvents()
        chunkCursor = new ChangefeedCursor("endTime", "segmentTime", "shardPath", chunkPath, 0, 0)

        mockContainer = mock(BlobContainerAsyncClient.class)
        mockBlob = mock(BlobAsyncClient.class)
        mockAvroReaderFactory = mock(AvroReaderFactory.class)
        mockAvroReader = mock(AvroReader.class)
        mockBlobLazyDownloaderFactory = mock(BlobLazyDownloaderFactory.class)
        mockBlobLazyDownloader = mock(BlobLazyDownloader.class)

        when(mockContainer.getBlobAsyncClient(anyString()))
            .thenReturn(mockBlob)
        when(mockBlobLazyDownloaderFactory.getBlobLazyDownloader(any(BlobAsyncClient.class), anyLong(), anyLong()))
            .thenReturn(mockBlobLazyDownloader)
        /* The data returned by the lazy downloader does not matter since we're mocking avroObjects.  */
        when(mockBlobLazyDownloader.download())
            .thenReturn(Flux.empty())
        when(mockAvroReader.readAvroObjects())
            .thenReturn(Flux.fromIterable(mockAvroObjects))
    }

    /* Tests no user cursor. */
    def "getEvents min"() {
        setup:
        when(mockAvroReaderFactory.getAvroReader(any(Flux.class)))
            .thenReturn(mockAvroReader)

        when:
        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory, mockContainer)
        Chunk chunk = factory.getChunk(chunkPath, chunkCursor, 0, 0)
        def sv = StepVerifier.create(chunk.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 1234, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 1234, 1) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 1234, 2) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 1234, 3) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 5678, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 5678, 1) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 5678, 2) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 5678, 3) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 9101, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 9101, 1) })
            .verifyComplete()

        verify(mockContainer).getBlobAsyncClient(chunkPath) || true
        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(mockBlob, ChunkFactory.DEFAULT_BODY_SIZE, 0) || true
        verify(mockBlobLazyDownloader).download() || true
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty()) || true
        verify(mockAvroReader).readAvroObjects() || true
    }

    /* Tests user cursor. */
    @Unroll
    def "getEvents cursor"() {
        setup:
        when(mockAvroReaderFactory.getAvroReader(any(Flux.class), any(Flux.class), anyLong(), anyLong()))
            .thenReturn(mockAvroReader)
        when(mockBlobLazyDownloaderFactory.getBlobLazyDownloader(any(BlobAsyncClient.class), anyLong()))
            .thenReturn(mockBlobLazyDownloader)

        when:
        ChunkFactory factory = new ChunkFactory(mockAvroReaderFactory, mockBlobLazyDownloaderFactory, mockContainer)
        Chunk chunk = factory.getChunk(chunkPath, chunkCursor, blockOffset, objectBlockIndex)
        def sv = StepVerifier.create(chunk.getEvents().index())

        then:
        sv.expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 1234, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 1234, 1) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 1234, 2) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 1234, 3) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 5678, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 5678, 1) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 5678, 2) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 5678, 3) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 9101, 0) })
            .expectNextMatches({ tuple2 -> this.&verifyWrapper(tuple2.getT2(), tuple2.getT1(), 9101, 1) })
            .verifyComplete()

        verify(mockContainer).getBlobAsyncClient(chunkPath) || true
        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(mockBlob, ChunkFactory.DEFAULT_HEADER_SIZE) || true
        verify(mockBlobLazyDownloaderFactory).getBlobLazyDownloader(mockBlob, ChunkFactory.DEFAULT_BODY_SIZE, blockOffset) || true
        verify(mockBlobLazyDownloader, times(2)).download() || true
        verify(mockAvroReaderFactory).getAvroReader(Flux.empty(), Flux.empty(), blockOffset, objectBlockIndex) || true
        verify(mockAvroReader).readAvroObjects() || true

        where:
        blockOffset | objectBlockIndex || _
        1234        | 0                || _
        1234        | 1                || _
        1234        | 2                || _
        1234        | 3                || _
        5678        | 0                || _
        5678        | 1                || _
        5678        | 2                || _
        5678        | 3                || _
        9101        | 0                || _
        9101        | 1                || _
    }

    boolean verifyWrapper(BlobChangefeedEventWrapper wrapper, long index, long blockOffset, long blockIndex) {
        boolean verify = true
        verify &= wrapper.getCursor().getEndTime() == chunkCursor.getEndTime()
        verify &= wrapper.getCursor().getSegmentTime() == chunkCursor.getSegmentTime()
        verify &= wrapper.getCursor().getShardPath() == chunkCursor.getShardPath()
        verify &= wrapper.getCursor().getChunkPath() == chunkCursor.getChunkPath()

        verify &= wrapper.getCursor().getBlockOffset() == blockOffset
        verify &= wrapper.getCursor().getObjectBlockIndex() == blockIndex
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
        mockAvroObjects.add(new AvroObject(1234, 0, mockRecords.get(0)))
        mockAvroObjects.add(new AvroObject(1234, 1, mockRecords.get(1)))
        mockAvroObjects.add(new AvroObject(1234, 2, mockRecords.get(2)))
        mockAvroObjects.add(new AvroObject(1234, 3, mockRecords.get(3)))
        mockAvroObjects.add(new AvroObject(5678, 0, mockRecords.get(4)))
        mockAvroObjects.add(new AvroObject(5678, 1, mockRecords.get(5)))
        mockAvroObjects.add(new AvroObject(5678, 2, mockRecords.get(6)))
        mockAvroObjects.add(new AvroObject(5678, 3, mockRecords.get(7)))
        mockAvroObjects.add(new AvroObject(9101, 0, mockRecords.get(8)))
        mockAvroObjects.add(new AvroObject(9101, 1, mockRecords.get(9)))
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
        cfEventData.put("recursive", data.getRecursive())
        return cfEventData
    }
}
