package com.azure.storage.file.share.options;

import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when uploading a range from a source URL.
 */
public class ShareFileUploadRangeFromUrlOptions {
    private long length;
    private long destinationOffset;
    private long sourceOffset;
    private String sourceUrl;
    private String sourceAuthorization;
    private ShareRequestConditions destinationRequestConditions;

    /**
     * @return data length to upload for this operation.
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length data length to upload.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setLength(long length) {
        this.length = length;
        return this;
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
     * @return source URL for this operation.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @param sourceUrl upload source URL.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    /**
     * @return optional bearer token for access to source URL for this operation.
     */
    public String getSourceAuthorization() {
        return sourceAuthorization;
    }

    /**
     * @param sourceAuthorization optional bearer token for access to source URL.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setSourceAuthorization(String sourceAuthorization) {
        this.sourceAuthorization = sourceAuthorization;
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
