// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when listing ranges for a File.
 */
@Fluent
public class ShareFileListRangesDiffOptions {

    private ShareFileRange range;
    private final String previousSnapshot;
    private ShareRequestConditions requestConditions;

    /**
     * @param previousSnapshot Specifies that the response will contain only ranges that were changed between target
     * file and previous snapshot. Changed ranges include both updated and cleared ranges. The target file may be a
     * snapshot, as long as the snapshot specified by previousSnapshot is the older of the two.
     */
    public ShareFileListRangesDiffOptions(String previousSnapshot) {
        StorageImplUtils.assertNotNull("previousSnapshot", previousSnapshot);
        this.previousSnapshot = previousSnapshot;
    }

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
    public ShareFileListRangesDiffOptions setRange(ShareFileRange range) {
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
     * @return {@link ShareRequestConditions}
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link ShareRequestConditions} for the file.
     * @return The updated options.
     */
    public ShareFileListRangesDiffOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
