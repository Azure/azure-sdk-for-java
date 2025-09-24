// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class MetadataThrottlingRetryPolicy extends ResourceThrottleRetryPolicy {
    private final static int RANDOM_SALT_IN_MS = 5;
    private final static int MAX_ATTEMPT_COUNT = Integer.MAX_VALUE;
    private final static Duration MAX_RETRY_WAIT_TIME = Duration.ofSeconds((Integer.MAX_VALUE / 1000) - 1);
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public MetadataThrottlingRetryPolicy(RetryContext retryContext) {
        super(MAX_ATTEMPT_COUNT, MAX_RETRY_WAIT_TIME, retryContext, false);
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        return super.shouldRetry(exception)
            .flatMap(shouldRetryResult -> {
                if (shouldRetryResult.shouldRetry) {
                    shouldRetryResult.withRandomSalt(random.nextInt(RANDOM_SALT_IN_MS));
                }

                return Mono.just(shouldRetryResult);
            });
    }
}
