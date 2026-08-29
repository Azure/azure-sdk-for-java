// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

/**
 * Extended options that may be passed when getting the layout of a blob.
 */
@Fluent
public class BlobGetLayoutOptions {
    private BlobRange range;
    private BlobRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link BlobGetLayoutOptions}.
     */
    public BlobGetLayoutOptions() {
    }

    /**
     * Gets the range property.
     *
     * @return The range property.
     */
    public BlobRange getRange() {
        return range;
    }

    /**
     * Sets the range property.
     *
     * @param range The range value to set.
     * @return The updated object
     */
    public BlobGetLayoutOptions setRange(BlobRange range) {
        this.range = range == null ? null : new BlobRange(range.getOffset(), range.getCount());
        return this;
    }

    /**
     * Gets the requestConditions property.
     *
     * @return The requestConditions property.
     */
    public BlobRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the requestConditions property.
     *
     * @param requestConditions The requestConditions value to set by copying the values from the provided RequestConditions.
     * @return The updated object
     */
    public BlobGetLayoutOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions == null ? null : new BlobRequestConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince())
            .setLeaseId(requestConditions.getLeaseId());
        return this;
    }
}
