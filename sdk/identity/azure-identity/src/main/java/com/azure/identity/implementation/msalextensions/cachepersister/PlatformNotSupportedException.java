// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions.cachepersister;

/**
 * Exception for when the current OS is not supported by {@link CachePersister}
 * so the OS specific DPAPI cannot be used to encrypt the token cache.
 * */
public class PlatformNotSupportedException extends RuntimeException {

    /**
     * Initializes PlatformNotSupportedException
     *
     * @param message Error message
     * */
    public PlatformNotSupportedException(String message) {
        super(message);
    }

}
