// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageInputStream;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathProperties;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provides an input stream to read a given Data Lake file resource.
 */
public final class DataLakeFileInputStream extends StorageInputStream {

    private final ClientLogger logger = new ClientLogger(DataLakeFileInputStream.class);

    /**
     * Holds the reference to the file this stream is associated with.
     */
    private final DataLakeFileAsyncClient fileClient;

    /**
     * Holds the {@link DataLakeRequestConditions} object that represents the access conditions for the file.
     */
    private final DataLakeRequestConditions accessCondition;

    /**
     * Holds the {@link PathProperties} object that represents the file's properties.
     */
    private final PathProperties properties;

    /**
     * Initializes a new instance of the DataLakeFileInputStream class. Note that if {@code fileRangeOffset} is not {@code 0} or
     * {@code fileRangeLength} is not {@code null}, there will be no content MD5 verification.
     *
     * @param fileClient A {@link DataLakeFileAsyncClient} object which represents the file that this stream is associated
     * with.
     * @param fileRangeOffset The offset of file data to begin stream.
     * @param fileRangeLength How much data the stream should return after fileRangeOffset.
     * @param chunkSize The size of the chunk to download.
     * @param accessCondition An {@link DataLakeRequestConditions} object which represents the access conditions for the
     * file.
     * @throws DataLakeStorageException An exception representing any error which occurred during the operation.
     */
    DataLakeFileInputStream(final DataLakeFileAsyncClient fileClient, long fileRangeOffset, Long fileRangeLength,
        int chunkSize, final DataLakeRequestConditions accessCondition, final PathProperties pathProperties)
        throws DataLakeStorageException {
        super(fileRangeOffset, fileRangeLength, chunkSize, pathProperties.getFileSize());

        this.fileClient = fileClient;
        this.accessCondition = accessCondition;
        this.properties = pathProperties;
    }

    /**
     * Dispatches a read operation of N bytes.
     *
     * @param readLength An <code>int</code> which represents the number of bytes to read.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected ByteBuffer dispatchRead(int readLength, long offset) throws IOException {
        try {
            ByteBuffer currentBuffer = this.fileClient.readWithResponse(new FileRange(offset,
                (long) readLength), null, this.accessCondition, false)
                .flatMap(response -> {
                    return FluxUtil.collectBytesInByteBufferStream(response.getValue()).map(ByteBuffer::wrap);
                })
                .block();

            this.bufferSize = readLength;
            this.bufferStartOffset = offset;
            return currentBuffer;
        } catch (final DataLakeStorageException e) {
            this.streamFaulted = true;
            this.lastError = new IOException(e);
            // cast required by CI checkstyle
            throw (IOException)logger.logThrowableAsError(this.lastError);
        }
    }

    /**
     * Gets the file properties.
     * <p>
     * If no data has been read from the stream, a network call is made to get properties. Otherwise, the file
     * properties obtained from the download are stored.
     *
     * @return {@link PathProperties}
     */
    public PathProperties getProperties() {
        return this.properties;
    }
}
