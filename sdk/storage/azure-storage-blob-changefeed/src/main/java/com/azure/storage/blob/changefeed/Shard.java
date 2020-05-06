// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.models.BlobItem;
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
    private final ChangefeedCursor userCursor;

    /**
     * Creates a shard with the associated path and cursors.
     */
    Shard(BlobContainerAsyncClient client, String shardPath, ChangefeedCursor segmentCursor,
        ChangefeedCursor userCursor) {
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
    private Flux<String> listChunks() {
        Flux<String> chunks = this.client.listBlobs(new ListBlobsOptions().setPrefix(shardPath))
            .map(BlobItem::getName);
        if (userCursor == null) {
            return chunks;
        } else {
            AtomicBoolean pass = new AtomicBoolean();
            return chunks.filter(chunkPath -> {
                if (pass.get()) {
                    return true;
                } else {
                    if (chunkPath.equals(userCursor.shardCursors.get(shardPath).getChunkPath())) {
                        pass.set(true);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
    }

    /**
     * Get events for a chunk.
     */
    Flux<BlobChangefeedEventWrapper> getEventsForChunk(String chunk) {
        return new Chunk(client, chunk, segmentCursor, userCursor).getEvents();
    }
}
