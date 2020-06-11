// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;
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
     * @param dataStream The data to write to the blob.
     * @param length The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    public BlobParallelUploadOptions(InputStream dataStream, long length) {
        this.dataStream = dataStream;
        this.length = length;
    }

    /**
     * @return The data to write to the blob.
     */
    public InputStream getDataStream() {
        return this.dataStream;
    }

    /**
     * @return The exact length of the data. It is important that this value match precisely the length of the
     * data provided in the {@link InputStream}.
     */
    public long getLength() {
        return length;
    }

    /**
     * @return {@link ParallelTransferOptions}
     */
    public ParallelTransferOptions getParallelTransferOptions() {
        return parallelTransferOptions;
    }

    /**
     * @param parallelTransferOptions {@link ParallelTransferOptions}
     * @return The updated options.
     */
    public BlobParallelUploadOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions) {
        this.parallelTransferOptions = parallelTransferOptions;
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
     * @return The updated options
     */
    public BlobParallelUploadOptions setHeaders(BlobHttpHeaders headers) {
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
    public BlobParallelUploadOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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
    public BlobParallelUploadOptions setTier(AccessTier tier) {
        this.tier = tier;
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
    public BlobParallelUploadOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    /**
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The updated options.
     */
    public BlobParallelUploadOptions setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
}
