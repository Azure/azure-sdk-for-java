// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedEventWrapper;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import reactor.core.publisher.Flux;

class Shard  {

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Segment manifest location. */
    private final String shardPath;

    private final BlobChangefeedCursor segmentCursor;

    private final BlobChangefeedCursor userCursor;

    Shard(BlobContainerAsyncClient client, String shardPath, BlobChangefeedCursor segmentCursor,
        BlobChangefeedCursor userCursor) {
        this.client = client;
        this.shardPath = shardPath;
        this.segmentCursor = segmentCursor;
        this.userCursor = userCursor;
    }

    Flux<BlobChangefeedEventWrapper> getEvents() {
        return getChunksForShard()
            .concatMap(this::getEventsForChunk);
    }

    Flux<BlobItem> getChunksForShard() {
        return this.client.listBlobs(new ListBlobsOptions().setPrefix(shardPath));
    }

    Flux<BlobChangefeedEventWrapper> getEventsForChunk(BlobItem chunk) {
        return new Chunk(client, chunk.getName(), segmentCursor.toChunkCursor(chunk.getName()), userCursor).getEvents();
    }
}
