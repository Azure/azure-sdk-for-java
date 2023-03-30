// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;

/**
 * <p>The Authentication Required Exception is thrown by {@link InteractiveBrowserCredential} and
 * {@link DeviceCodeCredential} to indicate to the user that automatic authentication is disabled and authentication
 * needs to be initiated via {@link InteractiveBrowserCredential#authenticate()} or
 * {@link DeviceCodeCredential#authenticate()} APIs respectively before fetching an access token.</p>
 *
 * @see com.azure.identity
 * @see InteractiveBrowserCredential
 * @see DeviceCodeCredential
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
