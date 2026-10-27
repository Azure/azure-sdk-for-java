// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when listing ranges for a file.
 */
@Fluent
public class ShareFileListRangesOptions {
    private ShareFileRange range;
    private ShareRequestConditions requestConditions;

    /**
     * Creates a new instance of {@link ShareFileListRangesOptions}.
     */
    public ShareFileListRangesOptions() {
    }

    /**
     * Gets the range of bytes over which to list ranges, inclusively.
     *
     * @return The range of bytes over which to list ranges, inclusively.
     */
    public ShareFileRange getRange() {
        return range;
    }

    /**
     * Sets the range of bytes over which to list ranges, inclusively.
     *
     * @param range The range of bytes over which to list ranges, inclusively.
     * @return The updated options.
     */
    public ShareFileListRangesOptions setRange(ShareFileRange range) {
        this.range = range;
        return this;
    }

    /**
     * Gets the {@link ShareRequestConditions}.
     *
     * @return {@link ShareRequestConditions}
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link ShareRequestConditions}.
     *
     * @param requestConditions {@link ShareRequestConditions} for the file.
     * @return The updated options.
     */
    public ShareFileListRangesOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
