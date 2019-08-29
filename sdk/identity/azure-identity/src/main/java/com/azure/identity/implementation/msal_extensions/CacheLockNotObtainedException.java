// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions;

/**
 * Exception for when the {@link CacheLock} cannot be obtained when trying cacheLock.lock()
 * */
public class CacheLockNotObtainedException extends Exception {

    /**
     * Initializes CacheLockNotObtainedException
     *
     * @param message Error message
     * */
    public CacheLockNotObtainedException(String message) {
        super(message);
    }
}
