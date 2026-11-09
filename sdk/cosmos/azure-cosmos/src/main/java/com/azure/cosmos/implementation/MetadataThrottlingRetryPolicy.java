// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class MetadataThrottlingRetryPolicy extends ResourceThrottleRetryPolicy {
    private final static int RANDOM_SALT_IN_MS = 100;
    private final static int MAX_ATTEMPT_COUNT = Integer.MAX_VALUE;
    private final static Duration MAX_RETRY_WAIT_TIME = Duration.ofSeconds((Integer.MAX_VALUE / 1000) - 1);

    public MetadataThrottlingRetryPolicy(RetryContext retryContext) {
        super(MAX_ATTEMPT_COUNT, MAX_RETRY_WAIT_TIME, retryContext, false);
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        return super.shouldRetry(exception)
            .flatMap(shouldRetryResult -> {
                if (shouldRetryResult.shouldRetry) {
                    // ResourceThrottleRetryPolicy will retry based on the back-off returned from server
                    // for metadata request, in order to spread the requests more, adding some random salt from sdk side
                    shouldRetryResult.withRandomSalt(ThreadLocalRandom.current().nextInt(RANDOM_SALT_IN_MS + 1));
                }

                return Mono.just(shouldRetryResult);
            });
    }
}
