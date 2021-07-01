// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class SessionTokenMismatchRetryPolicy implements IRetryPolicy {

    private final static Logger LOGGER = LoggerFactory.getLogger(SessionTokenMismatchRetryPolicy.class);

    private static final int BACKOFF_MULTIPLIER = 2;

    private final Duration maximumBackoff;
    private final TimeoutHelper waitTimeTimeoutHelper;
    private final AtomicInteger retryCount;

    private Duration currentBackoff;
    private RetryContext retryContext;

    public SessionTokenMismatchRetryPolicy(RetryContext retryContext, int waitTimeInMilliSeconds)
    {
        this.waitTimeTimeoutHelper = new TimeoutHelper(Duration.ofMillis(waitTimeInMilliSeconds));
        this.maximumBackoff = Duration.ofMillis(Configs.getSessionTokenMismatchMaximumBackoffTimeInMs());

        this.retryCount = new AtomicInteger();
        this.retryCount.set(0);
        this.currentBackoff = Duration.ofMillis(Configs.getSessionTokenMismatchInitialBackoffTimeInMs());
        this.retryContext = retryContext;
    }

    public SessionTokenMismatchRetryPolicy(RetryContext retryContext) {
        this(retryContext, Configs.getSessionTokenMismatchDefaultWaitTimeInMs());
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {

        if (!(e instanceof CosmosException)) {
            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }

        CosmosException cosmosException = (CosmosException)e;

        if (cosmosException.getStatusCode() != HttpConstants.StatusCodes.NOTFOUND ||
            cosmosException.getSubStatusCode() != HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE) {

            LOGGER.debug(
                "SessionTokenMismatchRetryPolicy not retrying because StatusCode or SubStatusCode not found.");

            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }

        if (this.waitTimeTimeoutHelper.isElapsed()) {

            LOGGER.warn(
                "SessionTokenMismatchRetryPolicy not retrying because it has exceeded " +
                    "the time limit. Retry count = {}",
                this.retryCount);

            return Mono.just(ShouldRetryResult.noRetry());
        }

        Duration effectiveBackoff = Duration.ZERO;

        // Don't penalize first retry with delay
        if (this.retryCount.getAndIncrement() > 0) {

            // Get the backoff time by selecting the smallest value between the remaining time and
            // the current back off time
            effectiveBackoff = getEffectiveBackoff(
                this.currentBackoff,
                this.waitTimeTimeoutHelper.getRemainingTime());

            // Update the current back off time
            this.currentBackoff = getEffectiveBackoff(
                    Duration.ofMillis(this.currentBackoff.toMillis() * BACKOFF_MULTIPLIER),
                    this.maximumBackoff);
        }

        LOGGER.debug(
            "SessionTokenMismatchRetryPolicy will retry. Retry count = {}.  Backoff time = {} ms",
            this.retryCount,
            effectiveBackoff.toMillis());

        return Mono.just(ShouldRetryResult.retryAfter(effectiveBackoff));
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }

    private static Duration getEffectiveBackoff(Duration backoff, Duration remainingTime) {
        if (backoff.compareTo(remainingTime) > 0) {
            return remainingTime;
        }

        return backoff;
    }
}
