// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.http.HttpAuthorization;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.FileShareTokenIntent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobCopySourceTagsMode;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

import java.util.Map;

/**
 * Extended options that may be passed when uploading a Block Blob from URL.
 */
public class BlobUploadFromUrlOptions {
    private final String sourceUrl;
    private BlobHttpHeaders headers;
    private Map<String, String> tags;
    private AccessTier tier;
    private byte[] contentMd5;
    private BlobRequestConditions destinationRequestConditions;
    private BlobRequestConditions sourceRequestConditions;
    private Boolean copySourceBlobProperties;
    private HttpAuthorization sourceAuthorization;
    private BlobCopySourceTagsMode copySourceTags;
    private FileShareTokenIntent sourceShareTokenIntent;
    private CustomerProvidedKey sourceCustomerProvidedKey;

    /**
     * Creates a new instance of {@link BlobUploadFromUrlOptions}.
     *
     * @param sourceUrl The source URL to upload from.
     */
    public BlobUploadFromUrlOptions(String sourceUrl) {
        StorageImplUtils.assertNotNull("copySource", sourceUrl);
        this.sourceUrl = sourceUrl;
    }

    /**
     * Gets the source URL to upload from.
     *
     * @return The source URL to upload from.
     */
    public String getSourceUrl() {
        return this.sourceUrl;
    }

    /**
     * Gets the {@link BlobHttpHeaders}.
     *
     * @return {@link BlobHttpHeaders}
     */
    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Sets the {@link BlobHttpHeaders}.
     *
     * @param headers {@link BlobHttpHeaders}
     * @return The updated options
     */
    public BlobUploadFromUrlOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the tags to associate with the blob.
     *
     * @return The tags to associate with the blob.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Sets the tags to associate with the blob.
     *
     * @param tags The tags to associate with the blob.
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the {@link AccessTier}.
     *
     * @return {@link AccessTier}
     */
    public AccessTier getTier() {
        return tier;
    }

    /**
     * Sets the {@link AccessTier}.
     *
     * @param tier {@link AccessTier}
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    /**
     * Gets the MD5 hash of the content.
     *
     * @return An MD5 hash of the content. This hash is used to verify the integrity of the content during transport.
     * When this header is specified, the storage service compares the hash of the content that has arrived with this
     * header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the operation
     * will fail.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Sets the MD5 hash of the content.
     *
     * @param contentMd5 An MD5 hash of the block content. This hash is used to verify the integrity of the block during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     * @return The updated options
     */
    public BlobUploadFromUrlOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = CoreUtils.clone(contentMd5);
        return this;
    }

    /**
     * Gets the {@link BlobRequestConditions}.
     *
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions}.
     *
     * @param destinationRequestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions
        setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * Gets the {@link BlobRequestConditions}.
     *
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions}.
     *
     * @param sourceRequestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * Optional, default is true.  Indicates if properties from the source blob should be copied.
     *
     * @return Whether properties from the source blob should be copied.
     */
    public Boolean isCopySourceBlobProperties() {
        return copySourceBlobProperties;
    }

    /**
     * Optional, default is true.  Indicates if properties from the source blob should be copied.
     *
     * @param copySourceBlobProperties Whether properties from the source blob should be copied.
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setCopySourceBlobProperties(Boolean copySourceBlobProperties) {
        this.copySourceBlobProperties = copySourceBlobProperties;
        return this;
    }

    /**
     * Gets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @return auth header for access to source.
     */
    public HttpAuthorization getSourceAuthorization() {
        return sourceAuthorization;
    }

    /**
     * Sets "Authorization" header for accessing source URL. Currently only "Bearer" authentication is accepted by
     * Storage.
     *
     * @param sourceAuthorization auth header for access to source.
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization) {
        this.sourceAuthorization = sourceAuthorization;
        return this;
    }

    /**
     * Gets the copy source tags mode
     *
     * @return The copy source tags mode.
     */
    public BlobCopySourceTagsMode getCopySourceTagsMode() {
        return copySourceTags;
    }

    /**
     * Sets the copy source tags mode
     *
     * @param copySourceTags Indicates if a legal hold should be placed on the blob.
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setCopySourceTagsMode(BlobCopySourceTagsMode copySourceTags) {
        this.copySourceTags = copySourceTags;
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
    public BlobUploadFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent) {
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
    public BlobUploadFromUrlOptions setSourceCustomerProvidedKey(CustomerProvidedKey sourceCustomerProvidedKey) {
        this.sourceCustomerProvidedKey = sourceCustomerProvidedKey;
        return this;
    }
}
