// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.util.CoreUtils;
import com.azure.core.experimental.http.HttpAuthorization;
import com.azure.storage.blob.models.AccessTier;
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

    /**
     * @param sourceUrl The source URL to upload from.
     */
    public BlobUploadFromUrlOptions(String sourceUrl) {
        StorageImplUtils.assertNotNull("copySource", sourceUrl);
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return The source URL to upload from.
     */
    public String getSourceUrl() {
        return this.sourceUrl;
    }

    /**
     * @return {@link BlobHttpHeaders}
     */
    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    /**
     * @param headers {@link BlobHttpHeaders}
     * @return The updated options
     */
    public BlobUploadFromUrlOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @return The tags to associate with the blob.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tags The tags to associate with the blob.
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * @return {@link AccessTier}
     */
    public AccessTier getTier() {
        return tier;
    }

    /**
     * @param tier {@link AccessTier}
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setTier(AccessTier tier) {
        this.tier = tier;
        return this;
    }

    /**
     * @return An MD5 hash of the content. This hash is used to verify the integrity of the content during
     * transport. When this header is specified, the storage service compares the hash of the content that has arrived
     * with this header value. Note that this MD5 hash is not stored with the blob. If the two hashes do not match, the
     * operation will fail.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
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
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getDestinationRequestConditions() {
        return destinationRequestConditions;
    }

    /**
     * @param destinationRequestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setDestinationRequestConditions(BlobRequestConditions
                                                                        destinationRequestConditions) {
        this.destinationRequestConditions = destinationRequestConditions;
        return this;
    }

    /**
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getSourceRequestConditions() {
        return sourceRequestConditions;
    }

    /**
     * @param sourceRequestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions) {
        this.sourceRequestConditions = sourceRequestConditions;
        return this;
    }

    /**
     * Optional, default is true.  Indicates if properties from the source blob should be copied.
     * @return Whether properties from the source blob should be copied.
     */
    public Boolean isCopySourceBlobProperties() {
        return copySourceBlobProperties;
    }

    /**
     * Optional, default is true.  Indicates if properties from the source blob should be copied.
     * @param copySourceBlobProperties Whether properties from the source blob should be copied.
     * @return The updated options.
     */
    public BlobUploadFromUrlOptions setCopySourceBlobProperties(Boolean copySourceBlobProperties) {
        this.copySourceBlobProperties = copySourceBlobProperties;
        return this;
    }

    /**
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
}
