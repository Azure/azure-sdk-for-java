// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

/**
 * Extended options that may be passed when uploading a Block Blob in parallel.
 */
@Fluent
public class BlobParallelUploadOptions {
    private final InputStream dataStream;
    private final long length;
    private ParallelTransferOptions parallelTransferOptions;
    private BlobHttpHeaders headers;
    private Map<String, String> metadata;
    private AccessTier tier;
    private BlobRequestConditions requestConditions;
    private Duration timeout;

    /**
     * Constructs a new {@code BlobParalleUploadOptions}.
     *
     * @param dataStream The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    public BlobParallelUploadOptions(InputStream dataStream, long length) {
        this.dataStream = dataStream;
        this.length = length;
    }

    /**
     * Gets the data source.
     *
     * @return The data to write to the blob.
     */
    public InputStream getDataStream() {
        return this.dataStream;
    }

    /**
     * Gets the length of the data.
     *
     * @return The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    public long getLength() {
        return length;
    }

    /**
     * Gets the {@link ParallelTransferOptions}.
     *
     * @return {@link ParallelTransferOptions}
     */
    public ParallelTransferOptions getParallelTransferOptions() {
        return parallelTransferOptions;
    }

    /**
     * Sets the {@link ParallelTransferOptions}.
     *
     * @param parallelTransferOptions {@link ParallelTransferOptions}
     * @return The updated options.
     */
    public BlobParallelUploadOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
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
     * @return The updated options
     */
    public BlobParallelUploadOptions setHeaders(BlobHttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the metadata.
     *
     * @return The metadata to associate with the blob.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata.
     *
     * @param metadata The metadata to associate with the blob.
     * @return The updated options.
     */
    public BlobParallelUploadOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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
    public BlobParallelUploadOptions setTier(AccessTier tier) {
        this.tier = tier;
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
    public BlobParallelUploadOptions setRequestConditions(BlobRequestConditions requestConditions) {
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
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The updated options.
     */
    public BlobParallelUploadOptions setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
}
