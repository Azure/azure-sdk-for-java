// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.exceptions.CredentialUnavailableException;

/**
 * <p>The Authentication Required Exception is thrown by {@link InteractiveBrowserCredential}
 * to indicate to the user that automatic authentication is disabled and authentication
 * needs to be initiated via {@link InteractiveBrowserCredential#authenticate()} or
 * {@link InteractiveBrowserCredential#authenticate()} APIs respectively before fetching an access token.</p>
 *
 * @see com.azure.v2.identity
 * @see InteractiveBrowserCredential
 */
public final class AuthenticationRequiredException extends CredentialUnavailableException {

    private final transient TokenRequestContext request;

    /**
     * Initializes a new instance of the {@link AuthenticationRequiredException} class.
     *
     * @param message The exception message.
     * @param request The details of the authentication request.
     */
    public AuthenticationRequiredException(String message, TokenRequestContext request) {
        super(message);
        this.request = request;
    }

    /**
     * Initializes a new instance of the {@link AuthenticationRequiredException} class.
     *
     * @param message The exception message.
     * @param request The details of the authentication request.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public AuthenticationRequiredException(String message, TokenRequestContext request, Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    /**
     * Get the details of the authentication request which resulted in the authentication failure.
     *
     * @return the token request context.
     */
    public TokenRequestContext getTokenRequestContext() {
        return request;
    }
}
