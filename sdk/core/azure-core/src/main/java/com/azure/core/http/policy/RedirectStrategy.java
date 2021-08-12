// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;

import java.net.HttpURLConnection;

/**
 * The interface for determining the retry strategy used in {@link RetryPolicy}.
 */
public interface RedirectStrategy {


    ClientLogger logger = new ClientLogger(RedirectStrategy.class);

    /**
     * Max number of retry attempts to be make.
     *
     * @return The max number of retry attempts.
     */
    int getMaxRetries();

    /**
     * Determines if the url should be redirected between each retry.
     *
     * @return {@code true} if the request should be redirected, {@code false}
     * otherwise
     */
    boolean shouldAttemptRedirect();

    /**
     * Determines if it's a valid retry scenario based on statusCode and tryCount.
     *
     * @param statusCode HTTP response status code
     * @param tryCount Redirect retries so far
     * @return True if statusCode corresponds to HTTP redirect response codes and redirect
     * retries is less than {@code MAX_REDIRECT_RETRIES}.
     */
    // default boolean shouldAttemptDirect(int statusCode, int tryCount) {
    //     if (tryCount >= MAX_REDIRECT_RETRIES) {
    //         logger.verbose("Max redirect retries limit reached: {}.", MAX_REDIRECT_RETRIES);
    //         return false;
    //     }
    //     return statusCode == HttpURLConnection.HTTP_MOVED_TEMP
    //         || statusCode == HttpURLConnection.HTTP_MOVED_PERM
    //         || statusCode == PERMANENT_REDIRECT_STATUS_CODE;
    // }
}
