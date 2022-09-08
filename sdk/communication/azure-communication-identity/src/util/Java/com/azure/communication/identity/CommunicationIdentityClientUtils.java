// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.List;

final class CommunicationIdentityClientUtils {

    static final String TOKEN_EXPIRATION_OVERFLOW_MESSAGE = "The tokenExpiresAfter argument is out of permitted bounds. Please refer to the documentation and set the value accordingly.";

    CommunicationIdentityCreateRequest createCommunicationIdentityCreateRequest(
        List<CommunicationTokenScope> scopesInput,
        Duration tokenExpiresAfter,
        ClientLogger logger) {

        CommunicationIdentityCreateRequest createRequest = new CommunicationIdentityCreateRequest();
        createRequest.setCreateTokenWithScopes(scopesInput);

        if (tokenExpiresAfter != null) {
            int expiresInMinutes = getTokenExpirationInMinutes(tokenExpiresAfter, logger);
            createRequest.setExpiresInMinutes(expiresInMinutes);
        }

        return createRequest;
    }

    CommunicationIdentityAccessTokenRequest createCommunicationIdentityAccessTokenRequest(
        List<CommunicationTokenScope> scopesInput,
        Duration tokenExpiresAfter,
        ClientLogger logger) {

        CommunicationIdentityAccessTokenRequest tokenRequest = new CommunicationIdentityAccessTokenRequest();
        tokenRequest.setScopes(scopesInput);

        if (tokenExpiresAfter != null) {
            int expiresInMinutes = getTokenExpirationInMinutes(tokenExpiresAfter, logger);
            tokenRequest.setExpiresInMinutes(expiresInMinutes);
        }

        return tokenRequest;
    }

    private int getTokenExpirationInMinutes(Duration tokenExpiresAfter, ClientLogger logger) {
        try {
            return Math.toIntExact(tokenExpiresAfter.toMinutes());
        } catch (ArithmeticException ex) {
            IllegalArgumentException expiresAfterOverflowEx = new IllegalArgumentException(TOKEN_EXPIRATION_OVERFLOW_MESSAGE, ex);
            throw logger.logExceptionAsError(expiresAfterOverflowEx);
        }
    }
}
