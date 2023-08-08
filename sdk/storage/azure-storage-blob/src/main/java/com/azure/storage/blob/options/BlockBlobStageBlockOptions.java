// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.StorageImplUtils;

/**
 * Extended options that may be passed when staging a block.
 */
@Fluent
public final class BlockBlobStageBlockOptions {
    private final String base64BlockId;
    private final BinaryData data;
    private String leaseId;
    private byte[] contentMd5;

    /**
     * @param base64BlockId The block ID to assign the new block.
     * @param data The data to write to the block. Note that this {@code BinaryData} must have defined length
     * and must be replayable if retries are enabled (the default), see {@link BinaryData#isReplayable()}.
     */
    public BlockBlobStageBlockOptions(String base64BlockId, BinaryData data) {
        StorageImplUtils.assertNotNull("base64BlockId must not be null", base64BlockId);
        StorageImplUtils.assertNotNull("data must not be null", data);
        StorageImplUtils.assertNotNull("data must have defined length", data.getLength());
        this.base64BlockId = base64BlockId;
        this.data = data;
    }

    /**
     * @return The block ID to assign the new block.
     */
    public String getBase64BlockId() {
        return base64BlockId;
    }

    /**
     * @return The data to write to the blob.
     */
    public BinaryData getData() {
        return this.data;
    }

    /**
     * @return Lease ID for accessing source content.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * @param leaseId Lease ID for accessing source content.
     * @return The updated options.
     */
    public BlockBlobStageBlockOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * @return An MD5 hash of the content. This hash is used to verify the integrity of the content during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
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
}
