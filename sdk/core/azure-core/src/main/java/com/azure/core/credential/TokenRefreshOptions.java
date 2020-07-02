// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import java.time.Duration;

/**
 * The options to configure the token refresh behavior.
 */
public class TokenRefreshOptions {
    private static final Duration DEFAULT_OFFSET = Duration.ofMinutes(2);

    /**
     * Returns a Duration value representing the amount of time to subtract from the token expiry time, whereupon
     * attempts will be made to refresh the token. By default this will occur two minutes prior to the expiry of the
     * token.
     *
     * This is used in {@link SimpleTokenCache} and {@link com.azure.core.http.policy.BearerTokenAuthenticationPolicy}
     * to proactively retrieve a more up-to-date token before the cached token gets too close to its expiry. You can
     * configure this from the {@code .tokenRefreshOffset(Duration)} method from any credential builder in the
     * azure-identity library.
     *
     * Extending this offset is recommended if it takes &gt; 2 minutes to reach the service (application is running on
     * high load), or you would like to simply keep a more up-to-date token in the cache (more robust against token
     * refresh API down times). The user is responsible for specifying a valid offset.
     *
     * When a proactive token refresh fails but the previously cached token is still valid,
     * {@link com.azure.core.http.policy.BearerTokenAuthenticationPolicy} will NOT fail but return the previous valid
     * token. Another proactive refresh will be attempted in 30 seconds.
     *
     * @return the duration value representing the amount of time to subtract from the token expiry time
     */
    public Duration getOffset() {
        return DEFAULT_OFFSET;
    }
}
