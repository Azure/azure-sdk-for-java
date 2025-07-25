// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class ImdsRetryStrategyTest {

    @Test
    public void testIMDSRetry() {
        ImdsRetryStrategy imdsRetryStrategy = new ImdsRetryStrategy();

        int retry = 0;
        Queue<Long> expectedEntries = new LinkedList<>();
        expectedEntries.addAll(Arrays.asList(800L, 1600L, 3200L, 6400L, 12800L));

        while (retry < imdsRetryStrategy.getMaxRetries()) {
            long timeout = (imdsRetryStrategy.calculateRetryDelay(retry).toMillis());
            if (expectedEntries.contains(timeout)) {
                expectedEntries.remove(timeout);
            } else {
                Assertions.fail("Unexpected timeout: " + timeout);
            }
            retry++;
        }
    }

    @ParameterizedTest
    @MethodSource("shouldRetryInDifferentScenarios")
    public void testShouldRetry(String headerValue, int statusCode, boolean expectedRetry, String description) {
        ImdsRetryStrategy imdsRetryStrategy = new ImdsRetryStrategy();

        MockHttpResponse httpResponse = new MockHttpResponse(null, statusCode);
        if (headerValue != null) {
            httpResponse.addHeader("ResponseMessage", headerValue);
        }

        Assertions.assertEquals(expectedRetry, imdsRetryStrategy.shouldRetry(httpResponse), description);
    }

    private static Stream<Arguments> shouldRetryInDifferentScenarios() {
        return Stream.of(Arguments.of(null, 400, false, "Imds Retry Strategy should not retry on 400 status response"),
            Arguments.of(null, 410, true, "Imds Retry Strategy should retry on 410 status response"),
            Arguments.of(null, 429, true, "Imds Retry Strategy should retry on 429 status response"),
            Arguments.of(null, 500, true, "Imds Retry Strategy should retry on 500 status reponse"),
            Arguments.of(null, 599, true, "Imds Retry Strategy should retry on 599 status response"),
            Arguments.of(null, 404, true, "Imds Retry Strategy should retry on 404 status response"),
            Arguments.of("A socket operation was attempted to an unreachable", 403, true,
                "Imds Retry Strategy should retry on 403 with unreachable message"),
            Arguments.of("Access denied", 403, false,
                "Imds Retry Strategy should not retry on 403 with Access Denied message"),
            Arguments.of(null, 403, false,
                "Imds Retry Strategy should not retry on 403 with no ResponseMessage header"));
    }

    @Test
    public void testShouldRetryOnException() {
        ImdsRetryStrategy imdsRetryStrategy = new ImdsRetryStrategy();

        Assertions.assertFalse(imdsRetryStrategy.shouldRetryException(new Exception("Test Error")),
            "Imds Retry Strategy should not retry on general exception.");

        Assertions.assertTrue(imdsRetryStrategy.shouldRetryException(new IOException("Test Error")),
            "Imds Retry Strategy should retry on IOException.");
    }
}
