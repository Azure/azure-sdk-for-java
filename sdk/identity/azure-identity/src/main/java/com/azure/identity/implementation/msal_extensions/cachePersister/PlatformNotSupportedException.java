// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions.cachePersister;

/**
 * Exception for when the current OS is not supported by {@link CachePersister}
 * so the OS specific DPAPI cannot be used to encrypt the token cache.
 * */
public class PlatformNotSupportedException extends Exception {

    /**
     * Initializes PlatformNotSupportedException
     *
     * @param message Error message
     * */
    public PlatformNotSupportedException(String message) {
        super(message);
    }

}
