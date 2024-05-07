package com.azure.cosmos.cris.querystuckrepro;

public abstract class SDKException extends Exception {
    protected SDKException(String message) {
        super(message);
    }

    protected SDKException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
