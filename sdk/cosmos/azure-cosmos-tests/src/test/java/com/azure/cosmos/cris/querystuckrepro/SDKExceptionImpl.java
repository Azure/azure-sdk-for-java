package com.azure.cosmos.cris.querystuckrepro;

public class SDKExceptionImpl extends SDKException {

    public SDKExceptionImpl(String message) {
        super(message);
    }

    public SDKExceptionImpl(String message, Throwable throwable) {
        super(message, throwable);
    }
}
