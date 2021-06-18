// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.experimental.http.HttpAuthorization;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;

/**
 * Extended options that may be passed when uploading a page range from a source URL.
 */
@Fluent
public class PageBlobUploadPagesFromUrlOptions {
    private final PageRange range;
    private final String sourceUrl;
    private Long sourceOffset;
    private byte[] sourceContentMd5;
    private PageBlobRequestConditions destinationRequestConditions;
    private BlobRequestConditions sourceRequestConditions;
    private HttpAuthorization sourceAuthorization;

    /**
     * @param range The destination page range. Pages must be aligned to 512 byte blocks.
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public PageBlobUploadPagesFromUrlOptions(PageRange range, String sourceUrl) {
        this.range = range;
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return The destination page range.
     */
    public PageRange getRange() {
        return range;
    }

    /**
     * @return the source URL to copy from.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @return Offset at source to copy from.
     */
    public Long getSourceOffset() {
        return sourceOffset;
    }

    /**
     * @param sourceOffset Offset at source to copy from.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions setSourceOffset(Long sourceOffset) {
        this.sourceOffset = sourceOffset;
        return this;
    }

    /**
     * @return Content MD5 of source content to copy.
     */
    public byte[] getSourceContentMd5() {
        return CoreUtils.clone(sourceContentMd5);
    }

    /**
     * @param sourceContentMd5 Content MD5 of source content to copy.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5) {
        this.sourceContentMd5 = CoreUtils.clone(sourceContentMd5);
        return this;
    }

    /**
     * @return {@link PageBlobRequestConditions} for writing to destination.
     */
    public PageBlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * @param destinationRequestConditions {@link PageBlobRequestConditions} for writing to destination.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions setDestinationRequestConditions(PageBlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
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
    public PageBlobUploadPagesFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * @return auth header for accessing source content.
     */
    public HttpAuthorization getSourceAuthorization() {
        return sourceAuthorization;
    }

    /**
     * Sets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @param sourceAuthorization auth header for accessing source content.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization) {
        this.sourceAuthorization = sourceAuthorization;
        return this;
    }
}
