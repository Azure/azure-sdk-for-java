// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class CommunicationIdentityClientUtils {

    static final String TOKEN_EXPIRATION_OVERFLOW_MESSAGE = "The tokenExpiresIn argument is out of permitted bounds [1,24] hours. Please refer to the documentation and set the value accordingly.";

    static CommunicationIdentityCreateRequest createCommunicationIdentityCreateRequest(
        Iterable<CommunicationTokenScope> scopes,
        Duration tokenExpiresIn,
        ClientLogger logger) {

        final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());

        CommunicationIdentityCreateRequest createRequest = new CommunicationIdentityCreateRequest();
        createRequest.setCreateTokenWithScopes(scopesInput);

        if (tokenExpiresIn != null) {
            int expiresInMinutes = getTokenExpirationInMinutes(tokenExpiresIn, logger);
            createRequest.setExpiresInMinutes(expiresInMinutes);
        }

        return createRequest;
    }

    static CommunicationIdentityAccessTokenRequest createCommunicationIdentityAccessTokenRequest(
        Iterable<CommunicationTokenScope> scopes,
        Duration tokenExpiresIn,
        ClientLogger logger) {

        final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());

        CommunicationIdentityAccessTokenRequest tokenRequest = new CommunicationIdentityAccessTokenRequest();
        tokenRequest.setScopes(scopesInput);

        if (tokenExpiresIn != null) {
            int expiresInMinutes = getTokenExpirationInMinutes(tokenExpiresIn, logger);
            tokenRequest.setExpiresInMinutes(expiresInMinutes);
        }

        return tokenRequest;
    }

    private static int getTokenExpirationInMinutes(Duration tokenExpiresIn, ClientLogger logger) {
        try {
            return Math.toIntExact(tokenExpiresIn.toMinutes());
        } catch (ArithmeticException ex) {
            IllegalArgumentException expiresAfterOverflowEx = new IllegalArgumentException(TOKEN_EXPIRATION_OVERFLOW_MESSAGE, ex);
            throw logger.logExceptionAsError(expiresAfterOverflowEx);
        }
    }
}
