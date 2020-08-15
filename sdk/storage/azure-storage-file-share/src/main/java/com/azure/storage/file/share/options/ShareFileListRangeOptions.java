// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when listing ranges for a File.
 */
@Fluent
public class ShareFileListRangeOptions {

    private ShareFileRange range;
    private String previousSnapshot;
    private ShareRequestConditions requestConditions;

    /**
     * @return The range of bytes over which to list ranges, inclusively.
     */
    public ShareFileRange getRange() {
        return range;
    }

    /**
     * @param range The range of bytes over which to list ranges, inclusively.
     * @return The updated options.
     */
    public ShareFileListRangeOptions setRange(ShareFileRange range) {
        this.range = range;
        return this;
    }

    /**
     * @return The previous snapshot to compare to.
     */
    public String getPreviousSnapshot() {
        return previousSnapshot;
    }

    /**
     * @param previousSnapshot Specifies that the response will contain only ranges that were changed between target
     * file and previous snapshot. Changed ranges include both updated and cleared ranges. The target file may be a
     * snapshot, as long as the snapshot specified by previousSnapshot is the older of the two.
     * @return The updated options.
     */
    public ShareFileListRangeOptions setPreviousSnapshot(String previousSnapshot) {
        this.previousSnapshot = previousSnapshot;
        return this;
    }

    /**
     * @return {@link ShareRequestConditions}
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link ShareRequestConditions} for the file.
     * @return The updated options.
     */
    public ShareFileListRangeOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
