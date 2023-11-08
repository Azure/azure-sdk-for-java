// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.http.HttpRange;
import com.azure.storage.blob.specialized.PageBlobClient;

/**
 * Represents a page range on a page blob returned by {@link PageBlobClient#listPageRanges(BlobRange)}.
 */
@Immutable
public final class PageRangeItem {
    private final HttpRange range;
    private final boolean isClear;

    /**
     * Creates a new {@code PageRangeItem}.
     *
     * @param range The {@link HttpRange}.
     * @param isClear Whether the bytes are cleared.
     */
    public PageRangeItem(HttpRange range, boolean isClear) {
        this.range = range;
        this.isClear = isClear;
    }

    /**
     * Gets the range property.
     *
     * @return The range property.
     */
    public HttpRange getRange() {
        return range;
    }

    /**
     * Gets whether the range is cleared.
     *
     * @return Whether the range is cleared.
     */
    public boolean isClear() {
        return this.isClear;
    }
}
