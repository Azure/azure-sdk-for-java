package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;

import java.time.OffsetDateTime;

/**
 * Factory class for {@link ChangefeedFactory}.
 */
class ChangefeedFactory {

    private final SegmentFactory segmentFactory;

    /**
     * Creates a default instance of the ChangefeedFactory.
     */
    ChangefeedFactory() {
        this.segmentFactory = new SegmentFactory();
    }

    /**
     * Creates a SegmentFactory with the designated factories.
     */
    ChangefeedFactory(SegmentFactory segmentFactory) {
        this.segmentFactory = segmentFactory;
    }

    /**
     * Gets a new instance of a Changefeed.
     */
    Changefeed getChangefeed(BlobContainerAsyncClient client, OffsetDateTime startTime, OffsetDateTime endTime) {
        return new Changefeed(client, startTime, endTime, segmentFactory);
    }

    /**
     * Gets a new instance of a Changefeed.
     */
    Changefeed getChangefeed(BlobContainerAsyncClient client, String cursor) {
        return new Changefeed(client, cursor, segmentFactory);
    }
}
