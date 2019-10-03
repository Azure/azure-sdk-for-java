// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Constants;
import com.azure.storage.common.StorageInputStream;
import com.azure.storage.file.models.FileRange;
import com.azure.storage.file.models.StorageException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides an input stream to read a given storage file resource.
 */
public class StorageFileInputStream extends StorageInputStream {
    final ClientLogger logger = new ClientLogger(StorageFileInputStream.class);

    private final FileAsyncClient fileAsyncClient;

    /**
     * Initializes a new instance of the StorageFileInputStream class.
     *
     * @param fileAsyncClient A {@link FileClient} object which represents the blob that this stream is associated with.
     * @throws StorageException An exception representing any error which occurred during the operation.
     */
    StorageFileInputStream(final FileAsyncClient fileAsyncClient)
        throws StorageException {
        this(fileAsyncClient, 0, null);
    }

    /**
     * Initializes a new instance of the StorageFileInputStream class. Note that if {@code fileRangeOffset} is not
     * {@code 0} or {@code fileRangeLength} is not {@code null}, there will be no content MD5 verification.
     *
     * @param fileAsyncClient A {@link FileAsyncClient} object which represents the blob
     * that this stream is associated with.
     * @param fileRangeOffset The offset of file range data to begin stream.
     * @param fileRangeLength How much data the stream should return after fileRangeOffset.
     * @throws StorageException An exception representing any error which occurred during the operation.
     */
    StorageFileInputStream(final FileAsyncClient fileAsyncClient, long fileRangeOffset, Long fileRangeLength)
        throws StorageException {
        super(fileRangeOffset, fileRangeLength, 4 * Constants.MB,
            fileAsyncClient.getProperties().block().getContentLength());
        this.fileAsyncClient = fileAsyncClient;
    }

    /**
     * Dispatches a read operation of N bytes.
     *
     * @param readLength An <code>int</code> which represents the number of bytes to read.
     */
    @Override
    protected synchronized ByteBuffer dispatchRead(final int readLength, final long offset) {
        try {
            ByteBuffer currentBuffer = this.fileAsyncClient.downloadWithPropertiesWithResponse(new FileRange(offset,
                 offset + readLength - 1), false)
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getValue().getBody())
                                        .map(ByteBuffer::wrap))
                                        .block();

            this.bufferSize = readLength;
            this.bufferStartOffset = offset;
            return currentBuffer;
        } catch (final StorageException e) {
            this.streamFaulted = true;
            this.lastError = new IOException(e);
            throw logger.logExceptionAsError(new RuntimeException(this.lastError.getMessage()));
        }
    }
}
