// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.options;

import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

import java.time.Duration;
import java.util.Objects;

public class PageBlobGetPageRangesOptions {
    private final BlobRange range;
    private BlobRequestConditions requestConditions;
    private Integer pageSize;
    private Duration timeout;


    public PageBlobGetPageRangesOptions(BlobRange range) {
        Objects.requireNonNull(range);
        this.range = new BlobRange(range.getOffset(), range.getCount());
    }

    public BlobRange getRange() {
        return this.range;
    }

    public PageBlobGetPageRangesOptions setMaxResultsPerPage(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getMaxResultsPerPage() {
        return this.pageSize;
    }

    public BlobRequestConditions getRequestConditions() {
        return this.requestConditions;
    }

    public PageBlobGetPageRangesOptions setRequestConditions(BlobRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    public Duration getTimeout() {
        return this.timeout;
    }

    public PageBlobGetPageRangesOptions setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
}
