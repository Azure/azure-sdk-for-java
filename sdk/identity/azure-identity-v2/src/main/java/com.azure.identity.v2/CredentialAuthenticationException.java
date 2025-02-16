// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;


import io.clientcore.core.credential.AccessToken;
import io.clientcore.core.credential.TokenCredential;

/**
 * The exception thrown when a {@link TokenCredential} did not attempt to authenticate and retrieve {@link AccessToken},
 * as its prerequisite information or state was not available.
 *
 * @see com.azure.identity.v2
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
