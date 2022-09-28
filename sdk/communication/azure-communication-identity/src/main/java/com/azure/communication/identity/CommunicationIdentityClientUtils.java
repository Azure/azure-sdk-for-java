// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

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

    static final String TOKEN_EXPIRATION_OVERFLOW_MESSAGE = "The tokenExpiresIn argument is out of permitted bounds [1,24] hours. Please refer to the documentation and set the value accordingly.";
    static final double TOKEN_EXPIRATION_ALLOWED_DEVIATION = 0.05;
    static final int MAX_TOKEN_EXPIRATION_IN_MINUTES = 1440;

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

    static TokenExpirationDeviationData tokenExpirationWithinAllowedDeviation(Duration expectedTokenExpiration, OffsetDateTime tokenExpiresIn) {

        Duration expectedExpiration = expectedTokenExpiration == null ? Duration.ofDays(1) : expectedTokenExpiration;

        OffsetDateTime utcDateTimeNow = OffsetDateTime.now(Clock.systemUTC());
        long tokenSeconds = ChronoUnit.SECONDS.between(utcDateTimeNow, tokenExpiresIn);
        long expectedTime = expectedExpiration.getSeconds();
        long timeDiff = Math.abs(expectedTime - tokenSeconds);
        double allowedTimeDiff = expectedTime * TOKEN_EXPIRATION_ALLOWED_DEVIATION;
        return new TokenExpirationDeviationData(tokenSeconds, timeDiff < allowedTimeDiff);
    }

    static String getTokenExpirationOutsideAllowedDeviationErrorMessage(Duration tokenExpiresIn, double actualExpiration) {

        int tokenExpiresInMinutes = tokenExpiresIn == null ? MAX_TOKEN_EXPIRATION_IN_MINUTES : (int) (tokenExpiresIn.getSeconds() / 60);
        double actualExpirationInMinutes = actualExpiration / 60;

        return String.format("Token expiration is outside of allowed %d%% deviation. Expected minutes: %d, actual minutes: %s",
            (int) (TOKEN_EXPIRATION_ALLOWED_DEVIATION * 100),
            tokenExpiresInMinutes,
            (double) Math.round(actualExpirationInMinutes * 100) / 100);
    }

    private static int getTokenExpirationInMinutes(Duration tokenExpiresIn, ClientLogger logger) {
        try {
            return Math.toIntExact(tokenExpiresIn.toMinutes());
        } catch (ArithmeticException ex) {
            IllegalArgumentException expiresAfterOverflowEx = new IllegalArgumentException(TOKEN_EXPIRATION_OVERFLOW_MESSAGE, ex);
            throw logger.logExceptionAsError(expiresAfterOverflowEx);
        }
    }

    static class TokenExpirationDeviationData {

        final long actualExpirationInSeconds;
        final boolean isWithinAllowedDeviation;

        TokenExpirationDeviationData(long actualExpirationInSeconds, boolean isWithinAllowedDeviation) {
            this.actualExpirationInSeconds = actualExpirationInSeconds;
            this.isWithinAllowedDeviation = isWithinAllowedDeviation;
        }

        long getActualExpirationInSeconds() {
            return this.actualExpirationInSeconds;
        }

        boolean getIsWithinAllowedDeviation() {
            return this.isWithinAllowedDeviation;
        }
    }
}
