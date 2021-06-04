package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

@Fluent
public class StageBlockFromUrlOptions {
    private final String base64BlockId;
    private final String sourceUrl;
    private BlobRange sourceRange;
    private byte[] sourceContentMd5;
    private String leaseId;
    private BlobRequestConditions sourceRequestConditions;
    private String sourceBearerToken;

    /**
     * @param base64BlockId The block ID to assign the new block.
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public StageBlockFromUrlOptions(String base64BlockId, String sourceUrl) {
        StorageImplUtils.assertNotNull("base64BlockId", base64BlockId);
        StorageImplUtils.assertNotNull("sourceUrl", sourceUrl);
        this.base64BlockId = base64BlockId;
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return The block ID to assign the new block.
     */
    public String getBase64BlockId() {
        return base64BlockId;
    }

    /**
     * @return The source URL to upload from.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @return Range of bytes to read from the source.
     */
    public BlobRange getSourceRange() {
        return sourceRange;
    }

    /**
     * @param sourceRange Range of bytes to read from the source.
     * @return The updated options.
     */
    public StageBlockFromUrlOptions setSourceRange(BlobRange sourceRange) {
        this.sourceRange = sourceRange;
        return this;
    }

    /**
     * @return MD5 of the source content.
     */
    public byte[] getSourceContentMd5() {
        return sourceContentMd5;
    }

    /**
     * @param sourceContentMd5 MD5 of the source content.
     * @return The updated options.
     */
    public StageBlockFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5) {
        this.sourceContentMd5 = sourceContentMd5;
        return this;
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
    public StageBlockFromUrlOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * @return {@link BlobRequestConditions} for accessing source content.
     */
    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * @param sourceRequestConditions {@link BlobRequestConditions} for accessing source content.
     * @return The updated options.
     */
    public StageBlockFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * @return Bearer token to access source.
     */
    public String getSourceBearerToken() {
        return sourceBearerToken;
    }

    /**
     * @param sourceBearerToken Bearer token to access source.
     * @return The updated options.
     */
    public StageBlockFromUrlOptions setSourceBearerToken(String sourceBearerToken) {
        this.sourceBearerToken = sourceBearerToken;
        return this;
    }
}
