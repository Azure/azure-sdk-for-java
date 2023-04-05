// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;

/**
 * The exception thrown when a {@link TokenCredential} did not attempt to authenticate and retrieve {@link AccessToken},
 * as its prerequisite information or state was not available.
 *
 * @see com.azure.identity
 */
public class CredentialUnavailableException extends ClientAuthenticationException {

    /**
     * Initializes a new instance of the {@link CredentialUnavailableException} class.
     *
     * @param message The exception message.
     */
    public CredentialUnavailableException(String message) {
        super(message, null);
    }

    /**
     * Initializes a new instance of the {@link CredentialUnavailableException} class.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public CredentialUnavailableException(String message, Throwable cause) {
        super(message, null, cause);
    }
}
