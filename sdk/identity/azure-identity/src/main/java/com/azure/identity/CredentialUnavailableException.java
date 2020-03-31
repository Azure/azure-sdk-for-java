package com.azure.identity;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpResponse;

public class CredentialUnavailableException extends ClientAuthenticationException {

    public CredentialUnavailableException(String message) {
        super(message, null);
    }
}
