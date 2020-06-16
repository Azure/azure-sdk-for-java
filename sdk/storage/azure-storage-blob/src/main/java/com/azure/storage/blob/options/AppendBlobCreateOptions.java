// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;

import java.time.Duration;
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
    private Duration timeout;

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
    public AppendBlobCreateOptions setHeaders(BlobHttpHeaders headers) {
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
     * @return The updated options
     */
    public AppendBlobCreateOptions setMetadata(Map<String, String> metadata) {
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
    public AppendBlobCreateOptions setTags(Map<String, String> tags) {
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
    public AppendBlobCreateOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the timeout.
     *
     * @return An optional timeout value beyond which a {@link RuntimeException} will be raised.
     */
    public Duration getTimeout() {
        return this.timeout;
    }

    /**
     * Sets the timeout.
     * <p>
     * This value will be ignored on async operations and must be set on the returned async object itself.
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The updated options.
     */
    public AppendBlobCreateOptions setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
}
