// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.common.implementation.contentvalidation.StorageChecksumAlgorithm;

/**
 * Extended options that may be passed when downloading blob content (full blob or range in memory).
 */
@Fluent
public final class BlobDownloadContentOptions {
    private BlobRange range;
    private DownloadRetryOptions downloadRetryOptions;
    private BlobRequestConditions requestConditions;
    private boolean retrieveContentRangeMd5;
    private StorageChecksumAlgorithm responseChecksumAlgorithm;

    /**
     * Creates a new instance of {@link BlobDownloadContentOptions}.
     */
    public BlobDownloadContentOptions() {
    }

    /**
     * Gets the {@link BlobRange}.
     *
     * @return The blob range.
     */
    public BlobRange getRange() {
        return range;
    }

    /**
     * Sets the {@link BlobRange}.
     *
     * @param range The blob range.
     * @return The updated options.
     */
    public BlobDownloadContentOptions setRange(BlobRange range) {
        this.range = range;
        return this;
    }

    /**
     * Gets the {@link DownloadRetryOptions}.
     *
     * @return The download retry options.
     */
    public DownloadRetryOptions getDownloadRetryOptions() {
        return downloadRetryOptions;
    }

    /**
     * Sets the {@link DownloadRetryOptions}.
     *
     * @param downloadRetryOptions The download retry options.
     * @return The updated options.
     */
    public BlobDownloadContentOptions setDownloadRetryOptions(DownloadRetryOptions downloadRetryOptions) {
        this.downloadRetryOptions = downloadRetryOptions;
        return this;
    }

    /**
     * Gets the {@link BlobRequestConditions}.
     *
     * @return The request conditions.
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link BlobRequestConditions}.
     *
     * @param requestConditions The request conditions.
     * @return The updated options.
     */
    public BlobDownloadContentOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets whether the content MD5 for the specified blob range should be returned.
     *
     * @return Whether to retrieve content range MD5.
     */
    public boolean isRetrieveContentRangeMd5() {
        return retrieveContentRangeMd5;
    }

    /**
     * Sets whether the content MD5 for the specified blob range should be returned.
     *
     * @param retrieveContentRangeMd5 Whether to retrieve content range MD5.
     * @return The updated options.
     */
    public BlobDownloadContentOptions setRetrieveContentRangeMd5(boolean retrieveContentRangeMd5) {
        this.retrieveContentRangeMd5 = retrieveContentRangeMd5;
        return this;
    }

    /**
     * Gets the algorithm to use for response content validation. Default is {@link StorageChecksumAlgorithm#NONE}.
     *
     * @return The response checksum algorithm.
     */
    public StorageChecksumAlgorithm getResponseChecksumAlgorithm() {
        return responseChecksumAlgorithm;
    }

    /**
     * Sets the algorithm to use for response content validation. When set to {@link StorageChecksumAlgorithm#AUTO},
     * {@link StorageChecksumAlgorithm#CRC64}, or {@link StorageChecksumAlgorithm#MD5}, the SDK will validate response
     * payload checksums during download. Default is {@link StorageChecksumAlgorithm#NONE}.
     *
     * @param responseChecksumAlgorithm The response checksum algorithm.
     * @return The updated options.
     */
    public BlobDownloadContentOptions setResponseChecksumAlgorithm(StorageChecksumAlgorithm responseChecksumAlgorithm) {
        this.responseChecksumAlgorithm = responseChecksumAlgorithm;
        return this;
    }
}
