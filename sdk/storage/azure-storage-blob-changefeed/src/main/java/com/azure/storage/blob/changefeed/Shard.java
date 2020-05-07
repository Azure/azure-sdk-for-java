// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;
import com.azure.storage.blob.models.ListBlobsOptions;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gets events for a shard (parallel writing in a single segment).
 */
class Shard  {

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Segment manifest location. */
    private final String shardPath;

    /* Cursor associated with parent segment. */
    private final ChangefeedCursor segmentCursor;

    /* User provided changefeed cursor. */
    private final ShardCursor userShardCursor;

    /* Chunk factory. */
    private final ChunkFactory chunkFactory;

    /**
     * Creates a shard with the associated path, cursors and factory.
     */
    Shard(BlobContainerAsyncClient client, String shardPath, ChangefeedCursor segmentCursor,
        ShardCursor userShardCursor, ChunkFactory chunkFactory) {
        this.client = client;
        this.shardPath = shardPath;
        this.segmentCursor = segmentCursor;
        this.userShardCursor = userShardCursor;
        this.chunkFactory = chunkFactory;
    }

    /**
     * Get all the events for the Shard.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        return listChunks()
            .concatMap(chunkPath -> {
                long blockOffset = 0;
                long objectBlockIndex = 0;
                /* If a user shard cursor was provided and it points to this chunk path,
                   the chunk should get events based off the blockOffset and objectBlockIndex. */
                if (userShardCursor != null && userShardCursor.getChunkPath().equals(chunkPath)) {
                    blockOffset = userShardCursor.getBlockOffset();
                    objectBlockIndex = userShardCursor.getObjectBlockIndex();
                }
                return chunkFactory.getChunk(client, chunkPath, segmentCursor, blockOffset, objectBlockIndex)
                    .getEvents();
            });
    }

    /**
     * Lists chunks in a shard.
     */
    private Flux<String> listChunks() {
        Flux<String> chunks = this.client.listBlobs(new ListBlobsOptions().setPrefix(shardPath))
            .map(blobItem ->
                blobItem.getName());
        if (userShardCursor == null) {
            return chunks;
        } else {
            /* Only passes through chunks equal to or after the desired chunk. */
            AtomicBoolean pass = new AtomicBoolean();
            return chunks.filter(chunkPath -> {
                if (pass.get()) {
                    return true;
                } else {
                    if (userShardCursor.getChunkPath().equals(chunkPath)) {
                        pass.set(true);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
    }
}
