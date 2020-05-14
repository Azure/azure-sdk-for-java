// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.specialized;

import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.models.BlobDownloadHeaders;
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
    private final BlobAsyncClientBase blobClient;

    /**
     * Holds the {@link BlobRequestConditions} object that represents the access conditions for the blob.
     */
    private final BlobRequestConditions accessCondition;

    /**
     * Holds the {@link BlobProperties} object that represents the blob's properties.
     */
    private BlobProperties properties;

    /**
     * Initializes a new instance of the BlobInputStream class.
     *
     * @param blobClient A {@link BlobAsyncClient} object which represents the blob that this stream is associated with.
     * @param accessCondition An {@link BlobRequestConditions} object which represents the access conditions for the
     * blob.
     * @throws BlobStorageException An exception representing any error which occurred during the operation.
     */
    BlobInputStream(final BlobAsyncClient blobClient, final BlobRequestConditions accessCondition)
        throws BlobStorageException {
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
     * @param accessCondition An {@link BlobRequestConditions} object which represents the access conditions for the
     * blob.
     * @throws BlobStorageException An exception representing any error which occurred during the operation.
     */
    BlobInputStream(final BlobAsyncClientBase blobClient, long blobRangeOffset, Long blobRangeLength,
                    final BlobRequestConditions accessCondition)
        throws BlobStorageException {
        super(blobRangeOffset, blobRangeLength, 4 * Constants.MB,
            blobClient.getProperties().block().getBlobSize());

        this.blobClient = blobClient;
        this.accessCondition = accessCondition;
        this.properties = null;
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
                .flatMap(response -> {
                    // Only populate properties if it has not been populated yet, this is ok since we etag lock on the
                    // blob while downloading, so it is guaranteed to be the same.
                    if (this.properties == null) {
                        this.properties = buildBlobProperties(response.getDeserializedHeaders());
                    }
                    return FluxUtil.collectBytesInByteBufferStream(response.getValue()).map(ByteBuffer::wrap);
                })
                .block();

            this.bufferSize = readLength;
            this.bufferStartOffset = offset;
            return currentBuffer;
        } catch (final BlobStorageException e) {
            this.streamFaulted = true;
            this.lastError = new IOException(e);
            throw this.lastError;
        }
    }

    private static BlobProperties buildBlobProperties(BlobDownloadHeaders hd) {
        if (hd == null) {
            return null;
        }
        return new BlobProperties(null, hd.getLastModified(), hd.getETag(),
            hd.getContentLength() == null ? 0 : hd.getContentLength(), hd.getContentType(), null,
            hd.getContentEncoding(), hd.getContentDisposition(), hd.getContentLanguage(), hd.getCacheControl(),
            hd.getBlobSequenceNumber(), hd.getBlobType(), hd.getLeaseStatus(), hd.getLeaseState(),
            hd.getLeaseDuration(), hd.getCopyId(), hd.getCopyStatus(), hd.getCopySource(), hd.getCopyProgress(),
            hd.getCopyCompletionTime(), hd.getCopyStatusDescription(), hd.isServerEncrypted(),
            null, null, null, null, null, hd.getEncryptionKeySha256(), hd.getEncryptionScope(), null, hd.getMetadata(),
            hd.getBlobCommittedBlockCount(), hd.getTagCount(), hd.getVersionId(), null,
            hd.getObjectReplicationSourcePolicies(), hd.getObjectReplicationDestinationPolicyId());
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
        if (this.properties == null) {
            this.properties = blobClient.getPropertiesWithResponse(accessCondition).block().getValue();
        }
        return this.properties;
    }

}
