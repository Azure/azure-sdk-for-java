// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions.cachePersister;

public class PlatformNotSupportedException extends Exception {

    public PlatformNotSupportedException(String message) {
        super(message);
    }

}
