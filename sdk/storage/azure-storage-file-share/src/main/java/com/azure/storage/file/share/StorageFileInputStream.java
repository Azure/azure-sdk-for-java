// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.StorageInputStream;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides an input stream to read a given storage file resource.
 */
public class StorageFileInputStream extends StorageInputStream {
    private static final ClientLogger LOGGER = new ClientLogger(StorageFileInputStream.class);

    private final ShareFileClient shareFileClient;


    /**
     * Initializes a new instance of the StorageFileInputStream class.
     *
     * @param shareFileClient A {@link ShareFileClient} object which represents the blob that this stream is
     * associated with.
     * @throws ShareStorageException An exception representing any error which occurred during the operation.
     */
    StorageFileInputStream(final ShareFileClient shareFileClient)
        throws ShareStorageException {
        this(shareFileClient, 0, null);
    }

    /**
     * Initializes a new instance of the StorageFileInputStream class. Note that if {@code fileRangeOffset} is not
     * {@code 0} or {@code fileRangeLength} is not {@code null}, there will be no content MD5 verification.
     *
     * @param shareFileClient A {@link ShareFileClient} object which represents the blob that this stream is
     * associated with.
     * @param fileRangeOffset The offset of file range data to begin stream.
     * @param fileRangeLength How much data the stream should return after fileRangeOffset.
     * @throws ShareStorageException An exception representing any error which occurred during the operation.
     */
    StorageFileInputStream(final ShareFileClient shareFileClient, long fileRangeOffset, Long fileRangeLength)
        throws ShareStorageException {
        super(fileRangeOffset, fileRangeLength, 4 * Constants.MB, shareFileClient.getProperties().getContentLength());
        this.shareFileClient = shareFileClient;
    }

    /**
     * Dispatches a read operation of N bytes.
     *
     * @param readLength An <code>int</code> which represents the number of bytes to read.
     */
    @Override
    protected synchronized ByteBuffer dispatchRead(final int readLength, final long offset) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(readLength);
            ShareFileRange fileRange = new ShareFileRange(offset, offset + readLength - 1);
            ShareFileDownloadOptions downloadOptions = new ShareFileDownloadOptions()
                .setRange(fileRange)
                .setRangeContentMd5Requested(false);
            shareFileClient.downloadWithResponse(outputStream, downloadOptions, null, null);

            // Convert the output stream data into a byte buffer
            byte[] data = outputStream.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(data);

            this.bufferSize = data.length;
            this.bufferStartOffset = offset;

            return buffer;
        } catch (final ShareStorageException e) {
            this.streamFaulted = true;
            this.lastError = new IOException("Error reading from input stream.", e);

            throw LOGGER.logExceptionAsError(new RuntimeException(this.lastError.getMessage(), e));
        }
    }
}
