package com.azure.storage.blob.changefeed

import com.azure.core.util.FluxUtil
import com.azure.storage.blob.BlobAsyncClient
import com.azure.storage.blob.BlobContainerAsyncClient
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedCursor
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventType
import com.azure.storage.blob.models.BlobType
import org.mockito.Mockito
import reactor.test.StepVerifier
import reactor.util.function.Tuple2
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class ChunkTest extends Specification {

    BlobContainerAsyncClient mockContainer
    String blobName

    def setup() {
        blobName = "changefeed_1000.avro"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(blobName).getFile())
        Path path = Paths.get(f.getAbsolutePath())

        BlobAsyncClient mockBlob = Mockito.mock(BlobAsyncClient.class);
        Mockito.when(mockBlob.download())
            .thenReturn(FluxUtil.readFile(AsynchronousFileChannel.open(path, StandardOpenOption.READ)))

        mockContainer = Mockito.mock(BlobContainerAsyncClient.class)
        Mockito.when(mockContainer.getBlobAsyncClient(blobName))
            .thenReturn(mockBlob)
    }

    @Unroll
    def "getEvents min"() {
        setup:
        def shardCursor = new BlobChangefeedCursor("endTime", "segmentTime", "shardPath", "chunkPath", null)
        def userCursor = cursorOffset == 0 ? null : shardCursor.toEventCursor(cursorOffset - 1)
        Chunk c = new Chunk(mockContainer, blobName, shardCursor, userCursor)

        when:
        def sv = StepVerifier.create(
            c.getEvents()
                .index()
        )

        then:
        if (count > 0) {
            assert sv
                .expectNextMatches({ tuple -> this.&verifyTuple(tuple, shardCursor, cursorOffset) })
                .expectNextCount(count)
                .verifyComplete()
        } else {
            assert sv.verifyComplete()
        }


        where:
        cursorOffset || count
        0            || 999     /* All events */
        1            || 998
        101          || 898
        500          || 499
        1000         || 0       /* No events */
    }

    boolean verifyTuple(Tuple2<Long, BlobChangefeedEventWrapper> tuple2, BlobChangefeedCursor shardCursor, long cursorOffset) {
        def index = tuple2.getT1() + cursorOffset
        def wrapper = tuple2.getT2()
        def cursor = wrapper.getCursor()
        def event = wrapper.getEvent()
        def verify = true
        verify &= event.getTopic() == "/subscriptions/ba45b233-e2ef-4169-8808-49eb0d8eba0d/resourceGroups/XClient/providers/Microsoft.Storage/storageAccounts/seanchangefeedstage"
        verify &= event.getSubject() == "/blobServices/default/containers/test-container/blobs/" + index
        verify &= event.getEventType() == BlobChangefeedEventType.BLOB_CREATED
        verify &= event.getEventTime() != null
        verify &= event.getId() != null
        verify &= validData(event.getData())
        verify &= event.getDataVersion() == null
        verify &= event.getMetadataVersion() == null
        verify &= cursor.getEventIndex() == index
        verify &= shardCursorEqual(cursor, shardCursor)

        return verify
    }

    boolean shardCursorEqual(BlobChangefeedCursor cursor, BlobChangefeedCursor shardCursor) {
        return cursor.getEndTime().equals(shardCursor.getEndTime()) && cursor.getSegmentTime().equals(shardCursor.getSegmentTime()) && cursor.getShardPath().equals(shardCursor.getShardPath()) && cursor.getChunkPath().equals(shardCursor.getChunkPath())
    }

    boolean validData(BlobChangefeedEventData data) {
        def verify = true
        verify &= data.getApi() == "PutBlob"
        verify &= data.getClientRequestId() != null
        verify &= data.getRequestId() != null
        verify &= data.geteTag() != null
        verify &= data.getContentType() == "application/octet-stream"
        verify &= data.getContentLength() != null
        verify &= data.getBlobType() == BlobType.BLOCK_BLOB
        verify &= data.getContentOffset() == null
        verify &= data.getDestinationUrl() == null
        verify &= data.getSourceUrl() == null
        verify &= data.getBlobUrl() == ""
        verify &= data.getRecursive() == null
        verify &= data.getSequencer() != null
        return verify
    }
}
