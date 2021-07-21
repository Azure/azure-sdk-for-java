// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.StorageInputStream;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareStorageException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides an input stream to read a given storage file resource.
 */
public class StorageFileInputStream extends StorageInputStream {
    private final ClientLogger logger = new ClientLogger(StorageFileInputStream.class);

    private final ShareFileAsyncClient shareFileAsyncClient;

    /**
     * Initializes a new instance of the StorageFileInputStream class.
     *
     * @param shareFileAsyncClient A {@link ShareFileClient} object which represents the blob that this stream is
     * associated with.
     * @throws ShareStorageException An exception representing any error which occurred during the operation.
     */
    StorageFileInputStream(final ShareFileAsyncClient shareFileAsyncClient)
        throws ShareStorageException {
        this(shareFileAsyncClient, 0, null);
    }

    /**
     * Initializes a new instance of the StorageFileInputStream class. Note that if {@code fileRangeOffset} is not
     * {@code 0} or {@code fileRangeLength} is not {@code null}, there will be no content MD5 verification.
     *
     * @param shareFileAsyncClient A {@link ShareFileAsyncClient} object which represents the blob that this stream is
     * associated with.
     * @param fileRangeOffset The offset of file range data to begin stream.
     * @param fileRangeLength How much data the stream should return after fileRangeOffset.
     * @throws ShareStorageException An exception representing any error which occurred during the operation.
     */
    StorageFileInputStream(final ShareFileAsyncClient shareFileAsyncClient, long fileRangeOffset, Long fileRangeLength)
        throws ShareStorageException {
        super(fileRangeOffset, fileRangeLength, 4 * Constants.MB,
            shareFileAsyncClient.getProperties().block().getContentLength());
        this.shareFileAsyncClient = shareFileAsyncClient;
    }

    /**
     * Dispatches a read operation of N bytes.
     *
     * @param readLength An <code>int</code> which represents the number of bytes to read.
     */
    @Override
    protected synchronized ByteBuffer dispatchRead(final int readLength, final long offset) {
        try {
            ByteBuffer currentBuffer = this.shareFileAsyncClient.downloadWithResponse(
                new ShareFileRange(offset, offset + readLength - 1), false)
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getValue()).map(ByteBuffer::wrap))
                .block();

            this.bufferSize = readLength;
            this.bufferStartOffset = offset;
            return currentBuffer;
        } catch (final ShareStorageException e) {
            this.streamFaulted = true;
            this.lastError = new IOException(e);

            throw logger.logExceptionAsError(new RuntimeException(this.lastError.getMessage()));
        }
    }
}
