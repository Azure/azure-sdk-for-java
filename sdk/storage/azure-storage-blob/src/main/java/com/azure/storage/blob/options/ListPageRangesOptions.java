// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

import java.util.Objects;

/**
 * Extended options that may be passed when getting the page ranges of a page blob.
 */
@Fluent
public class ListPageRangesOptions {
    private final BlobRange range;
    private BlobRequestConditions requestConditions;
    private Integer pageSize;


    /**
     * @param range The range to diff.
     */
    public ListPageRangesOptions(BlobRange range) {
        Objects.requireNonNull(range);
        this.range = new BlobRange(range.getOffset(), range.getCount());
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
     * @param requestConditions The requestConditions value to set.
     * @return The updated object
     */
    public ListPageRangesOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the pageSize property.
     *
     * @return The pageSize property.
     */
    public Integer getMaxResultsPerPage() {
        return pageSize;
    }

    /**
     * Sets the pageSize property.
     *
     * @param pageSize The pageSize value to set.
     * @return The updated object
     */
    public ListPageRangesOptions setMaxResultsPerPage(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}
