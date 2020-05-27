// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * The exception thrown to indicate that interactive authentication is required.
 */
public class AuthenticationRequiredException extends CredentialUnavailableException {

    /**
     * Initializes a new instance of the {@link AuthenticationRequiredException} class.
     *
     * @param message The exception message.
     */
    public AuthenticationRequiredException(String message) {
        super(message, null);
    }

    /**
     * Initializes a new instance of the {@link AuthenticationRequiredException} class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public AuthenticationRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
