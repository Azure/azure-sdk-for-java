// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The interface for credentials that can provide a token.
 */
@FunctionalInterface
public interface TokenCredential {
    /**
     * The default duration before the actual token expiry to refresh the token. The value is 2 minutes.
     */
    Duration DEFAULT_TOKEN_REFRESH_OFFSET = Duration.ofMinutes(2);

    /**
     * Asynchronously get a token for a given resource/audience.
     * @param request the details of the token request
     * @return a Publisher that emits a single access token
     */
    Mono<AccessToken> getToken(TokenRequestContext request);

    /**
     * The duration before the actual token expiry to refresh the token. Default is 2 minutes.
     */
    default Duration getTokenRefreshOffset() {
        return DEFAULT_TOKEN_REFRESH_OFFSET;
    }
}
