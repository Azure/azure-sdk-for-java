// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.Map;

/**
 * Extended options that may be passed when creating a Page Blob.
 */
public class PageBlobCreateOptions {
    private Long sequenceNumber;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private Map<String, String> tags;
    private BlobRequestConditions requestConditions;

    /**
     * @return A user-controlled value that you can use to track requests. The value of the sequence
     * number must be between 0 and 2^63 - 1.The default value is 0.
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * @param sequenceNumber A user-controlled value that you can use to track requests. The value of the sequence
     * number must be between 0 and 2^63 - 1.The default value is 0.
     * @return The updated options.
     */
    public PageBlobCreateOptions setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    /**
     * @return {@link BlobHttpHeaders}
     */
    public BlobHttpHeaders getHeaders() {
        return headers;
    }

    /**
     * @param headers {@link BlobHttpHeaders}
     * @return The updated {@code AppendBlobCreateOptions}
     */
    public PageBlobCreateOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * @return The metadata to associate with the blob.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata The metadata to associate with the blob.
     * @return The updated options.
     */
    public PageBlobCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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
    public PageBlobCreateOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * @return {@link BlobRequestConditions}
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link BlobRequestConditions}
     * @return The updated options.
     */
    public PageBlobCreateOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
