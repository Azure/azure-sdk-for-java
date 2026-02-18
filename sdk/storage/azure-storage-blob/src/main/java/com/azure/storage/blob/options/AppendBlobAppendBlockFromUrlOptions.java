// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpAuthorization;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.FileShareTokenIntent;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

/**
 * Extended options that may be passed when appending a block from a source URL.
 */
@Fluent
public final class AppendBlobAppendBlockFromUrlOptions {
    private final String sourceUrl;
    private BlobRange sourceRange;
    private byte[] sourceContentMd5;
    private AppendBlobRequestConditions destinationRequestConditions;
    private BlobRequestConditions sourceRequestConditions;
    private HttpAuthorization sourceAuthorization;
    private FileShareTokenIntent sourceShareTokenIntent;
    private CustomerProvidedKey sourceCustomerProvidedKey;

    /**
     * Creates a new instance of {@link AppendBlobAppendBlockFromUrlOptions}.
     *
     * @param sourceUrl The source URL to copy from. URLs outside of Azure may only be copied to block blobs.
     */
    public AppendBlobAppendBlockFromUrlOptions(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * Gets the source URL to copy from.
     *
     * @return Source URL to copy from.
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
    public AppendBlobAppendBlockFromUrlOptions setSourceRange(BlobRange sourceRange) {
        this.sourceRange = sourceRange;
        return this;
    }

    /**
     * Gets the MD5 of the source content to be appended.
     *
     * @return MD5 of the source content to be appended.
     */
    public byte[] getSourceContentMd5() {
        return CoreUtils.clone(sourceContentMd5);
    }

    /**
     * Sets the MD5 of the source content to be appended.
     *
     * @param sourceContentMd5 MD5 of the source content to be appended.
     * @return The updated options.
     */
    public AppendBlobAppendBlockFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5) {
        this.sourceContentMd5 = CoreUtils.clone(sourceContentMd5);
        return this;
    }

    /**
     * Gets the {@link AppendBlobRequestConditions} for writing to destination.
     *
     * @return {@link AppendBlobRequestConditions} for writing to destination.
     */
    public AppendBlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * Sets the {@link AppendBlobRequestConditions} for writing to destination.
     *
     * @param destinationRequestConditions {@link AppendBlobRequestConditions} for writing to destination.
     * @return The updated options.
     */
    public AppendBlobAppendBlockFromUrlOptions
        setDestinationRequestConditions(AppendBlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * Gets the {@link BlobRequestConditions} for accessing source.
     *
     * @return {@link BlobRequestConditions} for accessing source.
     */
    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions} for accessing source.
     *
     * @param sourceRequestConditions {@link BlobRequestConditions} for accessing source.
     * @return The updated options.
     */
    public AppendBlobAppendBlockFromUrlOptions
        setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * Gets the auth header for accessing source.
     *
     * @return auth header for accessing source.
     */
    public HttpAuthorization getSourceAuthorization() {
        return sourceAuthorization;
    }

    /**
     * Sets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @param sourceAuthorization auth header for accessing source.
     * @return The updated options.
     */
    public AppendBlobAppendBlockFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization) {
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
    public AppendBlobAppendBlockFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent) {
        this.sourceShareTokenIntent = sourceShareTokenIntent;
        return this;
    }

    /**
     * Gets the optional {@link CustomerProvidedKey} used for encrypting the source blob.
     * Applicable only for service version 2026-02-06 or later.
     *
     * @return the {@link CustomerProvidedKey} used for encrypting the source blob.
     */
    public CustomerProvidedKey getSourceCustomerProvidedKey() {
        return sourceCustomerProvidedKey;
    }

    /**
     * Sets the optional {@link CustomerProvidedKey} used for encrypting the source blob.
     * Applicable only for service version 2026-02-06 or later.
     *
     * @param sourceCustomerProvidedKey The {@link CustomerProvidedKey} used for encrypting the source blob.
     * @return The updated options.
     */
    public AppendBlobAppendBlockFromUrlOptions
        setSourceCustomerProvidedKey(CustomerProvidedKey sourceCustomerProvidedKey) {
        this.sourceCustomerProvidedKey = sourceCustomerProvidedKey;
        return this;
    }
}
