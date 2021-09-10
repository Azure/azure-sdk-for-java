// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.DownloadRetryOptions;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;

/**
 * Extended options that may be passed when downloading a File.
 */
@Fluent
public final class ShareFileDownloadOptions {

    private ShareFileRange range;
    private Boolean rangeContentMd5Requested;
    private ShareRequestConditions requestConditions;
    private DownloadRetryOptions retryOptions;

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
    public ShareFileDownloadOptions setRange(ShareFileRange range) {
        this.range = range;
        return this;
    }

    /**
     * @return Whether or not the service returns the MD5 hash for the range.
     */
    public Boolean isRangeContentMd5Requested() {
        return rangeContentMd5Requested;
    }

    /**
     * @param getRangeContentMd5 Whether or not the service returns the MD5 hash for the range.
     * @return The updated options.
     */
    public ShareFileDownloadOptions setRangeContentMd5Requested(Boolean rangeContentMd5Requested) {
        this.rangeContentMd5Requested = rangeContentMd5Requested;
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
    public ShareFileDownloadOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * @return {@link DownloadRetryOptions}
     */
    public DownloadRetryOptions getRetryOptions() {
        return retryOptions;
    }

    /**
     * @param retryOptions {@link DownloadRetryOptions}
     * @return The updated options.
     */
    public ShareFileDownloadOptions setRetryOptions(DownloadRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }
}
