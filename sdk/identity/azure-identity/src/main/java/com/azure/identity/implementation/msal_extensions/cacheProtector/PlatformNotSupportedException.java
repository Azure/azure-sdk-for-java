package com.azure.identity.implementation.msal_extensions.cacheProtector;

public class PlatformNotSupportedException extends Exception {

    public PlatformNotSupportedException(String message) {
        super(message);
    }

}
