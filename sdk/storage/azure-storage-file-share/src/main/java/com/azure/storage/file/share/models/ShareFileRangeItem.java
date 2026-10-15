// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents a range returned by a paged file range-list operation.
 */
@Immutable
public final class ShareFileRangeItem {
    private final ShareFileRange range;
    private final boolean isClear;

    /**
     * Creates a new {@link ShareFileRangeItem}.
     *
     * @param range The file range.
     * @param isClear Whether the range is cleared.
     */
    public ShareFileRangeItem(ShareFileRange range, boolean isClear) {
        this.range = range;
        this.isClear = isClear;
    }

    /**
     * Gets the file range.
     *
     * @return The file range.
     */
    public ShareFileRange getRange() {
        return range;
    }

    /**
     * Gets whether the range is cleared.
     *
     * @return Whether the range is cleared.
     */
    public boolean isClear() {
        return isClear;
    }
}
