// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Quadruple;
import com.azure.cosmos.implementation.RetryPolicyWithDiagnostics;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

public class RetryWithRetryPolicy extends RetryPolicyWithDiagnostics {
    private final static Logger logger = LoggerFactory.getLogger(RetryWithRetryPolicy.class);
    private final static int DEFAULT_WAIT_TIME_IN_SECONDS = 30;
    private final static int MAXIMUM_BACKOFF_TIME_IN_MS = 15000;
    private final static int INITIAL_BACKOFF_TIME_MS = 10;
    private final static int BACK_OFF_MULTIPLIER = 2;

    private final RxDocumentServiceRequest request;
    private volatile int attemptCount = 1;
    private volatile int currentBackoffMilliseconds = RetryWithRetryPolicy.INITIAL_BACKOFF_TIME_MS;
    private final StopWatch durationTimer = new StopWatch();
    private final int waitTimeInSeconds;
    //TODO once this is moved to IRetryPolicy, remove from here.
    public final static Quadruple<Boolean, Boolean, Duration, Integer> INITIAL_ARGUMENT_VALUE_POLICY_ARG = Quadruple.with(false, false,
        Duration.ofSeconds(60), 0);

    public RetryWithRetryPolicy(RxDocumentServiceRequest request, Integer waitTimeInSeconds) {
        this.request = request;
        startStopWatch(this.durationTimer);
        this.waitTimeInSeconds = Objects.requireNonNullElse(waitTimeInSeconds,
            DEFAULT_WAIT_TIME_IN_SECONDS);
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        Duration backoffTime;
        Duration timeout;

        if (!(exception instanceof RetryWithException)) {
            logger.debug("Operation will NOT be retried. Current attempt {}, Exception: ", this.attemptCount,
                exception);
            stopStopWatch(this.durationTimer);
            return Mono.just(ShouldRetryResult.noRetry());
        }

        RetryWithException lastRetryWithException = (RetryWithException)exception;
        this.request.setLastRetryWithException(lastRetryWithException);

        long remainingMilliseconds = (this.waitTimeInSeconds * 1000) - this.durationTimer.getTime();
        int currentRetryAttemptCount = this.attemptCount++;

        if (remainingMilliseconds <= 0) {
            logger.warn("Received retrywith exception after backoff/retry. Will fail the request.",
                lastRetryWithException);

            stopStopWatch(this.durationTimer);
            return Mono.just(ShouldRetryResult.error(lastRetryWithException));
        }

        backoffTime = Duration.ofMillis(
            Math.min(
                Math.min(this.currentBackoffMilliseconds, remainingMilliseconds),
                RetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_MS));
        this.currentBackoffMilliseconds *= RetryWithRetryPolicy.BACK_OFF_MULTIPLIER;
        logger.debug("BackoffTime: {} ms.", backoffTime.toMillis());

        // Calculate the remaining time based after accounting for the backoff that we
        // will perform
        long timeoutInMillSec = remainingMilliseconds - backoffTime.toMillis();
        timeout = timeoutInMillSec > 0 ? Duration.ofMillis(timeoutInMillSec)
            : Duration.ofMillis(RetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_MS);

        logger.warn("Received retrywith exception, will retry, ", exception);

        // For RetryWithException, prevent the caller
        // from refreshing any caches.
        return Mono.just(ShouldRetryResult.retryAfter(backoffTime,
            Quadruple.with(false, true, timeout, currentRetryAttemptCount)));
    }

    private void stopStopWatch(StopWatch stopwatch) {
        synchronized (stopwatch) {
            stopwatch.stop();
        }
    }

    private void startStopWatch(StopWatch stopwatch) {
        synchronized (stopwatch) {
            stopwatch.start();
        }
    }
}
