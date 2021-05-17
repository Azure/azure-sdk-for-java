// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized;

import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.StorageInputStream;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides an input stream to read a given blob resource.
 */
public final class BlobInputStream extends StorageInputStream {
    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final BlobAsyncClientBase blobClient;

    /**
     * Holds the {@link BlobRequestConditions} object that represents the access conditions for the blob.
     */
    private final BlobRequestConditions accessCondition;

    /**
     * Holds the {@link BlobProperties} object that represents the blob's properties.
     */
    private final BlobProperties properties;

    /**
     * Initializes a new instance of the BlobInputStream class. Note that if {@code blobRangeOffset} is not {@code 0} or
     * {@code blobRangeLength} is not {@code null}, there will be no content MD5 verification.
     *
     * @param blobClient A {@link BlobAsyncClientBase} object which represents the blob that this stream is associated
     * with.
     * @param blobRangeOffset The offset of blob data to begin stream.
     * @param blobRangeLength How much data the stream should return after blobRangeOffset.
     * @param chunkSize The size of the chunk to download.
     * @param accessCondition An {@link BlobRequestConditions} object which represents the access conditions for the
     * blob.
     * @throws BlobStorageException An exception representing any error which occurred during the operation.
     */
    BlobInputStream(final BlobAsyncClientBase blobClient, long blobRangeOffset, Long blobRangeLength, int chunkSize,
        final BlobRequestConditions accessCondition, final BlobProperties blobProperties)
        throws BlobStorageException {
        super(blobRangeOffset, blobRangeLength, chunkSize, blobProperties.getBlobSize());

        this.blobClient = blobClient;
        this.accessCondition = accessCondition;
        this.properties = blobProperties;
    }

    @Override
    protected Mono<ByteBuffer> executeRead(int readLength, long offset) {
        return this.blobClient.downloadWithResponse(
            new BlobRange(offset, (long) readLength), null, this.accessCondition, false)
            .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getValue()).map(ByteBuffer::wrap));
    }

    /**
     * Gets the blob properties.
     * <p>
     * If no data has been read from the stream, a network call is made to get properties. Otherwise, the blob
     * properties obtained from the download are stored.
     *
     * @return {@link BlobProperties}
     */
    public BlobProperties getProperties() {
        return this.properties;
    }

}
