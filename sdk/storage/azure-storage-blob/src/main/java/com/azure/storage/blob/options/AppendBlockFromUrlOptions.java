package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

@Fluent
public class AppendBlockFromUrlOptions {
    private final String sourceUrl;
    private BlobRange sourceRange;
    private byte[] sourceContentMD5;
    private AppendBlobRequestConditions destinationRequestConditions;
    private BlobRequestConditions sourceRequestConditions;
    private String sourceBearerToken;

    /**
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public AppendBlockFromUrlOptions(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return Source URL to copy from.
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
    public AppendBlockFromUrlOptions setSourceRange(BlobRange sourceRange) {
        this.sourceRange = sourceRange;
        return this;
    }

    /**
     * @return MD5 of the source content to be appended.
     */
    public byte[] getSourceContentMD5() {
        return sourceContentMD5;
    }

    /**
     * @param sourceContentMD5 MD5 of the source content to be appended.
     * @return The updated options.
     */
    public AppendBlockFromUrlOptions setSourceContentMD5(byte[] sourceContentMD5) {
        this.sourceContentMD5 = sourceContentMD5;
        return this;
    }

    /**
     * @return {@link AppendBlobRequestConditions} for writing to destination.
     */
    public AppendBlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * @param destinationRequestConditions {@link AppendBlobRequestConditions} for writing to destination.
     * @return The updated options.
     */
    public AppendBlockFromUrlOptions setDestinationRequestConditions(AppendBlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * @return {@link BlobRequestConditions} for accessing source.
     */
    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * @param sourceRequestConditions {@link BlobRequestConditions} for accessing source.
     * @return The updated options.
     */
    public AppendBlockFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * @return Bearer token for accessing source.
     */
    public String getSourceBearerToken() {
        return sourceBearerToken;
    }

    /**
     * @param sourceBearerToken Bearer token for accessing source.
     * @return The updated options.
     */
    public AppendBlockFromUrlOptions setSourceBearerToken(String sourceBearerToken) {
        this.sourceBearerToken = sourceBearerToken;
        return this;
    }
}
