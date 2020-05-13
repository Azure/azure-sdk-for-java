package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Factory class for {@link Shard}.
 */
class ShardFactory {

    private final ChunkFactory chunkFactory;

    /**
     * Creates a default instance of the ShardFactory.
     */
    ShardFactory() {
        this.chunkFactory = new ChunkFactory();
    }

    /**
     * Creates a ShardFactory with the designated factories.
     */
    ShardFactory(ChunkFactory chunkFactory) {
        this.chunkFactory = chunkFactory;
    }

    /**
     * Gets a new instance of a shard.
     * @param client The changefeed container client.
     * @param shardPath The prefix of the shard virtual directory.
     * @param segmentCursor The parent segment cursor.
     * @param userCursor The cursor provided by the user.
     * @return {@link Shard}
     */
    Shard getShard(BlobContainerAsyncClient client, String shardPath, ChangefeedCursor segmentCursor,
        ChangefeedCursor userCursor) {
        /* Validate parameters. */
        StorageImplUtils.assertNotNull("client", client);
        StorageImplUtils.assertNotNull("shardPath", shardPath);
        StorageImplUtils.assertNotNull("segmentCursor", segmentCursor);

        return new Shard(client, shardPath, segmentCursor, userCursor, chunkFactory);
    }
}
