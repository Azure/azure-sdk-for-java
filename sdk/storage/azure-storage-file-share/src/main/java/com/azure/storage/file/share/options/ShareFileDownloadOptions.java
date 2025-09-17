// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.DownloadRetryOptions;
import com.azure.storage.file.share.models.ShareFileRange;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.common.implementation.contentvalidation.DownloadContentValidationOptions;

/**
 * Extended options that may be passed when downloading a File.
 */
@Fluent
public final class ShareFileDownloadOptions {
    private ShareFileRange range;
    private Boolean rangeContentMd5Requested;
    private ShareRequestConditions requestConditions;
    private DownloadRetryOptions retryOptions;
    private DownloadContentValidationOptions contentValidationOptions;

    /**
     * Creates a new instance of {@link ShareFileDownloadOptions}.
     */
    public ShareFileDownloadOptions() {
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
    public ShareFileDownloadOptions setRange(ShareFileRange range) {
        this.range = range;
        return this;
    }

    /**
     * Gets whether the service returns the MD5 hash for the range.
     *
     * @return Whether the service returns the MD5 hash for the range.
     */
    public Boolean isRangeContentMd5Requested() {
        return rangeContentMd5Requested;
    }

    /**
     * Sets whether the service returns the MD5 hash for the range.
     *
     * @param rangeContentMd5Requested Whether the service returns the MD5 hash for the range.
     * @return The updated options.
     */
    public ShareFileDownloadOptions setRangeContentMd5Requested(Boolean rangeContentMd5Requested) {
        this.rangeContentMd5Requested = rangeContentMd5Requested;
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
     * Sets the {@link ShareRequestConditions} for the file.
     *
     * @param requestConditions {@link ShareRequestConditions} for the file.
     * @return The updated options.
     */
    public ShareFileDownloadOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Gets the {@link DownloadRetryOptions}.
     *
     * @return {@link DownloadRetryOptions}
     */
    public DownloadRetryOptions getRetryOptions() {
        return retryOptions;
    }

    /**
     * Sets the {@link DownloadRetryOptions}.
     *
     * @param retryOptions {@link DownloadRetryOptions}
     * @return The updated options.
     */
    public ShareFileDownloadOptions setRetryOptions(DownloadRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Gets the {@link DownloadContentValidationOptions}.
     *
     * @return {@link DownloadContentValidationOptions}
     */
    public DownloadContentValidationOptions getContentValidationOptions() {
        return contentValidationOptions;
    }

    /**
     * Sets the {@link DownloadContentValidationOptions}.
     *
     * @param contentValidationOptions {@link DownloadContentValidationOptions}
     * @return The updated options.
     */
    public ShareFileDownloadOptions setContentValidationOptions(DownloadContentValidationOptions contentValidationOptions) {
        this.contentValidationOptions = contentValidationOptions;
        return this;
    }
}
