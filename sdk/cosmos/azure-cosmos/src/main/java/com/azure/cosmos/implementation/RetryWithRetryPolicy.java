// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public class RetryWithRetryPolicy implements IRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(RetryWithRetryPolicy.class);
    private final static int DEFAULT_WAIT_TIME_IN_SECONDS = 30;
    private final static int MAXIMUM_BACKOFF_TIME_IN_MS = 1000;
    private final static int INITIAL_BACKOFF_TIME_MS = 10;
    private final static int BACK_OFF_MULTIPLIER = 2;
    private final static int RANDOM_SALT_IN_MS = 5;

    private final AtomicInteger attemptCount = new AtomicInteger(1);
    private final AtomicInteger currentBackoffMilliseconds =
        new AtomicInteger(RetryWithRetryPolicy.INITIAL_BACKOFF_TIME_MS);
    private final int waitTimeInSeconds;
    private final RetryContext retryContext;
    private final LongSupplier elapsedTimeInMillisSupplier;
    private final Consumer<CosmosException> retryWithExceptionConsumer;

    public RetryWithRetryPolicy(
        Integer waitTimeInSeconds,
        RetryContext retryContext,
        LongSupplier elapsedTimeInMillisSupplier,
        Consumer<CosmosException> retryWithExceptionConsumer) {

        this.waitTimeInSeconds = waitTimeInSeconds != null ? waitTimeInSeconds : DEFAULT_WAIT_TIME_IN_SECONDS;
        this.retryContext = retryContext;
        this.elapsedTimeInMillisSupplier = elapsedTimeInMillisSupplier;
        this.retryWithExceptionConsumer = retryWithExceptionConsumer;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        CosmosException cosmosException = Utils.as(exception, CosmosException.class);
        if (cosmosException == null || cosmosException.getStatusCode() != HttpConstants.StatusCodes.RETRY_WITH) {
            logger.debug("Operation will NOT be retried. Current attempt {}, Exception: ", this.attemptCount.get(),
                exception);
            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }

        if (this.retryWithExceptionConsumer != null) {
            this.retryWithExceptionConsumer.accept(cosmosException);
        }

        long remainingMilliseconds =
            (this.waitTimeInSeconds * 1_000L) - this.elapsedTimeInMillisSupplier.getAsLong();
        int currentRetryAttemptCount = this.attemptCount.getAndIncrement();

        if (remainingMilliseconds <= 0) {
            logger.warn("Received RetryWith response after backoff/retry. Will fail the request.", cosmosException);
            return Mono.just(ShouldRetryResult.error(cosmosException));
        }

        Duration backoffTime = Duration.ofMillis(
            Math.min(
                Math.min(
                    this.currentBackoffMilliseconds.get()
                        + ThreadLocalRandom.current().nextInt(RetryWithRetryPolicy.RANDOM_SALT_IN_MS),
                    remainingMilliseconds),
                RetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_MS));

        this.currentBackoffMilliseconds.set(
            Math.max(
                RetryWithRetryPolicy.INITIAL_BACKOFF_TIME_MS,
                Math.min(
                    RetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_MS,
                    this.currentBackoffMilliseconds.get() * RetryWithRetryPolicy.BACK_OFF_MULTIPLIER))
        );

        logger.debug("BackoffTime: {} ms.", backoffTime.toMillis());

        long timeoutInMillSec = remainingMilliseconds - backoffTime.toMillis();
        Duration timeout = timeoutInMillSec > 0 ? Duration.ofMillis(timeoutInMillSec)
            : Duration.ofMillis(RetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_MS);

        logger.debug("Received RetryWith response, will retry, ", exception);

        return Mono.just(ShouldRetryResult.retryAfter(backoffTime,
            Quadruple.with(false, true, timeout, currentRetryAttemptCount)));
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }
}