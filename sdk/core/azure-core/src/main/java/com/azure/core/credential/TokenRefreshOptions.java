// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import java.time.Duration;

/**
 * The options to configure the token refresh behavior.
 */
public class TokenRefreshOptions {
    private static final Duration DEFAULT_REFRESH_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_REFRESH_OFFSET = Duration.ofMinutes(2);

    private Duration tokenRefreshTimeout = DEFAULT_REFRESH_TIMEOUT;
    private Duration tokenRefreshOffset = DEFAULT_REFRESH_OFFSET;

    /**
     * Returns a Duration value representing the amount of time to wait between token refreshes. This is to prevent
     * sending too many requests to the authentication service.
     *
     * @return the duration value representing the amount of time to wait between token refreshes
     */
    public Duration getTokenRefreshTimeout() {
        return tokenRefreshTimeout;
    }

    /**
     * Specifies a Duration value representing the amount of time to wait between token refreshes. This is to prevent
     * sending too many requests to the authentication service.
     *
     * @param tokenRefreshTimeout the duration value representing the amount of time to wait between token refreshes
     * @return TokenRefreshOptions
     */
    public TokenRefreshOptions setTokenRefreshTimeout(Duration tokenRefreshTimeout) {
        this.tokenRefreshTimeout = tokenRefreshTimeout;
        return this;
    }

    /**
     * Returns a Duration value representing the amount of time to subtract from the token expiry time, whereupon
     * attempts will be made to refresh the token. By default this will occur two minutes prior to the expiry of the
     * token.
     *
     * @return the duration value representing the amount of time to subtract from the token expiry time
     */
    public Duration getTokenRefreshOffset() {
        return tokenRefreshOffset;
    }

    /**
     * Specifies the Duration value representing the amount of time to subtract from the token expiry time, whereupon
     * attempts will be made to refresh the token. By default this will occur two minutes prior to the expiry of the
     * token.
     *
     * @param tokenRefreshOffset the duration representing the amount of time to subtract from the token expiry time
     * @return TokenRefreshOptions
     */
    public TokenRefreshOptions setTokenRefreshOffset(Duration tokenRefreshOffset) {
        this.tokenRefreshOffset = tokenRefreshOffset;
        return this;
    }
}
