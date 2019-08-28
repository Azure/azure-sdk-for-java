package com.azure.identity.implementation.msal_extensions;

public class CrossPlatLockNotObtainedException extends Exception {

    public CrossPlatLockNotObtainedException(String message) {
        super(message);
    }
}
