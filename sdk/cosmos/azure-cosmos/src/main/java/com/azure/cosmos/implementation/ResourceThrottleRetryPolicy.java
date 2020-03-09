// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class ResourceThrottleRetryPolicy extends DocumentClientRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(ResourceThrottleRetryPolicy.class);

    private final static Duration DEFAULT_MAX_WAIT_TIME_IN_SECONDS = Duration.ofSeconds(60);
    private final static Duration DEFAULT_RETRY_IN_SECONDS = Duration.ofSeconds(5);
    private final int backoffDelayFactor;
    private final int maxAttemptCount;
    private final Duration maxWaitTime;

    // TODO: is this thread safe?
    // should we make this atomic int?
    private int currentAttemptCount;
    private Duration cumulativeRetryDelay;

    public ResourceThrottleRetryPolicy(int maxAttemptCount, Duration maxWaitTime) {
        this(maxAttemptCount, maxWaitTime, 1);
    }

    public ResourceThrottleRetryPolicy(int maxAttemptCount) {
        this(maxAttemptCount, DEFAULT_MAX_WAIT_TIME_IN_SECONDS, 1);
    }

    public ResourceThrottleRetryPolicy(int maxAttemptCount, Duration maxWaitTime, int backoffDelayFactor) {
        Utils.checkStateOrThrow(maxWaitTime.getSeconds() < Integer.MAX_VALUE / 1000, "maxWaitTime", "maxWaitTime must be less than " + Integer.MAX_VALUE / 1000);

        this.maxAttemptCount = maxAttemptCount;
        this.backoffDelayFactor = backoffDelayFactor;
        this.maxWaitTime = maxWaitTime;
        this.currentAttemptCount = 0;
        this.cumulativeRetryDelay = Duration.ZERO;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        Duration retryDelay = Duration.ZERO;

        if (this.currentAttemptCount < this.maxAttemptCount &&
                (retryDelay = checkIfRetryNeeded(exception)) != null) {
            this.currentAttemptCount++;
            logger.warn(
                    "Operation will be retried after {} milliseconds. Current attempt {}, Cumulative delay {}",
                    retryDelay.toMillis(), 
                    this.currentAttemptCount,
                    this.cumulativeRetryDelay,
                    exception);
            return Mono.just(ShouldRetryResult.retryAfter(retryDelay));
        } else {
            logger.debug(
                    "Operation will NOT be retried. Current attempt {}",
                    this.currentAttemptCount, 
                    exception);
            return Mono.just(ShouldRetryResult.noRetry());
        }
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        // no op
    }

    // if retry not needed reaturns null
    /// <summary>
    /// Returns True if the given exception <paramref name="exception"/> is retriable
    /// </summary>
    /// <param name="exception">Exception to examine</param>
    /// <param name="retryDelay">retryDelay</param>
    /// <returns>True if the exception is retriable; False otherwise</returns>
    private Duration checkIfRetryNeeded(Exception exception) {
        Duration retryDelay = Duration.ZERO;

        CosmosClientException dce = Utils.as(exception, CosmosClientException.class);

        if (dce != null){

            if (Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.TOO_MANY_REQUESTS))  {
                retryDelay = dce.getRetryAfterDuration();
                if (this.backoffDelayFactor > 1) {
                    retryDelay = Duration.ofNanos(retryDelay.toNanos() * this.backoffDelayFactor);
                }

                if (retryDelay.toMillis() < this.maxWaitTime.toMillis() &&
                        this.maxWaitTime.toMillis() >= (this.cumulativeRetryDelay = retryDelay.plus(this.cumulativeRetryDelay)).toMillis())
                {
                    if (retryDelay == Duration.ZERO){
                        // we should never reach here as BE should turn non-zero of retryDelay
                        logger.trace("Received retryDelay of 0 with Http 429", exception);
                        retryDelay = DEFAULT_RETRY_IN_SECONDS;
                    }

                    return retryDelay;
                }
            }
        }
        // if retry not needed returns null
        return null;
    }
}
