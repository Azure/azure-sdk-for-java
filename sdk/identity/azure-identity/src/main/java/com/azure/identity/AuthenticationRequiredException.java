// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;

/**
 * The exception thrown to indicate that interactive authentication is required.
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
