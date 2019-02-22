package com.microsoft.azure.msiAuthTokenProvider;

public class AzureMSICredentialException extends Exception{
    AzureMSICredentialException(String message) {
        super(message);
    }

    AzureMSICredentialException(String message, Throwable cause) {
        super(message, cause);
    }

    AzureMSICredentialException(Throwable cause) {
        super(cause);
    }
}
