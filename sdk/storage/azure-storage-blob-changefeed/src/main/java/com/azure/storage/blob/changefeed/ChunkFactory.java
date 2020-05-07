package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.internal.avro.implementation.AvroReaderFactory;

/**
 * Factory class for {@link Chunk}.
 */
class ChunkFactory {

    private final AvroReaderFactory avroReaderFactory;
    private final BlobLazyDownloaderFactory blobLazyDownloaderFactory;

    /**
     * Creates a default instance of the ChunkFactory.
     */
    ChunkFactory() {
        this.avroReaderFactory = new AvroReaderFactory();
        this.blobLazyDownloaderFactory = new BlobLazyDownloaderFactory();
    }

    /**
     * Creates a ChunkFactory with the designated factories.
     */
    ChunkFactory(AvroReaderFactory avroReaderFactory, BlobLazyDownloaderFactory blobLazyDownloaderFactory) {
        this.avroReaderFactory = avroReaderFactory;
        this.blobLazyDownloaderFactory = blobLazyDownloaderFactory;
    }

    /**
     * Gets a new instance of a Chunk.
     */
    Chunk getChunk(BlobContainerAsyncClient client, String chunkPath, ChangefeedCursor shardCursor,
        long blockOffset, long objectBlockIndex) {
        return new Chunk(client, chunkPath, shardCursor, blockOffset, objectBlockIndex, avroReaderFactory,
            blobLazyDownloaderFactory);
    }

}
