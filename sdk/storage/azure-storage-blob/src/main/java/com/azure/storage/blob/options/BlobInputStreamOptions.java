// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ConsistentReadControl;
import com.azure.storage.common.implementation.contentvalidation.DownloadContentValidationOptions;

/**
 * Extended options that may be passed when opening a blob input stream.
 */
@Fluent
public class BlobInputStreamOptions {
    private BlobRange range;
    private BlobRequestConditions requestConditions;
    private Integer blockSize;
    private ConsistentReadControl consistentReadControl;
    private DownloadContentValidationOptions contentValidationOptions;

    /**
     * Creates a new instance of {@link BlobInputStreamOptions}.
     */
    public BlobInputStreamOptions() {
    }

    /**
     * Gets the {@link BlobRange}.
     *
     * @return {@link BlobRange}
     */
    public BlobRange getRange() {
        return range;
    }

    /**
     * Sets the {@link BlobRange}.
     *
     * @param range {@link BlobRange}
     * @return The updated options.
     */
    public BlobInputStreamOptions setRange(BlobRange range) {
        this.range = range;
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
    public BlobInputStreamOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the size of each data chunk returned from the service. If block size is large, input stream will make
     * fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     *
     * @return The size of each data chunk returned from the service. If block size is large, input stream will make
     * fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     */
    public Integer getBlockSize() {
        return blockSize;
    }

    /**
     * Sets the size of each data chunk returned from the service. If block size is large, input stream will make
     * fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     *
     * @param blockSize The size of each data chunk returned from the service. If block size is large, input stream
     * will make fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     * @return The updated options.
     */
    public BlobInputStreamOptions setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
        return this;
    }

    /**
     * Gets the {@link ConsistentReadControl} Default is E-Tag.
     *
     * @return {@link ConsistentReadControl} Default is E-Tag.
     */
    public ConsistentReadControl getConsistentReadControl() {
        return consistentReadControl;
    }

    /**
     * Sets the {@link ConsistentReadControl} Default is E-Tag.
     *
     * @param consistentReadControl {@link ConsistentReadControl} Default is E-Tag.
     * @return The updated options.
     */
    public BlobInputStreamOptions setConsistentReadControl(ConsistentReadControl consistentReadControl) {
        this.consistentReadControl = consistentReadControl;
        return this;
    }

    /**
     * Gets the {@link DownloadContentValidationOptions}.
     *
     * @return {@link DownloadContentValidationOptions}
     */
    public DownloadContentValidationOptions getContentValidationOptions() {
        return contentValidationOptions;
    }

    /**
     * Sets the {@link DownloadContentValidationOptions}.
     *
     * @param contentValidationOptions {@link DownloadContentValidationOptions}
     * @return The updated options.
     */
    public BlobInputStreamOptions setContentValidationOptions(DownloadContentValidationOptions contentValidationOptions) {
        this.contentValidationOptions = contentValidationOptions;
        return this;
    }
}
