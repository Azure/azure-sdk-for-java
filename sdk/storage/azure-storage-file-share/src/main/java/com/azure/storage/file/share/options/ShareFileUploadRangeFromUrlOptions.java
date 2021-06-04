package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when uploading a range from a source URL.
 */
@Fluent
public class ShareFileUploadRangeFromUrlOptions {
    private final long length;
    private final String sourceUrl;
    private long destinationOffset;
    private long sourceOffset;
    private String sourceBearerToken;
    private ShareRequestConditions destinationRequestConditions;

    public ShareFileUploadRangeFromUrlOptions(
        long length, String sourceUrl) {
        StorageImplUtils.assertNotNull("sourceUrl", sourceUrl);
        this.length = length;
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return data length to upload for this operation.
     */
    public long getLength() {
        return length;
    }

    /**
     * @return source URL for this operation.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @return destination offset for this operation.
     */
    public long getDestinationOffset() {
        return destinationOffset;
    }

    /**
     * @param destinationOffset offset for upload destination.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setDestinationOffset(long destinationOffset) {
        this.destinationOffset = destinationOffset;
        return this;
    }

    /**
     * @return source offset for this operation.
     */
    public long getSourceOffset() {
        return sourceOffset;
    }

    /**
     * @param sourceOffset offset for upload source.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setSourceOffset(long sourceOffset) {
        this.sourceOffset = sourceOffset;
        return this;
    }

    /**
     * @return optional bearer token for access to source URL for this operation.
     */
    public String getSourceBearerToken() {
        return sourceBearerToken;
    }

    /**
     * @param sourceAuthorization optional bearer token for access to source URL.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setSourceBearerToken(String sourceAuthorization) {
        this.sourceBearerToken = sourceAuthorization;
        return this;
    }

    /**
     * @return {@link ShareRequestConditions} for this operation.
     */
    public ShareRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * @param destinationRequestConditions {@link ShareRequestConditions} for this operation.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setDestinationRequestConditions(
        ShareRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }
}
