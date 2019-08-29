package com.azure.identity.implementation.msal_extensions;

public class CacheLockNotObtainedException extends Exception {

    public CacheLockNotObtainedException(String message) {
        super(message);
    }
}
