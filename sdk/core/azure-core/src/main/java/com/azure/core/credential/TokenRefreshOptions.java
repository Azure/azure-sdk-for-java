// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import java.time.Duration;

/**
 * The options to configure the token refresh behavior.
 */
public class TokenRefreshOptions {
    private static final Duration DEFAULT_RETRY_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_OFFSET = Duration.ofMinutes(2);

    private Duration retryTimeout = DEFAULT_RETRY_TIMEOUT;
    private Duration offset = DEFAULT_OFFSET;

    /**
     * Returns a Duration value representing the amount of time to wait before retrying a token refresh. This is to
     * prevent sending too many requests to the authentication service.
     *
     * @return the duration value representing the amount of time to wait before retrying a token refresh
     */
    public Duration getRetryTimeout() {
        return retryTimeout;
    }

    /**
     * Specifies a Duration value representing the amount of time to wait before retrying a token refresh. This is to
     * prevent sending too many requests to the authentication service.
     *
     * @param retryTimeout the amount of time to wait before retrying a token refresh
     * @return the updated TokenRefreshOptions object
     */
    public TokenRefreshOptions setRetryTimeout(Duration retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    /**
     * Returns a Duration value representing the amount of time to subtract from the token expiry time, whereupon
     * attempts will be made to refresh the token. By default this will occur two minutes prior to the expiry of the
     * token.
     *
     * @return the duration value representing the amount of time to subtract from the token expiry time
     */
    public Duration getOffset() {
        return offset;
    }

    /**
     * Specifies the Duration value representing the amount of time to subtract from the token expiry time, whereupon
     * attempts will be made to refresh the token. By default this will occur two minutes prior to the expiry of the
     * token.
     *
     * @param offset the duration representing the amount of time to subtract from the token expiry time
     * @return the updated TokenRefreshOptions object
     */
    public TokenRefreshOptions setOffset(Duration offset) {
        this.offset = offset;
        return this;
    }
}
