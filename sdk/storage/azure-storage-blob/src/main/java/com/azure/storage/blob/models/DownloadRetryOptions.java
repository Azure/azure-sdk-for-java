// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.util.Locale;

/**
 * This class contains the configuration options used to download from the blob service using smart retries, starting
 * from a checkpoint, rather than starting over from the beginning.
 */
@Fluent
public final class DownloadRetryOptions {
    private static final String PARAMETER_NOT_IN_RANGE = "The value of the parameter '%s' should be between %s and %s.";

    private final ClientLogger logger = new ClientLogger(DownloadRetryOptions.class);

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
    public int getMaxRetryRequests() {
        return maxRetryRequests;
    }

    /**
     * Specifies the maximum number of additional HTTP Get requests that will be made while reading the data from a
     * response body.
     *
     * @param maxRetryRequests The number of retries to attempt before the request finally fails
     * @return the updated DownloadRetryOptions object
     * @throws IllegalArgumentException If {@code maxRetryRequests} is less than 0
     */
    public DownloadRetryOptions setMaxRetryRequests(int maxRetryRequests) {
        if (maxRetryRequests < 0) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(String.format(Locale.ROOT, PARAMETER_NOT_IN_RANGE,
                    "options.maxRetryRequests", 0, Integer.MAX_VALUE)));
        }

        this.maxRetryRequests = maxRetryRequests;
        return this;
    }
}
