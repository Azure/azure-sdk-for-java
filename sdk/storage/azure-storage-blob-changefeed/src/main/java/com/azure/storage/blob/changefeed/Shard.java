// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.blob.models.ListBlobsOptions;
import reactor.core.publisher.Flux;

class Shard  {

    /* Changefeed container */
    private final BlobContainerAsyncClient client;

    /* Segment manifest location. */
    private final String path;

    Shard(BlobContainerAsyncClient client, String path) {
        this.client = client;
        this.path = path;
    }

    Flux<BlobChangefeedEvent> getEvents() {
        /* Get all chunks in the shard. */
        return this.client.listBlobs(new ListBlobsOptions().setPrefix(path))
            /* Get events for each chunk. */
            .concatMap(chunk ->
                new Chunk(client, chunk.getName()).getEvents()
            );
    }
}
