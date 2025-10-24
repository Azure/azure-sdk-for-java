// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

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