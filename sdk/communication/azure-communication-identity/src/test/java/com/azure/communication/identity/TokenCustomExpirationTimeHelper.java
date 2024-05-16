// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.core.test.TestMode;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.implementation.TestingHelpers.getTestMode;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenCustomExpirationTimeHelper {

    private static final double TOKEN_EXPIRATION_ALLOWED_DEVIATION = 0.05;
    private static final int MAX_TOKEN_EXPIRATION_IN_MINUTES = 1440;

    static Stream<Arguments> getValidExpirationTimes() {
        List<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(Arguments.of("MinValidCustomExpiration", Duration.ofHours(1)));
        argumentsList.add(Arguments.of("MaxValidCustomExpiration", Duration.ofHours(24)));
        argumentsList.add(Arguments.of("NullExpiration", null));
        return argumentsList.stream();
    }

    static Stream<Arguments> getInvalidExpirationTimes() {
        List<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(Arguments.of("MaxInvalidCustomExpiration", Duration.ofMinutes(59)));
        argumentsList.add(Arguments.of("MinInvalidCustomExpiration", Duration.ofMinutes(1441)));

        return argumentsList.stream();
    }

    static void assertTokenExpirationWithinAllowedDeviation(Duration expectedTokenExpiration, OffsetDateTime tokenExpiresIn) {
        if (getTestMode() != TestMode.PLAYBACK) {
            Duration expectedExpiration = expectedTokenExpiration == null ? Duration.ofDays(1) : expectedTokenExpiration;
            OffsetDateTime utcDateTimeNow = OffsetDateTime.now(Clock.systemUTC());
            long tokenSeconds = ChronoUnit.SECONDS.between(utcDateTimeNow, tokenExpiresIn);
            long expectedTime = expectedExpiration.getSeconds();
            long timeDiff = Math.abs(expectedTime - tokenSeconds);
            double allowedTimeDiff = expectedTime * TOKEN_EXPIRATION_ALLOWED_DEVIATION;
            assertTrue(timeDiff < allowedTimeDiff, getTokenExpirationOutsideAllowedDeviationErrorMessage(expectedTokenExpiration, tokenSeconds));
        }
    }

    private static String getTokenExpirationOutsideAllowedDeviationErrorMessage(Duration tokenExpiresIn, long actualExpiration) {
        long tokenExpiresInMinutes = tokenExpiresIn == null ? MAX_TOKEN_EXPIRATION_IN_MINUTES : tokenExpiresIn.getSeconds() / 60;
        double actualExpirationInMinutes = actualExpiration / 60;
        return String.format("Token expiration is outside of allowed %d%% deviation. Expected minutes: %d, actual minutes: %s",
            (int) (TOKEN_EXPIRATION_ALLOWED_DEVIATION * 100),
            tokenExpiresInMinutes,
            Math.round(actualExpirationInMinutes * 100) / 100);
    }

}
