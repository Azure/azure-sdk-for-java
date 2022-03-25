// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.options;

import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

import java.time.Duration;
import java.util.Objects;

public class PageBlobGetPageRangesDiffOptions {
    private final BlobRange range;
    private final String previousSnapshot;
    private BlobRequestConditions requestConditions;
    private Integer pageSize;
    private Duration timeout;


    public PageBlobGetPageRangesDiffOptions(BlobRange range, String previousSnapshot) {
        Objects.requireNonNull(range);
        Objects.requireNonNull(previousSnapshot);
        this.range = new BlobRange(range.getOffset(), range.getCount());
        this.previousSnapshot = previousSnapshot;
    }

    public BlobRange getRange() {
        return this.range;
    }

    public String getPreviousSnapshot() {
        return this.previousSnapshot;
    }

    public PageBlobGetPageRangesDiffOptions setMaxResultsPerPage(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getMaxResultsPerPage() {
        return this.pageSize;
    }

    public BlobRequestConditions getRequestConditions() {
        return this.requestConditions;
    }

    public PageBlobGetPageRangesDiffOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    public PageBlobGetPageRangesDiffOptions setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
}
