// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.credential;

/**
 * <p>
 * Token Credential interface serves as a fundamental component for managing and providing access tokens.
 * </p>
 *
 * <p>
 * The {@link TokenCredential} interface, offers {@link TokenCredential#getToken(TokenRequestContext)} methods.
 * These methods are responsible for retrieving an access token that can be used for bearer token authentication.
 * The scopes parameter specified as part of {@link TokenRequestContext} represents the resources
 * or permissions required for the token.
 * </p>
 *
 * <p>
 * By utilizing the Token Credential interface, you can abstract the authentication logic away from your
 * application code. This allows for flexibility in choosing authentication mechanisms and simplifies the management
 * of access tokens, including token caching and refreshing.
 * </p>
 *
 * @see com.azure.core.credential
 */
@FunctionalInterface
public interface TokenCredential {

    /**
     * Synchronously get a token for a given resource/audience.
     * <p>
     * This method is called automatically by Azure SDK client libraries.
     * <p>
     * You may call this method directly, but you must also handle token caching and token refreshing.
     *
     * @param request the details of the token request
     * @return The Access Token
     */
    AccessToken getToken(TokenRequestContext request);
}
