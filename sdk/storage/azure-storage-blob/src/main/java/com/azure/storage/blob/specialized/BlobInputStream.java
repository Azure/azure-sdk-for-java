// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.common.StorageInputStream;
import com.azure.storage.common.implementation.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides an input stream to read a given blob resource.
 */
public final class BlobInputStream extends StorageInputStream {
    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final BlobClientBase blobClient;

    /**
     * Holds the {@link BlobRequestConditions} object that represents the access conditions for the blob.
     */
    private final BlobRequestConditions accessCondition;

    /**
     * Holds the {@link BlobProperties} object that represents the blob's properties.
     */
    private final BlobProperties properties;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Initializes a new instance of the BlobInputStream class. Note that if {@code blobRangeOffset} is not {@code 0} or
     * {@code blobRangeLength} is not {@code null}, there will be no content MD5 verification.
     *
     * @param blobClient A {@link BlobAsyncClientBase} object which represents the blob that this stream is associated
     * with.
     * @param blobRangeOffset The offset of blob data to begin stream.
     * @param blobRangeLength How much data the stream should return after blobRangeOffset.
     * @param chunkSize The size of the chunk to download.
     * @param initialBuffer The result of the initial download.
     * @param accessCondition An {@link BlobRequestConditions} object which represents the access conditions for the
     * blob.
     * @param context The {@link Context}
     * @throws BlobStorageException An exception representing any error which occurred during the operation.
     */
    BlobInputStream(BlobClientBase blobClient, long blobRangeOffset, Long blobRangeLength, int chunkSize,
        ByteBuffer initialBuffer, BlobRequestConditions accessCondition, BlobProperties blobProperties, Context context)
        throws BlobStorageException {

        super(blobRangeOffset, blobRangeLength, chunkSize, adjustBlobLength(blobProperties.getBlobSize(), context),
            initialBuffer);

        this.blobClient = blobClient;
        this.accessCondition = accessCondition;
        this.properties = blobProperties;
        this.context = context;
    }

    /**
     * Dispatches a read operation of N bytes. When using sparse page blobs, the page ranges are evaluated and zero
     * bytes may be generated on the client side for some ranges that do not exist.
     *
     * @param readLength An <code>int</code> which represents the number of bytes to read.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected synchronized ByteBuffer dispatchRead(final int readLength, final long offset) throws IOException {
        try {
            ByteBuffer currentBuffer = this.blobClient
                .downloadContentWithResponse(null, accessCondition, new BlobRange(offset, (long) readLength), false,
                    null, context)
                .getValue()
                .toByteBuffer();

            this.bufferSize = readLength;
            this.bufferStartOffset = offset;
            return currentBuffer;
        } catch (final BlobStorageException e) {
            this.streamFaulted = true;
            this.lastError = new IOException(e);

            throw this.lastError;
        }
    }

    /**
     * Gets the blob properties as fetched upon download.
     *
     * @return {@link BlobProperties}
     */
    public BlobProperties getProperties() {
        return this.properties;
    }

    /**
     * Allows for encrypted blobs to use BlobInputStream correctly by using the non-encrypted blob length
     */
    private static long adjustBlobLength(long initialLength, Context context) {
        if (context != null && context.getData(Constants.ADJUSTED_BLOB_LENGTH_KEY).isPresent()) {
            return (long) context.getData(Constants.ADJUSTED_BLOB_LENGTH_KEY).get();
        }
        return initialLength;
    }

}
