// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;

/**
 * Factory class for {@link Shard}.
 */
class SegmentFactory {

    private final ShardFactory shardFactory;

    /**
     * Creates a default instance of the SegmentFactory.
     */
    SegmentFactory() {
        this.shardFactory = new ShardFactory();
    }

    /**
     * Creates a SegmentFactory with the designated factories.
     */
    SegmentFactory(ShardFactory shardFactory) {
        this.shardFactory = shardFactory;
    }

    /**
     * Gets a new instance of a Segment.
     */
    Segment getSegment(BlobContainerAsyncClient client, String segmentPath, ChangefeedCursor cfCursor,
        ChangefeedCursor userCursor) {
        return new Segment(client, segmentPath, cfCursor, userCursor, shardFactory);
    }
}
