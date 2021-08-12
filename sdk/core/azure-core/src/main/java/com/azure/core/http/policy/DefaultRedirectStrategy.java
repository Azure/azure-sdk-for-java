// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.logging.ClientLogger;

import java.net.HttpURLConnection;
import java.util.Objects;

/**
 * A default implementation of {@link RedirectStrategy} that uses the status code to determine if to redirect
 * between each retry attempt.
 */
public class DefaultRedirectStrategy implements RedirectStrategy {
    static final int PERMANENT_REDIRECT_STATUS_CODE = 308;
    // Based on Stamp specific redirects design doc
    static final int MAX_REDIRECT_RETRIES = 10;

    private final int maxRetries;
    private final int statusCode;

    /**
     * Creates an instance of {@link DefaultRedirectStrategy}.
     *
     * @param maxRetries The max number of retry attempts that can be made.
     * @param statusCode HTTP response status code
     */
    public DefaultRedirectStrategy(int statusCode, int maxRetries) {
        if (maxRetries < 0) {
            ClientLogger logger = new ClientLogger(DefaultRedirectStrategy.class);
            throw logger.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.statusCode = Objects.requireNonNull(statusCode, "'statusCode' cannot be null.");
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public boolean shouldAttemptRedirect() {
        if (maxRetries > MAX_REDIRECT_RETRIES) {
            logger.verbose("Max redirect retries limit reached: {}.", MAX_REDIRECT_RETRIES);
            return false;
        }
        return statusCode == HttpURLConnection.HTTP_MOVED_TEMP
            || statusCode == HttpURLConnection.HTTP_MOVED_PERM
            || statusCode == PERMANENT_REDIRECT_STATUS_CODE;
    }

}
