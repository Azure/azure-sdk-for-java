package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;

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
     * Gets a new instance of a Shard.
     */
    Shard getShard(BlobContainerAsyncClient client, String shardPath, ChangefeedCursor segmentCursor,
        ShardCursor userShardCursor) {
        return new Shard(client, shardPath, segmentCursor, userShardCursor, chunkFactory);
    }
}
