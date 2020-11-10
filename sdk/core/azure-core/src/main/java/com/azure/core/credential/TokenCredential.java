// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import reactor.core.publisher.Mono;

/**
 * The interface for credentials that can provide a token.
 */
@FunctionalInterface
public interface TokenCredential {
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
    Mono<AccessToken> getToken(TokenRequestContext request);
}
