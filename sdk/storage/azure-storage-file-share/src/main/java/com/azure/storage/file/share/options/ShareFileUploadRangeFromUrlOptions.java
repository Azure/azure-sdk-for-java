// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpAuthorization;
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
    private HttpAuthorization sourceAuthorization;
    private ShareRequestConditions destinationRequestConditions;

    /**
     * @param length data length to upload for this operation.
     * @param sourceUrl source URL for this operation.
     */
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
