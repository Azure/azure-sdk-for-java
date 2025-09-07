// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.implementation.structuredmessage.StorageChecksumAlgorithm;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * Extended options that may be passed when staging a block.
 */
@Fluent
public final class BlockBlobStageBlockOptions {
    private final String base64BlockId;
    private final long length;
    private final BinaryData data;
    private final Flux<ByteBuffer> dataFlux;
    private String leaseId;
    private byte[] contentMd5;
    private StorageChecksumAlgorithm storageChecksumAlgorithm;

    /**
     * Creates a new instance of {@link BlockBlobStageBlockOptions}.
     *
     * @param base64BlockId The block ID to assign the new block.
     * @param data The data to write to the block. Note that this {@code BinaryData} must have defined length
     * and must be replayable if retries are enabled (the default), see {@link BinaryData#isReplayable()}.
     * @throws NullPointerException If {@code base64BlockId} or {@code data} is null.
     */
    public BlockBlobStageBlockOptions(String base64BlockId, BinaryData data) {
        StorageImplUtils.assertNotNull("base64BlockId must not be null", base64BlockId);
        StorageImplUtils.assertNotNull("data must not be null", data);
        StorageImplUtils.assertNotNull("data must have defined length", data.getLength());
        this.data = data;
        this.length = data.getLength();
        this.base64BlockId = base64BlockId;
        this.dataFlux = null;
    }

    /**
     * comment
     *
     * @param base64BlockId comment
     * @param dataFlux comment
     * @param length comment
     */
    public BlockBlobStageBlockOptions(String base64BlockId, Flux<ByteBuffer> dataFlux, long length) {
        StorageImplUtils.assertNotNull("base64BlockId must not be null", base64BlockId);
        StorageImplUtils.assertNotNull("data must not be null", dataFlux);
        StorageImplUtils.assertInBounds("length", length, 0, Long.MAX_VALUE);
        this.dataFlux = dataFlux;
        this.length = length;
        this.base64BlockId = base64BlockId;
        this.data = null;

    }

    /**
     * Gets the block ID to assign the new block.
     *
     * @return The block ID to assign the new block.
     */
    public String getBase64BlockId() {
        return base64BlockId;
    }

    /**
     * Gets the data to write to the blob.
     *
     * @return The data to write to the blob.
     */
    public BinaryData getData() {
        return this.data;
    }

    /**
     * comment
     *
     * @return comment
     */
    public Flux<ByteBuffer> getDataFlux() {
        return this.dataFlux;
    }

    /**
     * Gets the lease ID for accessing source content.
     *
     * @return Lease ID for accessing source content.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets the lease ID for accessing source content.
     *
     * @param leaseId Lease ID for accessing source content.
     * @return The updated options.
     */
    public BlockBlobStageBlockOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the MD5 of the block content.
     *
     * @return An MD5 hash of the content. This hash is used to verify the integrity of the content during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Sets the MD5 of the block content.
     *
     * @param contentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @return The updated options
     */
    public BlockBlobStageBlockOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = CoreUtils.clone(contentMd5);
        return this;
    }

    /**
     * temp
     *
     * @return temp.
     */
    public StorageChecksumAlgorithm getStorageChecksumAlgorithm() {
        return storageChecksumAlgorithm;
    }

    /**
     * temp
     *
     * @param storageChecksumAlgorithm temp.
     * @return temp.
     */
    public BlockBlobStageBlockOptions setStorageChecksumAlgorithm(StorageChecksumAlgorithm storageChecksumAlgorithm) {
        this.storageChecksumAlgorithm = storageChecksumAlgorithm;
        return this;
    }

    /**
     * comment
     *
     * @return comment
     */
    public long getLength() {
        return length;
    }
}
