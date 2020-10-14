// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.SegmentCursor;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Factory class for {@link Shard}.
 */
class SegmentFactory {

    private final ShardFactory shardFactory;
    private final BlobContainerAsyncClient client;

    /**
     * Creates a SegmentFactory with the designated factories.
     */
    SegmentFactory(ShardFactory shardFactory, BlobContainerAsyncClient client) {
        StorageImplUtils.assertNotNull("shardFactory", shardFactory);
        StorageImplUtils.assertNotNull("client", client);
        this.shardFactory = shardFactory;
        this.client = client;
    }

    /**
     * Gets a new instance of a Segment.
     */
    Segment getSegment(String segmentPath, ChangefeedCursor changefeedCursor,
        SegmentCursor userCursor) {
        /* Validate parameters. */
        StorageImplUtils.assertNotNull("segmentPath", segmentPath);
        StorageImplUtils.assertNotNull("changefeedCursor", changefeedCursor);

        return new Segment(this.client, segmentPath, changefeedCursor, userCursor, shardFactory);
    }
}
