// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.internal.avro.implementation.AvroObject;
import com.azure.storage.internal.avro.implementation.AvroReaderFactory;
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

    private static final long DEFAULT_HEADER_SIZE = 4 * Constants.KB;
    private static final long DEFAULT_BODY_SIZE = Constants.MB;

    private final BlobAsyncClient client; /* Chunk blob. */
    private final String chunkPath; /* Chunk path. */

    private final ChangefeedCursor shardCursor; /* Cursor associated with parent shard. */

    private final long blockOffset; /* blockOffset to start reading chunk. */

    private final long objectBlockIndex;

    private final AvroReaderFactory avroReaderFactory;
    private final BlobLazyDownloaderFactory blobLazyDownloaderFactory;

    /**
     * Creates a new Chunk.
     */
    Chunk(BlobContainerAsyncClient client, String chunkPath, ChangefeedCursor shardCursor, long blockOffset,
        long objectBlockIndex, AvroReaderFactory avroReaderFactory,
        BlobLazyDownloaderFactory blobLazyDownloaderFactory) {

        this.client = client.getBlobAsyncClient(chunkPath);
        this.chunkPath = chunkPath;
        this.shardCursor = shardCursor;

        this.blockOffset = blockOffset;
        this.objectBlockIndex = objectBlockIndex;

        this.avroReaderFactory = avroReaderFactory;
        this.blobLazyDownloaderFactory = blobLazyDownloaderFactory;
    }

    Flux<BlobChangefeedEventWrapper> getEvents() {
            /* Read relevant avro objects. */
        return readAvroObjects()
            /* Convert AvroObjects into BlobChangefeedEventWrappers. */
            .map(avroObject -> {
                /* Unwrap AvroObject. */
                long blockOffset = avroObject.getBlockOffset();
                long objectBlockIndex = avroObject.getObjectBlockIndex();
                Object object = avroObject.getObject();

                /* Get the event cursor associated with this event. */
                ChangefeedCursor eventCursor = shardCursor.toEventCursor(chunkPath, blockOffset, objectBlockIndex);
                BlobChangefeedEvent event = BlobChangefeedEvent.fromRecord(object);

                /* Wrap the event and cursor. */
                return new BlobChangefeedEventWrapper(event, eventCursor);
            });
    }

    private Flux<AvroObject> readAvroObjects() {
        if (blockOffset == 0) {
            /* Read events like normal. */
            Flux<ByteBuffer> avro = blobLazyDownloaderFactory.getBlobLazyDownloader(client, DEFAULT_BODY_SIZE, blockOffset)
                .download();
            return avroReaderFactory.getAvroReader(avro)
                .readAvroObjects();
        } else {
            /* Read header and body separately. */
            Flux<ByteBuffer> avroHeader =
                blobLazyDownloaderFactory.getBlobLazyDownloader(client, DEFAULT_HEADER_SIZE)
                .download();
            Flux<ByteBuffer> avroBody =
                blobLazyDownloaderFactory.getBlobLazyDownloader(client, DEFAULT_BODY_SIZE, blockOffset)
                .download();

            AtomicBoolean pass = new AtomicBoolean();
            return avroReaderFactory.getAvroReader(avroHeader, avroBody, blockOffset)
                .readAvroObjects()
                /* The Avro Parser should start emitting objects in the blockOffset. */
                /* Pass through events that are emitted after the desired objectBlockIndex. Note: the object at
                   objectBlockIndex is not inclusive since that is the last object that was emitted to the CF user. */
                .filter(avroObject -> {
                    if (pass.get()) {
                        return true;
                    } else {
                        assert blockOffset == avroObject.getBlockOffset()
                            : String.format("Expected objects in block %d, but got objects in block %d.",
                            blockOffset, avroObject.getBlockOffset());

                        if (objectBlockIndex == avroObject.getObjectBlockIndex()) {
                            pass.set(true);
                        }
                        return false;
                    }
                });
        }
    }
}
