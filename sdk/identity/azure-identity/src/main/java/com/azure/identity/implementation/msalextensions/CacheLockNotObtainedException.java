// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

/**
 * Exception for when the {@link CacheLock} cannot be obtained when trying cacheLock.lock()
 * */
public class CacheLockNotObtainedException extends RuntimeException {

    /**
     * Initializes CacheLockNotObtainedException
     *
     * @param message Error message
     * */
    public CacheLockNotObtainedException(String message) {
        super(message);
    }
}
