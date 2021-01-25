// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ConsistentReadControl;

/**
 * Extended options that may be passed when opening a blob input stream.
 */
@Fluent
public class BlobInputStreamOptions {

    private BlobRange range;
    private BlobRequestConditions requestConditions;
    private Integer blockSize;
    private ConsistentReadControl consistentReadControl;

    /**
     * @return {@link BlobRange}
     */
    public BlobRange getRange() {
        return range;
    }

    /**
     * @param range {@link BlobRange}
     * @return The updated options.
     */
    public BlobInputStreamOptions setRange(BlobRange range) {
        this.range = range;
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
    public BlobInputStreamOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * @return The size of each data chunk returned from the service. If block size is large, input stream will make
     * fewer network calls, but each individual call will send more data and will therefore take longer.
     * The default value is 4 MB.
     */
    public Integer getBlockSize() {
        return blockSize;
    }

    /**
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
     * @return {@link ConsistentReadControl} Default is E-Tag.
     */
    public ConsistentReadControl getConsistentReadControl() {
        return consistentReadControl;
    }

    /**
     * @param consistentReadControl {@link ConsistentReadControl} Default is E-Tag.
     * @return The updated options.
     */
    public BlobInputStreamOptions setConsistentReadControl(ConsistentReadControl consistentReadControl) {
        this.consistentReadControl = consistentReadControl;
        return this;
    }
}
