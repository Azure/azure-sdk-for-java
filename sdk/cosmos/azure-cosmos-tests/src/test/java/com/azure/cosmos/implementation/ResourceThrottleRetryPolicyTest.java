// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ResourceThrottleRetryPolicyTest {

    @DataProvider(name = "requestRateTooLargeExceptionArgProvider")
    public Object[][] requestRateTooLargeExceptionArgProvider() {
        return new Object[][]{
            // SubStatusCode, retryAfter header, shouldRetry, shouldRetry delay
            { 3200, 500, true, 500 },
            { 3200, null, true, 0 },
            { 3089, null, true, 100}
        };
    }

    @Test(groups = "unit", dataProvider = "requestRateTooLargeExceptionArgProvider")
    public void requestTooLargeException(
        int subStatusCode,
        Integer retryAfterInMillis,
        boolean shouldRetry,
        int shouldRetryDelay) {
        ResourceThrottleRetryPolicy retryPolicy =
            new ResourceThrottleRetryPolicy(
                1,
                Duration.ofSeconds(2),
                new RetryContext(),
                false);

        Map<String, String> responseHeaders = new ConcurrentHashMap<>();
        if (retryAfterInMillis != null) {
            responseHeaders.put(
                HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS,
                String.valueOf(retryAfterInMillis));
        }

        responseHeaders.put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(subStatusCode));

        long lsn = 1;
        String partitionKeyRangeId = "5";
        CosmosException cosmosException = new RequestRateTooLargeException(null, lsn, partitionKeyRangeId, responseHeaders);
        ShouldRetryResult shouldRetryResult = retryPolicy.shouldRetry(cosmosException).block();

        assertThat(shouldRetryResult.shouldRetry).isEqualTo(shouldRetry);
        assertThat(shouldRetryResult.backOffTime.get().toMillis()).isEqualTo(shouldRetryDelay);
    }
}
