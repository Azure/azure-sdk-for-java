// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.exceptions;

import com.azure.v2.core.credentials.TokenCredential;
import io.clientcore.core.credentials.oauth.AccessToken;

/**
 * The exception thrown when a {@link TokenCredential} did not attempt to authenticate and retrieve {@link AccessToken},
 * as its prerequisite information or state was not available.
 *
 * @see com.azure.v2.identity
 */
public class CredentialUnavailableException extends RuntimeException {

    /**
     * Initializes a new instance of the {@link CredentialUnavailableException} class.
     *
     * @param message The exception message.
     */
    public CredentialUnavailableException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the {@link CredentialUnavailableException} class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public CredentialUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
