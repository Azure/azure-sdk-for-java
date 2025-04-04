// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.exceptions;

import com.azure.v2.core.credentials.TokenCredential;
import io.clientcore.core.credentials.oauth.AccessToken;

/**
 * The exception thrown when a {@link TokenCredential} attempted to authenticate and retrieve {@link AccessToken},
 * but failed to do so.
 *
 * <p>
 *     This exception breaks the chained authentication flow of chained credentials.
 * </p>
 *
 * @see com.azure.v2.identity
 */
public class CredentialAuthenticationException extends RuntimeException {

    /**
     * Initializes a new instance of the {@link CredentialAuthenticationException} class.
     *
     * @param message The exception message.
     */
    public CredentialAuthenticationException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the {@link CredentialAuthenticationException} class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public CredentialAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
