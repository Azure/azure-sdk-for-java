// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Factory class for {@link Shard}.
 */
class ShardFactory {

    private final ChunkFactory chunkFactory;
    private final BlobContainerAsyncClient client;

    /**
     * Creates a ShardFactory with the designated factories.
     */
    ShardFactory(ChunkFactory chunkFactory, BlobContainerAsyncClient client) {
        StorageImplUtils.assertNotNull("chunkFactory", chunkFactory);
        StorageImplUtils.assertNotNull("client", client);
        this.chunkFactory = chunkFactory;
        this.client = client;
    }

    /**
     * Gets a new instance of a shard.
     * @param shardPath The prefix of the shard virtual directory.
     * @param changefeedCursor The parent changefeed cursor.
     * @param userCursor The cursor provided by the user.
     * @return {@link Shard}
     */
    Shard getShard(String shardPath, ChangefeedCursor changefeedCursor, ShardCursor userCursor) {
        /* Validate parameters. */
        StorageImplUtils.assertNotNull("shardPath", shardPath);
        StorageImplUtils.assertNotNull("changefeedCursor", changefeedCursor);

        return new Shard(this.client, shardPath, changefeedCursor, userCursor, chunkFactory);
    }
}
