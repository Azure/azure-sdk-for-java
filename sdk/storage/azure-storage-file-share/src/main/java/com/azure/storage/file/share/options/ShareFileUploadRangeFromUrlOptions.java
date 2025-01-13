// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpAuthorization;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when uploading a range from a source URL.
 */
@Fluent
public final class ShareFileUploadRangeFromUrlOptions {
    private final long length;
    private final String sourceUrl;
    private long destinationOffset;
    private long sourceOffset;
    private HttpAuthorization sourceAuthorization;
    private ShareRequestConditions destinationRequestConditions;
    private FileLastWrittenMode lastWrittenMode;

    /**
     * Creates a new instance of {@link ShareFileUploadRangeFromUrlOptions}.
     *
     * @param length data length to upload for this operation.
     * @param sourceUrl source URL for this operation.
     * @throws NullPointerException if {@code sourceUrl} is null.
     */
    public ShareFileUploadRangeFromUrlOptions(long length, String sourceUrl) {
        StorageImplUtils.assertNotNull("sourceUrl", sourceUrl);
        this.length = length;
        this.sourceUrl = sourceUrl;
    }

    /**
     * Gets the length of the data to upload for this operation.
     *
     * @return data length to upload for this operation.
     */
    public long getLength() {
        return length;
    }

    /**
     * Gets the source URL for this operation.
     *
     * @return source URL for this operation.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Gets the destination offset for this operation.
     *
     * @return destination offset for this operation.
     */
    public long getDestinationOffset() {
        return destinationOffset;
    }

    /**
     * Sets the destination offset for this operation.
     *
     * @param destinationOffset offset for upload destination.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setDestinationOffset(long destinationOffset) {
        this.destinationOffset = destinationOffset;
        return this;
    }

    /**
     * Gets the source offset for this operation.
     *
     * @return source offset for this operation.
     */
    public long getSourceOffset() {
        return sourceOffset;
    }

    /**
     * Sets the source offset for this operation.
     *
     * @param sourceOffset offset for upload source.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setSourceOffset(long sourceOffset) {
        this.sourceOffset = sourceOffset;
        return this;
    }

    /**
     * Gets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @return optional auth header for access to source URL for this operation.
     */
    public HttpAuthorization getSourceAuthorization() {
        return sourceAuthorization;
    }

    /**
     * Sets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @param sourceAuthorization optional auth header for access to source URL.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization) {
        this.sourceAuthorization = sourceAuthorization;
        return this;
    }

    /**
     * Gets {@link ShareRequestConditions} for this operation.
     *
     * @return {@link ShareRequestConditions} for this operation.
     */
    public ShareRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * Sets {@link ShareRequestConditions} for this operation.
     *
     * @param destinationRequestConditions {@link ShareRequestConditions} for this operation.
     * @return modified options.
     */
    public ShareFileUploadRangeFromUrlOptions
        setDestinationRequestConditions(ShareRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * Gets the {@link FileLastWrittenMode}.
     *
     * @return The {@link FileLastWrittenMode}.
     */
    public FileLastWrittenMode getLastWrittenMode() {
        return this.lastWrittenMode;
    }

    /**
     * Sets the {@link FileLastWrittenMode}.
     *
     * @param lastWrittenMode {@link FileLastWrittenMode}
     * @return The updated options.
     */
    public ShareFileUploadRangeFromUrlOptions setLastWrittenMode(FileLastWrittenMode lastWrittenMode) {
        this.lastWrittenMode = lastWrittenMode;
        return this;
    }
}
