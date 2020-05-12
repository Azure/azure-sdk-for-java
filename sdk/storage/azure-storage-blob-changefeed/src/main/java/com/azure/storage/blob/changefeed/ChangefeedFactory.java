package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.common.implementation.StorageImplUtils;

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
        StorageImplUtils.assertNotNull("client", client);

        OffsetDateTime start = startTime == null ? OffsetDateTime.MIN : startTime;
        OffsetDateTime end = endTime == null ? OffsetDateTime.MAX : endTime;

        return new Changefeed(client, start, end, null, segmentFactory);
    }

    /**
     * Gets a new instance of a Changefeed.
     */
    Changefeed getChangefeed(BlobContainerAsyncClient client, String cursor) {
        StorageImplUtils.assertNotNull("client", client);
        StorageImplUtils.assertNotNull("cursor", cursor);

        ChangefeedCursor userCursor = ChangefeedCursor.deserialize(cursor);
        OffsetDateTime start = OffsetDateTime.parse(userCursor.getSegmentTime());
        OffsetDateTime end = OffsetDateTime.parse(userCursor.getEndTime());

        return new Changefeed(client, start, end, ChangefeedCursor.deserialize(cursor), segmentFactory);
    }
}
