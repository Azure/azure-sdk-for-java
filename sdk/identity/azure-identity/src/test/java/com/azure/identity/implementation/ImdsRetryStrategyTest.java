// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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

    @Test
    public void testShouldRetry() {
        ImdsRetryStrategy imdsRetryStrategy = new ImdsRetryStrategy();

        HttpResponse httpResponse = new MockHttpResponse(null, 400);

        Assertions.assertFalse(imdsRetryStrategy.shouldRetry(httpResponse),
            "Imds Retry Strategy should not retry on 400 status response.");

        Assertions.assertTrue(imdsRetryStrategy.shouldRetry(new MockHttpResponse(null, 410)),
            "Imds Retry Strategy should retry on 410 status response.");

        Assertions.assertTrue(imdsRetryStrategy.shouldRetry(new MockHttpResponse(null, 429)),
            "Imds Retry Strategy should retry on 429 status response.");

        Assertions.assertTrue(imdsRetryStrategy.shouldRetry(new MockHttpResponse(null, 500)),
            "Imds Retry Strategy should retry on 500 status response.");

        Assertions.assertTrue(imdsRetryStrategy.shouldRetry(new MockHttpResponse(null, 599)),
            "Imds Retry Strategy should retry on 599 status response.");

        Assertions.assertTrue(imdsRetryStrategy.shouldRetry(new MockHttpResponse(null, 404)),
            "Imds Retry Strategy should retry on 404 status response.");

        MockHttpResponse responseStatus403Network = new MockHttpResponse(null, 403);
        responseStatus403Network.addHeader("ResponseMessage",
            "A socket operation was attempted to an unreachable network");
        Assertions.assertTrue(imdsRetryStrategy.shouldRetry(responseStatus403Network),
            "Imds Retry Strategy should retry on 403 status with unreachable network message.");

        MockHttpResponse responseStatus403Host = new MockHttpResponse(null, 403);
        responseStatus403Host.addHeader("ResponseMessage", "A socket operation was attempted to an unreachable host");
        Assertions.assertTrue(imdsRetryStrategy.shouldRetry(responseStatus403Host),
            "Imds Retry Strategy should retry on 403 status with unreachable host message.");

        MockHttpResponse responseStatus403NotSuccesful = new MockHttpResponse(null, 403);
        responseStatus403NotSuccesful.addHeader("ResponseMessage", "Access denied");
        Assertions.assertFalse(imdsRetryStrategy.shouldRetry(responseStatus403NotSuccesful),
            "Imds Retry Strategy should not retry on 403 status with other messages.");
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
