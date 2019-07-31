// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.DownloadAsyncResponse;

import java.util.Locale;

/**
 * {@code ReliableDownloadOptions} contains properties which help the {@code Flux} returned from
 * {@link DownloadAsyncResponse#body(ReliableDownloadOptions)} determine when to retry.
 */
public final class ReliableDownloadOptions {
    private static final String PARAMETER_NOT_IN_RANGE = "The value of the parameter '%s' should be between %s and %s.";

    /*
    We use "retry" here because by the time the user passes this type, the initial request, or try, has already been
    issued and returned. This is in contrast to the retry policy options, which includes the initial try in its count,
    thus the difference in verbiage.
     */
    private int maxRetryRequests = 0;

    /**
     * Specifies the maximum number of additional HTTP Get requests that will be made while reading the data from a
     * response body.
     *
     * @return the maximum number of retries to attempt before the request finally fails
     */
    public int maxRetryRequests() {
        return maxRetryRequests;
    }

    /**
     * Specifies the maximum number of additional HTTP Get requests that will be made while reading the data from a
     * response body.
     *
     * @param maxRetryRequests The number of retries to attempt before the request finally fails
     * @return the updated ReliableDownloadOptions object
     * @throws IllegalArgumentException If {@code maxRetryRequests} is less than 0
     */
    public ReliableDownloadOptions maxRetryRequests(int maxRetryRequests) {
        if (maxRetryRequests < 0) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, PARAMETER_NOT_IN_RANGE,
                "options.maxRetryRequests", 0, Integer.MAX_VALUE));
        }

        this.maxRetryRequests = maxRetryRequests;
        return this;
    }
}
