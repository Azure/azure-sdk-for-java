// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.internal.avro.implementation.AvroReader;
import com.azure.storage.internal.avro.implementation.AvroReaderFactory;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * Factory class for {@link Chunk}.
 */
class ChunkFactory {

    private final ClientLogger logger = new ClientLogger(ChunkFactory.class);
    private static final long DEFAULT_HEADER_SIZE = 4 * Constants.KB;
    /* TODO (gapra): This should probably be configurable by a user. */
    private static final long DEFAULT_BODY_SIZE = Constants.MB;

    private final AvroReaderFactory avroReaderFactory;
    private final BlobChunkedDownloaderFactory blobChunkedDownloaderFactory;

    /**
     * Creates a ChunkFactory with the designated factories.
     */
    ChunkFactory(AvroReaderFactory avroReaderFactory, BlobChunkedDownloaderFactory blobChunkedDownloaderFactory) {
        StorageImplUtils.assertNotNull("avroReaderFactory", avroReaderFactory);
        StorageImplUtils.assertNotNull("blobLazyDownloaderFactory", blobChunkedDownloaderFactory);
        this.avroReaderFactory = avroReaderFactory;
        this.blobChunkedDownloaderFactory = blobChunkedDownloaderFactory;
    }

    /**
     * Gets a new instance of a Chunk.
     *
     * @param chunkPath The path to the chunk blob.
     * @param changefeedCursor The parent changefeed cursor.
     * @param blockOffset The offset of the block to start reading from. If 0, this indicates we should read the whole
     *                    avro file from the beginning.
     * @param eventIndex The index of the last object in the block that was returned to the user.
     * @return {@link Chunk}
     */
    Chunk getChunk(String chunkPath, long chunkLength, ChangefeedCursor changefeedCursor, long blockOffset,
        long eventIndex) {
        /* Validate parameters. */
        StorageImplUtils.assertNotNull("chunkPath", chunkPath);
        StorageImplUtils.assertNotNull("chunkLength", chunkLength);
        StorageImplUtils.assertNotNull("changefeedCursor", changefeedCursor);
        StorageImplUtils.assertInBounds("blockOffset", blockOffset, 0, Long.MAX_VALUE);
        StorageImplUtils.assertInBounds("eventIndex", eventIndex, 0, Long.MAX_VALUE);

        /* Determine which AvroReader should be used. */
        AvroReader avroReader;
        /* If blockOffset is 0, that means we are starting from the beginning of the file.
           Read events like normal using the simple AvroReader. */
        if (blockOffset == 0) {
            /* Download the whole blob lazily in chunks and use that as the source for the AvroReader. */
            Flux<ByteBuffer> avro = blobChunkedDownloaderFactory.getBlobLazyDownloader(chunkPath, DEFAULT_BODY_SIZE,
                blockOffset /* Note: this is 0. */)
                .download();
            avroReader = avroReaderFactory.getAvroReader(avro);
        /* If blockOffset > 0, that means we are reading the avro file header and body separately.
           Read events starting at the designated blockOffset and eventIndex. */
        } else {
            if (chunkLength > blockOffset) { /* There are more events to read from the chunk. */
                /* Download the first DEFAULT_HEADER_SIZE bytes from the blob lazily in chunks and use that as the
                header source for the AvroReader. */
                Flux<ByteBuffer> avroHeader =
                    blobChunkedDownloaderFactory.getBlobLazyDownloader(chunkPath, DEFAULT_HEADER_SIZE)
                        .download();
                /* Download the rest of the blob starting at the blockOffset lazily in chunks and use that as the body
               source for the AvroReader. */
                Flux<ByteBuffer> avroBody =
                    blobChunkedDownloaderFactory.getBlobLazyDownloader(chunkPath, DEFAULT_BODY_SIZE, blockOffset)
                        .download();
                avroReader = avroReaderFactory.getAvroReader(avroHeader, avroBody, blockOffset, eventIndex);
            } else if (chunkLength == blockOffset) { /* We hit the end of the chunk, return 0 events. */
                avroReader = Flux::empty;
            } else {
                throw logger.logExceptionAsError(new IllegalArgumentException("Cursor contains a blockOffset that"
                    + " is invalid."));
            }
        }

        return new Chunk(chunkPath, changefeedCursor, avroReader);
    }

}
