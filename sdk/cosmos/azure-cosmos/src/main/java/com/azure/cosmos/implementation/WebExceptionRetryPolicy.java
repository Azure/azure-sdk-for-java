// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WebExceptionRetryPolicy implements IRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(WebExceptionRetryPolicy.class);

    // total wait time in seconds to retry. should be max of primary reconfigrations/replication wait duration etc
    private final static int waitTimeInSeconds = 30;
    private final static int initialBackoffSeconds = 1;
    private final static int backoffMultiplier = 2;

    private StopWatch durationTimer = new StopWatch();
    private int attemptCount = 1;
    // Don't penalise first retry with delay.
    private int currentBackoffSeconds = WebExceptionRetryPolicy.initialBackoffSeconds;
    private RetryContext retryContext;

    public WebExceptionRetryPolicy() {
        durationTimer.start();
    }

    public WebExceptionRetryPolicy(RetryContext retryContext) {
        durationTimer.start();
        this.retryContext = retryContext;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        Duration backoffTime = Duration.ofSeconds(0);

        if (!WebExceptionUtility.isWebExceptionRetriable(exception)) {
            // Have caller propagate original exception.
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }

        // Don't penalise first retry with delay.
        if (attemptCount++ > 1) {
            int remainingSeconds = WebExceptionRetryPolicy.waitTimeInSeconds - Math.toIntExact(this.durationTimer.getTime(TimeUnit.SECONDS));
            if (remainingSeconds <= 0) {
                this.durationTimer.stop();
                return Mono.just(ShouldRetryResult.noRetry());
            }

            backoffTime = Duration.ofSeconds(Math.min(this.currentBackoffSeconds, remainingSeconds));
            this.currentBackoffSeconds *= WebExceptionRetryPolicy.backoffMultiplier;
        }

        logger.warn("Received retriable web exception, will retry", exception);

        return Mono.just(ShouldRetryResult.retryAfter(backoffTime));
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }
}
