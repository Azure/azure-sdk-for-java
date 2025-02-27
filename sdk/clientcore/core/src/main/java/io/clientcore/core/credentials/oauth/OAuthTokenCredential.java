// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credentials.oauth;

/**
 * <p>
 * OAuth Token Credential interface serves as a fundamental component for managing and providing access tokens.
 * </p>
 *
 * <p>
 * The {@link OAuthTokenCredential} interface, offers an API to retrieve an access token that can be used for
 * bearer token authentication. The scopes parameter specified as part of {@link OAuthTokenRequestContext} represents
 * the resources or permissions required for the token.
 * </p>
 *
 * <p>
 * By utilizing the OAuth Token Provider interface, you can abstract the authentication logic away from your
 * application code. This allows for flexibility in choosing authentication mechanisms and simplifies the management
 * of access tokens, including token caching and refreshing.
 * </p>
 *
 * @see io.clientcore.core.credentials
 */
@FunctionalInterface
public interface OAuthTokenCredential {

    /**
     * Get a token for a given resource/audience.
     *
     * @param request the details of the token request
     * @return The Access Token
     */
    AccessToken getToken(OAuthTokenRequestContext request);
}
