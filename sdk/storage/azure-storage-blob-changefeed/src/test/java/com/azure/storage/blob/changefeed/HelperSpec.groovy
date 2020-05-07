package com.azure.storage.blob.changefeed

import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventData
import com.azure.storage.blob.changefeed.models.BlobChangefeedEventType
import com.azure.storage.blob.models.BlobType
import spock.lang.Specification

import java.time.OffsetDateTime

class HelperSpec extends Specification {

    List<BlobChangefeedEvent> mockEvents

    def setup() {
        mockEvents = new LinkedList<>()
        for (int i = 0; i < 10; i++) {
            BlobChangefeedEvent event = getMockBlobChangefeedEvent(i)
            mockEvents.add(event)
        }
    }

    BlobChangefeedEvent getMockBlobChangefeedEvent(int index) {
        return new BlobChangefeedEvent(
            "topic",
            "subject",
            BlobChangefeedEventType.BLOB_CREATED,
            OffsetDateTime.now(),
            "id" + index, /* Just to verify later. */
            getMockBlobChangefeedEventData(),
            0,
            "metadataVersion"
        )
    }

    BlobChangefeedEventData getMockBlobChangefeedEventData() {
        return new BlobChangefeedEventData(
            "PutBlob",
            "clientRequestId",
            "requestId",
            "etag",
            "application/octet-stream",
            100,
            BlobType.BLOCK_BLOB,
            0,
            "destinationUrl",
            "sourceUrl",
            "",
            false,
            "sequencer"
        )
    }
}
