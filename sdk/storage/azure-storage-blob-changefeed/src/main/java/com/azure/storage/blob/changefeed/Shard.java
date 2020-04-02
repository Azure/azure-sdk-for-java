// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedEventWrapper;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import reactor.core.publisher.Flux;

/**
 * Gets events for a shard (parallel writing in a single segment).
 */
class Shard  {

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Segment manifest location. */
    private final String shardPath;

    /* Cursor associated with parent segment. */
    private final BlobChangefeedCursor segmentCursor;

    /* User provided changefeed cursor. */
    private final BlobChangefeedCursor userCursor;

    /**
     * Creates a shard with the associated path and cursors.
     */
    Shard(BlobContainerAsyncClient client, String shardPath, BlobChangefeedCursor segmentCursor,
        BlobChangefeedCursor userCursor) {
        this.client = client;
        this.shardPath = shardPath;
        this.segmentCursor = segmentCursor;
        this.userCursor = userCursor;
    }

    /**
     * Get all the events for the Shard.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        return listChunks()
            .concatMap(this::getEventsForChunk);
    }

    /**
     * Lists chunks in a shard.
     */
    private Flux<BlobItem> listChunks() {
        return this.client.listBlobs(new ListBlobsOptions().setPrefix(shardPath));
    }

    /**
     * Get events for a chunk.
     */
    private Flux<BlobChangefeedEventWrapper> getEventsForChunk(BlobItem chunk) {
        return new Chunk(client, chunk.getName(), segmentCursor.toChunkCursor(chunk.getName()), userCursor).getEvents();
    }
}
