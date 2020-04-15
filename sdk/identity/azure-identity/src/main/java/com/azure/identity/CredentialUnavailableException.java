package com.azure.identity;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpResponse;

public class CredentialUnavailableException extends ClientAuthenticationException {

    public CredentialUnavailableException(String message, HttpResponse httpResponse) {
        super(message, httpResponse);
    }

    public CredentialUnavailableException(String message, HttpResponse response, Object value) {
        super(message, response, value);
    }

    public CredentialUnavailableException(String message, HttpResponse response, Throwable cause) {
        super(message, response, cause);
    }
}
