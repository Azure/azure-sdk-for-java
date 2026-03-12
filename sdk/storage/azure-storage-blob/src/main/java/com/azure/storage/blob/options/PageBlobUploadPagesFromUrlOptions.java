// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpAuthorization;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.FileShareTokenIntent;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;

/**
 * Extended options that may be passed when uploading a page range from a source URL.
 */
@Fluent
public final class PageBlobUploadPagesFromUrlOptions {
    private final PageRange range;
    private final String sourceUrl;
    private Long sourceOffset;
    private byte[] sourceContentMd5;
    private PageBlobRequestConditions destinationRequestConditions;
    private BlobRequestConditions sourceRequestConditions;
    private HttpAuthorization sourceAuthorization;
    private FileShareTokenIntent sourceShareTokenIntent;

    /**
     * Creates a new instance of {@link PageBlobUploadPagesFromUrlOptions}.
     *
     * @param range The destination page range. Pages must be aligned to 512 byte blocks.
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public PageBlobUploadPagesFromUrlOptions(PageRange range, String sourceUrl) {
        this.range = range;
        this.sourceUrl = sourceUrl;
    }

    /**
     * Gets the destination page range.
     * @return The destination page range.
     */
    public PageRange getRange() {
        return range;
    }

    /**
     * Gets the source URL to copy from.
     *
     * @return the source URL to copy from.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Gets the offset at source to copy from.
     *
     * @return Offset at source to copy from.
     */
    public Long getSourceOffset() {
        return sourceOffset;
    }

    /**
     * Sets the offset at source to copy from.
     *
     * @param sourceOffset Offset at source to copy from.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions setSourceOffset(Long sourceOffset) {
        this.sourceOffset = sourceOffset;
        return this;
    }

    /**
     * Gets the content MD5 of source content to copy.
     *
     * @return Content MD5 of source content to copy.
     */
    public byte[] getSourceContentMd5() {
        return CoreUtils.clone(sourceContentMd5);
    }

    /**
     * Sets the content MD5 of source content to copy.
     *
     * @param sourceContentMd5 Content MD5 of source content to copy.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5) {
        this.sourceContentMd5 = CoreUtils.clone(sourceContentMd5);
        return this;
    }

    /**
     * Gets {@link PageBlobRequestConditions} for writing to destination.
     *
     * @return {@link PageBlobRequestConditions} for writing to destination.
     */
    public PageBlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * Sets {@link PageBlobRequestConditions} for writing to destination.
     *
     * @param destinationRequestConditions {@link PageBlobRequestConditions} for writing to destination.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions
        setDestinationRequestConditions(PageBlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * Gets {@link BlobRequestConditions} for accessing source content.
     *
     * @return {@link BlobRequestConditions} for accessing source content.
     */
    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * Sets {@link BlobRequestConditions} for accessing source content.
     *
     * @param sourceRequestConditions {@link BlobRequestConditions} for accessing source content.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * Gets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
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

    /**
     * Optional, only applicable (but required) when the source is Azure Storage Files and using token authentication.
     * Gets the intent of the request.
     *
     * @return the {@link FileShareTokenIntent} for the file share.
     */
    public FileShareTokenIntent getSourceShareTokenIntent() {
        return sourceShareTokenIntent;
    }

    /**
     * Optional, only applicable (but required) when the source is Azure Storage Files and using token authentication.
     * Sets the intent of the request.
     *
     * @param sourceShareTokenIntent Used to indicate the intent of the request.
     * @return The updated options.
     */
    public PageBlobUploadPagesFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent) {
        this.sourceShareTokenIntent = sourceShareTokenIntent;
        return this;
    }
}
