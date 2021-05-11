package com.azure.storage.file.datalake;

import com.azure.core.util.FluxUtil;
import com.azure.storage.common.StorageInputStream;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.PathProperties;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class DataLakeFileInputStream extends StorageInputStream {
    /**
     * Holds the reference to the blob this stream is associated with.
     */
    private final DataLakeFileAsyncClient fileClient;

    /**
     * Holds the {@link DataLakeRequestConditions} object that represents the access conditions for the blob.
     */
    private final DataLakeRequestConditions accessCondition;

    /**
     * Holds the {@link PathProperties} object that represents the blob's properties.
     */
    private final PathProperties properties;

    /**
     * Initializes a new instance of the BlobInputStream class. Note that if {@code blobRangeOffset} is not {@code 0} or
     * {@code blobRangeLength} is not {@code null}, there will be no content MD5 verification.
     *
     * @param blobClient A {@link DataLakeFileAsyncClient} object which represents the blob that this stream is associated
     * with.
     * @param fileRangeOffset The offset of blob data to begin stream.
     * @param fileRangeLength How much data the stream should return after blobRangeOffset.
     * @param chunkSize The size of the chunk to download.
     * @param accessCondition An {@link DataLakeRequestConditions} object which represents the access conditions for the
     * blob.
     * @throws DataLakeStorageException An exception representing any error which occurred during the operation.
     */
    DataLakeFileInputStream(final DataLakeFileAsyncClient blobClient, long fileRangeOffset, Long fileRangeLength,
        int chunkSize, final DataLakeRequestConditions accessCondition, final PathProperties pathProperties)
        throws DataLakeStorageException {
        super(fileRangeOffset, fileRangeLength, chunkSize, pathProperties.getFileSize());

        this.fileClient = blobClient;
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
            throw this.lastError;
        }
    }

    /**
     * Gets the blob properties.
     * <p>
     * If no data has been read from the stream, a network call is made to get properties. Otherwise, the blob
     * properties obtained from the download are stored.
     *
     * @return {@link PathProperties}
     */
    public PathProperties getProperties() {
        return this.properties;
    }
}
