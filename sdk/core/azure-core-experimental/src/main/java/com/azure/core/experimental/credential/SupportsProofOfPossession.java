// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.credential;

import com.azure.core.credential.AccessToken;
import reactor.core.publisher.Mono;

/**
 * An interface for credentials that support proof of possession (PoP) tokens.
 */
public interface SupportsProofOfPossession {
    /**
     * Asynchronously get a token for a given resource/audience.
     *
     * This method is called automatically by Azure SDK client libraries.
     * You may call this method directly, but you must also handle token
     * caching and token refreshing.
     *
     * @param request the details of the token request
     * @return a Publisher that emits a single access token
     */
    Mono<AccessToken> getToken(PopTokenRequestContext request);

    /**
     * Synchronously get a token for a given resource/audience.
     *
     * This method is called automatically by Azure SDK client libraries.
     * You may call this method directly, but you must also handle token
     * caching and token refreshing.
     *
     * @param request the details of the token request
     * @return The Access Token
     */
    default AccessToken getTokenSync(PopTokenRequestContext request) {
        return getToken(request).block();
    }
}
