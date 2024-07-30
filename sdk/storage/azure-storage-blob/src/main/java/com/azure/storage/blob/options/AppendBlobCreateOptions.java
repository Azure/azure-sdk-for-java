// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRequestConditions;

import java.util.Map;

/**
 * Extended options that may be passed when creating an Append Blob.
 */
@Fluent
public class AppendBlobCreateOptions {
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private BlobRequestConditions requestConditions;
    private BlobImmutabilityPolicy immutabilityPolicy;
    private Boolean legalHold;

    /**
     * Creates a new instance of {@link AppendBlobCreateOptions}.
     */
    public AppendBlobCreateOptions() {
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
     * @return The updated {@code AppendBlobCreateOptions}
     */
    public AppendBlobCreateOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the metadata to associate with the blob.
     *
     * @return The metadata to associate with the blob.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata to associate with the blob.
     *
     * @param metadata The metadata to associate with the blob.
     * @return The updated options
     */
    public AppendBlobCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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
    public AppendBlobCreateOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the {@link BlobRequestConditions}.
     *
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions}.
     *
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public AppendBlobCreateOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the immutability policy for the blob.
     *
     * @return {@link BlobImmutabilityPolicy}
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return immutabilityPolicy;
    }

    /**
     * Sets the immutability policy for the blob.
     * <p>
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     *
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy}
     * @return The updated options.
     */
    public AppendBlobCreateOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        this.immutabilityPolicy = immutabilityPolicy;
        return this;
    }

    /**
     * Gets if a legal hold should be placed on the blob.
     *
     * @return If a legal hold should be placed on the blob.
     */
    public Boolean hasLegalHold() {
        return legalHold;
    }

    /**
     * Sets if a legal hold should be placed on the blob.
     * <p>
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     *
     * @param legalHold Indicates if a legal hold should be placed on the blob.
     * @return The updated options.
     */
    public AppendBlobCreateOptions setLegalHold(Boolean legalHold) {
        this.legalHold = legalHold;
        return this;
    }
}
