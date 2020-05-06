// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ShardCursor;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.internal.avro.implementation.AvroObject;
import com.azure.storage.internal.avro.implementation.AvroReader;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class that represents a Chunk in Changefeed.
 *
 * A chunk is an append blob that contains avro encoded changefeed events.
 *
 * The log files in each chunkFilePath are guaranteed to contain mutually exclusive blobs.
 */
class Chunk {

    private final BlobAsyncClient client; /* Chunk blob */

    private final String chunkPath;

    private final ChangefeedCursor shardCursor; /* Cursor associated with parent shard. */

    private final ChangefeedCursor userCursor; /* Cursor provided by user. */
    private final ShardCursor userShardCursor; /* Shard cursor provided by user. */

    private long blockOffset;

    private long objectBlockIndex;

    /**
     * Creates a new Chunk with the associated path and cursors.
     */
    Chunk(BlobContainerAsyncClient client, String chunkPath, ChangefeedCursor shardCursor,
        ChangefeedCursor userCursor) {
        this.client = client.getBlobAsyncClient(chunkPath);
        this.chunkPath = chunkPath;
        this.shardCursor = shardCursor;

        this.userCursor = userCursor;
        this.userShardCursor = this.userCursor == null ? null : userCursor.shardCursors.get(shardCursor.shardPath);

        if (userCursor != null && this.userShardCursor != null) {
            this.blockOffset = userShardCursor.getBlockOffset();
            this.objectBlockIndex = userShardCursor.getObjectBlockIndex();
        }
    }

    Flux<BlobChangefeedEventWrapper> getEvents() {
        return readEvents()
            /* Map each object into an event. */
            .map(this::wrapObject);
    }

    private Flux<AvroObject> readEvents() {
        if (userCursor == null || userShardCursor == null) {
            /* Read events like normal. */
            Flux<ByteBuffer> avro = new BlobLazyDownloader(client, Constants.MB, 0).download();
            return AvroReader.readAvro(avro);
        } else {
            /* Read header and body separately. */
            Flux<ByteBuffer> avroHeader = new BlobLazyDownloader(client, 4 * Constants.KB, 0)
                .download();
            Flux<ByteBuffer> avroBody = new BlobLazyDownloader(client, Constants.MB, blockOffset)
                .download();

            AtomicBoolean pass = new AtomicBoolean();

            return AvroReader.readAvro(avroHeader, avroBody, blockOffset)
                /* Pass through events that are emitted after the userCursors offset and index. */
                .filter(avroObject -> {
                    if (pass.get()) {
                        return true;
                    } else {
                        if (blockOffset == avroObject.getBlockOffset()
                            && objectBlockIndex == avroObject.getObjectBlockIndex()) {
                            pass.set(true);
                        }
                        return false;
                    }
                });
        }
    }

    private BlobChangefeedEventWrapper wrapObject(AvroObject avroObject) {
        long blockOffset = avroObject.getBlockOffset();
        long objectBlockIndex = avroObject.getObjectBlockIndex();
        Object object = avroObject.getObject();

        ChangefeedCursor eventCursor = shardCursor.toEventCursor(chunkPath, blockOffset, objectBlockIndex);
        BlobChangefeedEvent event = BlobChangefeedEvent.fromRecord(object);

        return new BlobChangefeedEventWrapper(event, eventCursor);
    }

}
