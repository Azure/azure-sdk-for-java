// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

/**
 * {@code ReliableDownloadOptions} contains properties which help the {@code Flowable} returned from
 * {@link DownloadResponse#body(ReliableDownloadOptions)} determine when to retry.
 */
public final class ReliableDownloadOptions {

    /*
    We use "retry" here because by the time the user passes this type, the initial request, or try, has already been
    issued and returned. This is in contrast to the retry policy options, which includes the initial try in its count,
    thus the difference in verbiage.
     */
    private int maxRetryRequests = 0;

    /**
     * Specifies the maximum number of additional HTTP Get requests that will be made while reading the data from a
     * response body.
     */
    public int maxRetryRequests() {
        return maxRetryRequests;
    }

    /**
     * Specifies the maximum number of additional HTTP Get requests that will be made while reading the data from a
     * response body.
     */
    public ReliableDownloadOptions withMaxRetryRequests(int maxRetryRequests) {
        Utility.assertInBounds("options.maxRetryRequests", maxRetryRequests, 0, Integer.MAX_VALUE);
        this.maxRetryRequests = maxRetryRequests;
        return this;
    }
}
