// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;
import com.azure.storage.blob.models.ListBlobsOptions;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that represents a Shard in Changefeed.
 *
 * A shard is a virtual directory that contains a number of chunks.
 *
 * The log files in each shardPath are guaranteed to contain mutually exclusive blobs, and can be consumed and
 * processed in parallel without violating the ordering of modifications per blob during the iteration.
 */
class Shard  {

    private final BlobContainerAsyncClient client; /* Changefeed container */
    private final String shardPath; /* Shard virtual directory path/prefix. */
    private final ChangefeedCursor changefeedCursor; /* Cursor associated with parent segment. */
    private final ShardCursor userCursor; /* User provided cursor. */
    private final ChunkFactory chunkFactory;

    /**
     * Creates a new Shard.
     */
    Shard(BlobContainerAsyncClient client, String shardPath, ChangefeedCursor changefeedCursor,
        ShardCursor userCursor, ChunkFactory chunkFactory) {
        this.client = client;
        this.shardPath = shardPath;
        this.changefeedCursor = changefeedCursor;
        this.userCursor = userCursor;
        this.chunkFactory = chunkFactory;
    }

    /**
     * Get events for the Shard.
     * @return A reactive stream of {@link BlobChangefeedEventWrapper}
     */
    Flux<BlobChangefeedEventWrapper> getEvents() {
        /* List relevant chunks. */
        return listChunks()
            .concatMap(tuple2 -> {
                /* Defaults for blockOffset and eventIndex. */
                long blockOffset = 0;
                long eventIndex = 0;
                /* If a user cursor was provided and it points to this chunk path, the chunk should get events based
                   off the blockOffset and eventIndex.
                   This just makes sure only the targeted chunkPath uses the blockOffset and eventIndex to
                   read events. Any subsequent chunk will read all of its events (i.e. blockOffset = 0). */
                if (userCursor != null && userCursor.getCurrentChunkPath().equals(tuple2.getT1())) {
                    blockOffset = userCursor.getBlockOffset();
                    eventIndex = userCursor.getEventIndex();
                }
                return chunkFactory.getChunk(tuple2.getT1(), tuple2.getT2(), changefeedCursor, blockOffset, eventIndex)
                    .getEvents();
            });
    }

    /**
     * Lists relevant chunks in a shard.
     * @return A reactive stream of chunks.
     */
    private Flux<Tuple2<String, Long>> listChunks() {
        /* Shard paths looks like $blobchangefeed/log/00/2020/03/25/0200/ */
        /* The underlying chunks will look like
           $blobchangefeed/log/00/2020/03/25/0200/0000.avro,
           $blobchangefeed/log/00/2020/03/25/0200/0001.avro,
           $blobchangefeed/log/00/2020/03/25/0200/0002.avro

           If the chunk indicated by the cursor is 0001.avro, we want the resulting flux to return
           $blobchangefeed/log/00/2020/03/25/0200/0001.avro,
           $blobchangefeed/log/00/2020/03/25/0200/0002.avro
           */
        Flux<Tuple2<String, Long>> chunks = client.listBlobs(new ListBlobsOptions().setPrefix(shardPath))
            .map(blobItem -> Tuples.of(blobItem.getName(), blobItem.getProperties().getContentLength()));
        /* If no user cursor was provided, just return all chunks without filtering. */
        if (userCursor == null) {
            return chunks;
        /* If a user cursor was provided, filter out chunks that come before the chunk specified in the cursor. */
        } else {
            return Flux.defer(() -> {
                AtomicBoolean pass = new AtomicBoolean(); /* Whether or not to pass the event through. */
                return chunks.filter(tuple2 -> {
                    if (pass.get()) {
                        return true;
                    } else {
                    /* If we hit the chunk specified in the user cursor, set pass to true and pass this chunk
                       and any subsequent chunks through. */
                        if (userCursor.getCurrentChunkPath().equals(tuple2.getT1())) {
                            pass.set(true); /* This allows us to pass subsequent chunks through.*/
                            return true; /* This allows us to pass this chunk through. */
                        } else {
                            return false;
                        }
                    }
                });
            });
        }
    }
}
