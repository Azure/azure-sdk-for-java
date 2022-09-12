// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.util.logging.ClientLogger;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class CommunicationIdentityClientUtils {

    static final String TOKEN_EXPIRATION_OVERFLOW_MESSAGE = "The tokenExpiresAfter argument is out of permitted bounds. Please refer to the documentation and set the value accordingly.";

    static CommunicationIdentityCreateRequest createCommunicationIdentityCreateRequest(
        Iterable<CommunicationTokenScope> scopes,
        Duration tokenExpiresAfter,
        ClientLogger logger) {

        final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());

        CommunicationIdentityCreateRequest createRequest = new CommunicationIdentityCreateRequest();
        createRequest.setCreateTokenWithScopes(scopesInput);

        if (tokenExpiresAfter != null) {
            int expiresInMinutes = getTokenExpirationInMinutes(tokenExpiresAfter, logger);
            createRequest.setExpiresInMinutes(expiresInMinutes);
        }

        return createRequest;
    }

    static CommunicationIdentityAccessTokenRequest createCommunicationIdentityAccessTokenRequest(
        Iterable<CommunicationTokenScope> scopes,
        Duration tokenExpiresAfter,
        ClientLogger logger) {

        final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());

        CommunicationIdentityAccessTokenRequest tokenRequest = new CommunicationIdentityAccessTokenRequest();
        tokenRequest.setScopes(scopesInput);

        if (tokenExpiresAfter != null) {
            int expiresInMinutes = getTokenExpirationInMinutes(tokenExpiresAfter, logger);
            tokenRequest.setExpiresInMinutes(expiresInMinutes);
        }

        return tokenRequest;
    }

    static boolean IsTokenExpirationValid(Duration expectedTokenExpiration, OffsetDateTime tokenExpiresAfter) {

        Duration expectedExpiration = expectedTokenExpiration == null ? Duration.ofDays(1) : expectedTokenExpiration;

        OffsetDateTime utcDateTimeNow = OffsetDateTime.now(Clock.systemUTC());
        long tokenSeconds = ChronoUnit.SECONDS.between(utcDateTimeNow, tokenExpiresAfter);
        long expectedTime = expectedExpiration.getSeconds();
        long timeDiff = Math.abs(expectedTime - tokenSeconds);
        double allowedTimeDiff = expectedTime * 0.05;
        return timeDiff < allowedTimeDiff;
    }

    private static int getTokenExpirationInMinutes(Duration tokenExpiresAfter, ClientLogger logger) {
        try {
            return Math.toIntExact(tokenExpiresAfter.toMinutes());
        } catch (ArithmeticException ex) {
            IllegalArgumentException expiresAfterOverflowEx = new IllegalArgumentException(TOKEN_EXPIRATION_OVERFLOW_MESSAGE, ex);
            throw logger.logExceptionAsError(expiresAfterOverflowEx);
        }
    }
}
