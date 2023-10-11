// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.credential;

/**
 * The interface for credentials that can provide a token.
 */
@FunctionalInterface
public interface TokenCredential {
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
    AccessToken getTokenSync(TokenRequestContext request);

}
