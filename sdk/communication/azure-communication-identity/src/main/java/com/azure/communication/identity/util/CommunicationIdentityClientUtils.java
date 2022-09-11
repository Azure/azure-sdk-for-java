// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity.util;

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

/**
 * Utility class with shared functionality for {@link CommunicationIdentityClient}, {@link CommunicationIdentityAsyncClient}
 * and their test classes.
 */
public final class CommunicationIdentityClientUtils {

    /**
     * Error message for the case when expiresAfter argument overflows its allowed value.
     */
    public static final String TOKEN_EXPIRATION_OVERFLOW_MESSAGE = "The tokenExpiresAfter argument is out of permitted bounds. Please refer to the documentation and set the value accordingly.";

    /**
     *
     * @param scopes The list of scopes for the token.
     * @param tokenExpiresAfter Custom validity period of the Communication Identity access token within [1,24]
     * hours range. If not provided, the default value of 24 hours will be used.
     * @param logger {@link ClientLogger} for exception logging.
     * @return {@link CommunicationIdentityCreateRequest} request to create Communication Identity.
     */
    public static CommunicationIdentityCreateRequest createCommunicationIdentityCreateRequest(
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

    /**
     *
     * @param scopes The list of scopes for the token.
     * @param tokenExpiresAfter Custom validity period of the Communication Identity access token within [1,24]
     * hours range. If not provided, the default value of 24 hours will be used.
     * @param logger {@link ClientLogger} for exception logging.
     * @return {@link CommunicationIdentityAccessTokenRequest} request to create Communication Identity access token.
     */
    public static CommunicationIdentityAccessTokenRequest createCommunicationIdentityAccessTokenRequest(
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

    /**
     * @param expectedTokenExpiration Expected token expiration.
     * @param tokenExpiresAfter Actual token expiration.
     * @return Whether actual token expiration corresponds with expected token expiration.
     */
    public static boolean IsTokenExpirationValid(Duration expectedTokenExpiration, OffsetDateTime tokenExpiresAfter) {

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
