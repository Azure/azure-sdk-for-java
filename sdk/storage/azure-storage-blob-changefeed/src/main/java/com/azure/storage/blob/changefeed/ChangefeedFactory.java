// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.TimeUtils;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.time.OffsetDateTime;

/**
 * Factory class for {@link ChangefeedFactory}.
 */
class ChangefeedFactory {

    private final ClientLogger logger = new ClientLogger(ChangefeedFactory.class);

    private final SegmentFactory segmentFactory;
    private final BlobContainerAsyncClient client;

    /**
     * Creates a SegmentFactory with the designated factories.
     */
    ChangefeedFactory(SegmentFactory segmentFactory, BlobContainerAsyncClient client) {
        StorageImplUtils.assertNotNull("segmentFactory", segmentFactory);
        StorageImplUtils.assertNotNull("client", client);
        this.segmentFactory = segmentFactory;
        this.client = client;
    }

    /**
     * Gets a new instance of a Changefeed.
     */
    Changefeed getChangefeed(OffsetDateTime startTime, OffsetDateTime endTime) {
        OffsetDateTime start = startTime == null ? OffsetDateTime.MIN : startTime;
        OffsetDateTime end = endTime == null ? OffsetDateTime.MAX : endTime;

        return new Changefeed(this.client, start, end, null, segmentFactory);
    }

    /**
     * Gets a new instance of a Changefeed.
     */
    Changefeed getChangefeed(String cursor) {
        StorageImplUtils.assertNotNull("cursor", cursor);

        ChangefeedCursor userCursor = ChangefeedCursor.deserialize(cursor, logger);
        OffsetDateTime start = TimeUtils.convertPathToTime(userCursor.getCurrentSegmentCursor().getSegmentPath());
        OffsetDateTime end = userCursor.getEndTime();

        return new Changefeed(this.client, start, end, userCursor, segmentFactory);
    }
}
