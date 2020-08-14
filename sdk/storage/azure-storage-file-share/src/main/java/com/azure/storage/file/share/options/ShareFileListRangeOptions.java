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
    private String prevSnapshot;
    private ShareRequestConditions requestConditions;

    public ShareFileListRangeOptions() {
    }

    public ShareFileRange getRange() {
        return range;
    }

    public ShareFileListRangeOptions setRange(ShareFileRange range) {
        this.range = range;
        return this;
    }

    public String getPrevSnapshot() {
        return prevSnapshot;
    }

    public ShareFileListRangeOptions setPrevSnapshot(String prevSnapshot) {
        this.prevSnapshot = prevSnapshot;
        return this;
    }

    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    public ShareFileListRangeOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
