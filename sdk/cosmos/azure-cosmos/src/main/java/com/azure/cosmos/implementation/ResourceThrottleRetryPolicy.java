// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
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

    private final static Duration DEFAULT_RETRY_IN_SECONDS_FOR_3200 = Duration.ofSeconds(0);
    private final static Duration DEFAULT_RETRY_IN_SECONDS_FOR_OTHERS = Duration.ofMillis(100);
    private final int backoffDelayFactor;
    private final int maxAttemptCount;
    private final Duration maxWaitTime;

    // TODO: is this thread safe?
    // should we make this atomic int?
    private int currentAttemptCount;
    private Duration cumulativeRetryDelay;
    private RetryContext retryContext;
    private final boolean retryOnClientSideThrottledBatchRequests;

    public ResourceThrottleRetryPolicy(
        int maxAttemptCount,
        Duration maxWaitTime,
        RetryContext retryContext,
        boolean retryOnClientSideThrottledBatchRequests) {

        this(maxAttemptCount, maxWaitTime, retryOnClientSideThrottledBatchRequests);
        this.retryContext = retryContext;
    }

    public ResourceThrottleRetryPolicy(
        int maxAttemptCount,
        Duration maxWaitTime,
        boolean retryOnClientSideThrottledBatchRequests) {

        this(maxAttemptCount, maxWaitTime, 1, retryOnClientSideThrottledBatchRequests);
    }

    public ResourceThrottleRetryPolicy(
        int maxAttemptCount,
        Duration maxWaitTime,
        int backoffDelayFactor,
        boolean retryOnClientSideThrottledBatchRequests) {

        Utils.checkStateOrThrow(maxWaitTime.getSeconds() <= Integer.MAX_VALUE / 1000, "maxWaitTime", "maxWaitTime must not be larger than " + Integer.MAX_VALUE / 1000);

        this.maxAttemptCount = maxAttemptCount;
        this.backoffDelayFactor = backoffDelayFactor;
        this.maxWaitTime = maxWaitTime;
        this.currentAttemptCount = 0;
        this.cumulativeRetryDelay = Duration.ZERO;
        this.retryOnClientSideThrottledBatchRequests = retryOnClientSideThrottledBatchRequests;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        Duration retryDelay = Duration.ZERO;

        CosmosException dce = Utils.as(exception, CosmosException.class);
        if (dce == null || !Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.TOO_MANY_REQUESTS)) {
            logger.debug(
                "Operation will NOT be retried - not a throttled request. Current attempt {}",
                this.currentAttemptCount,
                exception);
            return Mono.just(ShouldRetryResult.errorOnNonRelatedException(exception));
        }

        if (!retryOnClientSideThrottledBatchRequests &&
            dce.getSubStatusCode() == HttpConstants.SubStatusCodes.THROUGHPUT_CONTROL_BULK_REQUEST_RATE_TOO_LARGE) {

            return Mono.just(ShouldRetryResult.noRetry());
        }

        if (this.currentAttemptCount < this.maxAttemptCount &&
                (retryDelay = checkIfRetryNeeded(dce)) != null) {
            this.currentAttemptCount++;

            logger.warn(
                "Operation will be retried after {} milliseconds. Current attempt {}, Cumulative delay {} for statusCode {} and subStatusCode {}",
                retryDelay.toMillis(),
                this.currentAttemptCount,
                this.cumulativeRetryDelay,
                dce.getStatusCode(),
                dce.getSubStatusCode());

            logger.debug(
                    "Operation will be retried after {} milliseconds. Current attempt {}, Cumulative delay {}",
                    retryDelay.toMillis(),
                    this.currentAttemptCount,
                    this.cumulativeRetryDelay,
                    exception);

            return Mono.just(ShouldRetryResult.retryAfter(retryDelay));
        } else {
            if (retryDelay != null) {
                logger.warn(
                    "Operation will NOT be retried. Current attempt {}",
                    this.currentAttemptCount,
                    exception);
            } else {
                logger.debug(
                    "Operation will NOT be retried - not a throttled request. Current attempt {}",
                    this.currentAttemptCount,
                    exception);
            }

            return Mono.just(ShouldRetryResult.noRetry());
        }
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        // no op
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }

    // if retry not needed returns null
    /// <summary>
    /// Returns True if the given exception <paramref name="exception"/> is retriable
    /// </summary>
    /// <param name="exception">Exception to examine</param>
    /// <param name="retryDelay">retryDelay</param>
    /// <returns>True if the exception is retriable; False otherwise</returns>
    private Duration checkIfRetryNeeded(CosmosException dce) {
        Duration retryDelay = Duration.ZERO;
        if (dce != null){
            if (Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.TOO_MANY_REQUESTS))  {
                retryDelay = dce.getRetryAfterDuration();
                if (this.backoffDelayFactor > 1) {
                    retryDelay = Duration.ofNanos(retryDelay.toNanos() * this.backoffDelayFactor);
                }

                if (retryDelay.isNegative()) {
                    // There is no server returned retryAfter
                    if (Exceptions.isSubStatusCode(dce, HttpConstants.SubStatusCodes.USER_REQUEST_RATE_TOO_LARGE)) {
                        // for 429/3200, server should have returned retry after. If not, then retry immediately
                        logger.warn("No retryAfter returned by server in 429/3200, retrying immediately");
                        retryDelay = DEFAULT_RETRY_IN_SECONDS_FOR_3200;
                    } else {
                        // retryAfter will not always be returned, for example 429/3089
                        // in this case, wait for a minimum time then retry
                        retryDelay = DEFAULT_RETRY_IN_SECONDS_FOR_OTHERS;
                    }
                }

                if (retryDelay.toMillis() < this.maxWaitTime.toMillis() &&
                        this.maxWaitTime.toMillis() >= (this.cumulativeRetryDelay = retryDelay.plus(this.cumulativeRetryDelay)).toMillis())
                {
                    return retryDelay;
                }
            }
        }
        // if retry not needed returns null
        return null;
    }
}
