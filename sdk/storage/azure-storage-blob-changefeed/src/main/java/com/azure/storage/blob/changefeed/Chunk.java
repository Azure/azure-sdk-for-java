// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.internal.avro.implementation.AvroParser;
import reactor.core.publisher.Flux;

/**
 * Gets events for a chunk.
 * A chunk is an avro formatted append blob that contains change feed events.
 */
class Chunk {

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Chunk event data location. */
    private final String chunkPath;

    /* Cursor associated with parent shard. */
    private final BlobChangefeedCursor shardCursor;

    private long eventNumber;

    private final BlobChangefeedCursor userCursor;

    private AvroParser parser;

    Chunk(BlobContainerAsyncClient client, String chunkPath, BlobChangefeedCursor shardCursor,
        BlobChangefeedCursor userCursor) {
        this.client = client;
        this.chunkPath = chunkPath;
        this.shardCursor = shardCursor;
        this.eventNumber = -1;
        this.userCursor = userCursor;
        this.parser = new AvroParser();
    }

    Flux<BlobChangefeedEventWrapper> getEvents() {
        /* Download Avro data file. */
        /* TODO (gapra): Lazy download. */
        return client.getBlobAsyncClient(chunkPath)
            .download()
            .concatMap(this.parser::parse)
            .map(object -> {
                BlobChangefeedCursor eventCursor = shardCursor.toEventCursor(eventNumber++);
                return new BlobChangefeedEventWrapper(BlobChangefeedEvent.fromRecord(object), eventCursor);
            });
    }
}
