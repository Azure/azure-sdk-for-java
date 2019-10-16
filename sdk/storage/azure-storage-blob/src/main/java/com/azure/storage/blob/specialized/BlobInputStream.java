// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized;

import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.implementation.Constants;

import com.azure.storage.common.StorageInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides an input stream to read a given blob resource.
 */
public final class BlobInputStream extends StorageInputStream {
    private final ClientLogger logger = new ClientLogger(BlobInputStream.class);

    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final BlobAsyncClientBase blobClient;

    /**
     * Holds the {@link BlobAccessConditions} object that represents the access conditions for the blob.
     */
    private final BlobAccessConditions accessCondition;

    /**
     * Initializes a new instance of the BlobInputStream class.
     *
     * @param blobClient A {@link BlobAsyncClient} object which represents the blob that this stream is associated with.
     * @param accessCondition An {@link BlobAccessConditions} object which represents the access conditions for the
     * blob.
     * @throws StorageException An exception representing any error which occurred during the operation.
     */
    BlobInputStream(final BlobAsyncClient blobClient, final BlobAccessConditions accessCondition)
        throws StorageException {
        this(blobClient, 0, null, accessCondition);
    }

    /**
     * Initializes a new instance of the BlobInputStream class. Note that if {@code blobRangeOffset} is not {@code 0} or
     * {@code blobRangeLength} is not {@code null}, there will be no content MD5 verification.
     *
     * @param blobClient A {@link BlobAsyncClientBase} object which represents the blob that this stream is associated
     * with.
     * @param blobRangeOffset The offset of blob data to begin stream.
     * @param blobRangeLength How much data the stream should return after blobRangeOffset.
     * @param accessCondition An {@link BlobAccessConditions} object which represents the access conditions for the
     * blob.
     * @throws StorageException An exception representing any error which occurred during the operation.
     */
    BlobInputStream(final BlobAsyncClientBase blobClient, long blobRangeOffset, Long blobRangeLength,
                    final BlobAccessConditions accessCondition)
        throws StorageException {
        super(blobRangeOffset, blobRangeLength, 4 * Constants.MB,
            blobClient.getProperties().block().getBlobSize());

        this.blobClient = blobClient;
        this.accessCondition = accessCondition;


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
            ByteBuffer currentBuffer = this.blobClient.downloadWithResponse(new BlobRange(offset,
                (long) readLength), null, this.accessCondition, false)
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getValue()).map(ByteBuffer::wrap))
                .block();

            this.bufferSize = readLength;
            this.bufferStartOffset = offset;
            return currentBuffer;
        } catch (final StorageException e) {
            this.streamFaulted = true;
            this.lastError = new IOException(e);
            throw this.lastError;
        }
    }

}
