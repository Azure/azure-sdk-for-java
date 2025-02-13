// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpAuthorization;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

/**
 * Extended options that may be passed when staging a block from a source URL.
 */
@Fluent
public final class BlockBlobStageBlockFromUrlOptions {
    private final String base64BlockId;
    private final String sourceUrl;
    private BlobRange sourceRange;
    private byte[] sourceContentMd5;
    private String leaseId;
    private BlobRequestConditions sourceRequestConditions;
    private HttpAuthorization sourceAuthorization;

    /**
     * Creates a new instance of {@link BlockBlobStageBlockFromUrlOptions}.
     *
     * @param base64BlockId The block ID to assign the new block.
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public BlockBlobStageBlockFromUrlOptions(String base64BlockId, String sourceUrl) {
        this.base64BlockId = base64BlockId;
        this.sourceUrl = sourceUrl;
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
     * Gets the source URL to upload from.
     *
     * @return The source URL to upload from.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Gets the range of bytes to read from the source.
     *
     * @return Range of bytes to read from the source.
     */
    public BlobRange getSourceRange() {
        return sourceRange;
    }

    /**
     * Sets the range of bytes to read from the source.
     *
     * @param sourceRange Range of bytes to read from the source.
     * @return The updated options.
     */
    public BlockBlobStageBlockFromUrlOptions setSourceRange(BlobRange sourceRange) {
        this.sourceRange = sourceRange;
        return this;
    }

    /**
     * Gets the MD5 of the source content.
     *
     * @return MD5 of the source content.
     */
    public byte[] getSourceContentMd5() {
        return CoreUtils.clone(sourceContentMd5);
    }

    /**
     * Sets the MD5 of the source content.
     *
     * @param sourceContentMd5 MD5 of the source content.
     * @return The updated options.
     */
    public BlockBlobStageBlockFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5) {
        this.sourceContentMd5 = CoreUtils.clone(sourceContentMd5);
        return this;
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
    public BlockBlobStageBlockFromUrlOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the {@link BlobRequestConditions} for accessing source content.
     *
     * @return {@link BlobRequestConditions} for accessing source content.
     */
    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions} for accessing source content.
     *
     * @param sourceRequestConditions {@link BlobRequestConditions} for accessing source content.
     * @return The updated options.
     */
    public BlockBlobStageBlockFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * Gets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @return auth header to access source.
     */
    public HttpAuthorization getSourceAuthorization() {
        return sourceAuthorization;
    }

    /**
     * Sets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @param sourceAuthorization auth header to access source.
     * @return The updated options.
     */
    public BlockBlobStageBlockFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization) {
        this.sourceAuthorization = sourceAuthorization;
        return this;
    }
}
