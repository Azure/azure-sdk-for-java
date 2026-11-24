// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.caches.InCompleteRoutingMapRetryPolicy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.test.StepVerifier;

public class InCompleteRoutingMapRetryPolicyTest {
    private InCompleteRoutingMapRetryPolicy retryPolicy;

    @BeforeMethod(groups = {"unit"})
    public void beforeMethod() {
        retryPolicy = new InCompleteRoutingMapRetryPolicy();
    }

    @Test(groups = {"unit"})
    public void shouldRetry_WithInCompleteRoutingMapException() {
        InCompleteRoutingMapException exception = new InCompleteRoutingMapException("test");

        // First attempt - should retry
        StepVerifier.create(retryPolicy.shouldRetry(exception))
            .expectNext(ShouldRetryResult.RETRY_NOW)
            .verifyComplete();

        // Second attempt - should not retry
        StepVerifier.create(retryPolicy.shouldRetry(exception))
            .expectNext(ShouldRetryResult.NO_RETRY)
            .verifyComplete();
    }

    @Test(groups = {"unit"})
    public void shouldRetry_WithOtherException_ShouldNotRetry() {
        Exception otherException = new RuntimeException("Some other error");

        StepVerifier.create(retryPolicy.shouldRetry(otherException))
            .expectNext(ShouldRetryResult.NO_RETRY)
            .verifyComplete();
    }

    @Test(groups = {"unit"})
    public void shouldRetry_WithPartitionKeyRangeNotFound_UnknownSubStatus() {
        // A plain NotFoundException carries sub-status 0 (UNKNOWN) - this exercises the UNKNOWN branch.
        NotFoundException exception = new NotFoundException("partition key range not found");
        long[] expectedBackoffInMillis = new long[] {100, 200, 400, 800, 1000};

        for (long expectedBackoff : expectedBackoffInMillis) {
            StepVerifier.create(retryPolicy.shouldRetry(exception))
                .expectNextMatches(shouldRetryResult -> shouldRetryResult.shouldRetry
                    && shouldRetryResult.backOffTime != null
                    && shouldRetryResult.backOffTime.toMillis() == expectedBackoff)
                .verifyComplete();
        }
    }

    @Test(groups = {"unit"})
    public void shouldRetry_WithPartitionKeyRangeNotFound_OwnerResourceNotExists() {
        NotFoundException exception = new NotFoundException("owner resource does not exist");
        BridgeInternal.setSubStatusCode(exception, HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS);

        StepVerifier.create(retryPolicy.shouldRetry(exception))
            .expectNextMatches(shouldRetryResult -> shouldRetryResult.shouldRetry
                && shouldRetryResult.backOffTime != null
                && shouldRetryResult.backOffTime.toMillis() == 100)
            .verifyComplete();
    }

    @Test(groups = {"unit"})
    public void shouldRetry_WithPartitionKeyRangeNotFound_CollectionNotAvailableForRead() {
        NotFoundException exception = new NotFoundException("collection not available for read");
        BridgeInternal.setSubStatusCode(exception, HttpConstants.SubStatusCodes.COLLECTION_NOT_AVAILABLE_FOR_READ);

        StepVerifier.create(retryPolicy.shouldRetry(exception))
            .expectNextMatches(shouldRetryResult -> shouldRetryResult.shouldRetry
                && shouldRetryResult.backOffTime != null
                && shouldRetryResult.backOffTime.toMillis() == 100)
            .verifyComplete();
    }

    @Test(groups = {"unit"})
    public void shouldRetry_PartitionKeyRangeNotFound_ExhaustsAfterMaxRetries() {
        NotFoundException exception = new NotFoundException("owner resource does not exist");
        BridgeInternal.setSubStatusCode(exception, HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS);

        // 10 retries are allowed; backoff doubles from 100ms and is capped at 1000ms.
        long[] expectedBackoffInMillis = new long[] {100, 200, 400, 800, 1000, 1000, 1000, 1000, 1000, 1000};
        for (long expectedBackoff : expectedBackoffInMillis) {
            StepVerifier.create(retryPolicy.shouldRetry(exception))
                .expectNextMatches(shouldRetryResult -> shouldRetryResult.shouldRetry
                    && shouldRetryResult.backOffTime != null
                    && shouldRetryResult.backOffTime.toMillis() == expectedBackoff)
                .verifyComplete();
        }

        // 11th attempt: all retries exhausted, must stop.
        StepVerifier.create(retryPolicy.shouldRetry(exception))
            .expectNext(ShouldRetryResult.NO_RETRY)
            .verifyComplete();
    }

    @Test(groups = {"unit"})
    public void shouldRetry_WithNullException_ShouldNotRetry() {
        StepVerifier.create(retryPolicy.shouldRetry(null))
            .expectNext(ShouldRetryResult.NO_RETRY)
            .verifyComplete();
    }

    @Test(groups = {"unit"})
    public void getRetryContext_ReturnsNull() {
        RetryContext context = retryPolicy.getRetryContext();
        assert context == null : "RetryContext should be null";
    }
}
