// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobRequestConditions;

import java.util.Map;

/**
 * Extended options that may be passed when creating a Page Blob.
 */
public class PageBlobCreateOptions {
    private final long size;
    private Long sequenceNumber;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private BlobRequestConditions requestConditions;
    private BlobImmutabilityPolicy immutabilityPolicy;
    private Boolean legalHold;

    /**
     * Creates a new instance of {@link PageBlobCreateOptions}.
     *
     * @param size Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     */
    public PageBlobCreateOptions(long size) {
        this.size = size;
    }

    /**
     * Gets the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a 512-byte boundary.
     *
     * @return Specifies the maximum size for the page blob, up to 8 TB. The page blob size must be aligned to a
     * 512-byte boundary.
     */
    public long getSize() {
        return this.size;
    }

    /**
     * Gets a user-controlled value that you can use to track requests. The value of the sequence number must be
     * between 0 and 2^63 - 1.The default value is 0.
     *
     * @return A user-controlled value that you can use to track requests. The value of the sequence
     * number must be between 0 and 2^63 - 1.The default value is 0.
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets a user-controlled value that you can use to track requests. The value of the sequence number must be
     * between 0 and 2^63 - 1.The default value is 0.
     *
     * @param sequenceNumber A user-controlled value that you can use to track requests. The value of the sequence
     * number must be between 0 and 2^63 - 1.The default value is 0.
     * @return The updated options.
     */
    public PageBlobCreateOptions setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
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
    public PageBlobCreateOptions setHeaders(BlobHttpHeaders headers) {
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
     * @return The updated options.
     */
    public PageBlobCreateOptions setMetadata(Map<String, String> metadata) {
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
    public PageBlobCreateOptions setTags(Map<String, String> tags) {
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
    public PageBlobCreateOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the {@link BlobImmutabilityPolicy}.
     *
     * @return {@link BlobImmutabilityPolicy}
     */
    public BlobImmutabilityPolicy getImmutabilityPolicy() {
        return immutabilityPolicy;
    }

    /**
     * Sets the {@link BlobImmutabilityPolicy}.
     * <p>
     * Note that this parameter is only applicable to a blob within a container that has immutable storage with
     * versioning enabled.
     *
     * @param immutabilityPolicy {@link BlobImmutabilityPolicy}
     * @return The updated options.
     */
    public PageBlobCreateOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy) {
        this.immutabilityPolicy = immutabilityPolicy;
        return this;
    }

    /**
     * Gets if a legal hold should be placed on the blob.
     *
     * @return If a legal hold should be placed on the blob.
     */
    public Boolean isLegalHold() {
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
    public PageBlobCreateOptions setLegalHold(Boolean legalHold) {
        this.legalHold = legalHold;
        return this;
    }
}
