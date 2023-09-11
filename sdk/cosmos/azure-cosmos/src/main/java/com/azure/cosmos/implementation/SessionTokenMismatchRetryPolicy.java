// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.SessionRetryOptions;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import com.azure.cosmos.CosmosRegionSwitchHint;
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
    private final AtomicInteger maxRetryAttemptsInCurrentRegion;
    private final SessionRetryOptions sessionRetryOptions;

    public SessionTokenMismatchRetryPolicy(RetryContext retryContext, int waitTimeInMilliseconds, SessionRetryOptions sessionRetryOptions) {
        this.waitTimeTimeoutHelper = new TimeoutHelper(Duration.ofMillis(Configs.getSessionTokenMismatchDefaultWaitTimeInMs()));
        this.maximumBackoff = Duration.ofMillis(Configs.getSessionTokenMismatchMaximumBackoffTimeInMs());
        this.retryCount = new AtomicInteger();
        this.retryCount.set(0);
        this.currentBackoff = Duration.ofMillis(Configs.getSessionTokenMismatchInitialBackoffTimeInMs());
        this.maxRetryAttemptsInCurrentRegion = new AtomicInteger(Configs.getMaxRetriesInLocalRegionWhenRemoteRegionPreferred());
        this.retryContext = retryContext;
        this.sessionRetryOptions = sessionRetryOptions;
    }

    public SessionTokenMismatchRetryPolicy(RetryContext retryContext, SessionRetryOptions sessionRetryOptions) {
        this(retryContext, Configs.getSessionTokenMismatchDefaultWaitTimeInMs(), sessionRetryOptions);
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

        // when retry is directed to the current region
        // we should use the region-switch hint to determine
        // to move to a different region
        // special case of switching to the same region again:
        //   1. for single-write account, if the original read request is directed
        //      to the write region then region switch using ClientRetryPolicy will route
        //      the retry to the same write region again, therefore the DIFFERENT_REGION_PREFERRED
        //      hint causes quicker switch to the same write region which is reasonable
        if (!shouldRetryLocally(sessionRetryOptions, retryCount.get())) {

            LOGGER.debug("SessionTokenMismatchRetryPolicy not retrying because it a retry attempt for the current region and " +
                "fallback to a different region is preferred ");

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

    private boolean shouldRetryLocally(SessionRetryOptions sessionRetryOptions, int sessionTokenMismatchRetryAttempts) {

        if (sessionRetryOptions == null) {
            return true;
        }

        CosmosRegionSwitchHint regionSwitchHint = ImplementationBridgeHelpers
            .CosmosSessionRetryOptionsHelper
            .getCosmosSessionRetryOptionsAccessor()
            .getRegionSwitchHint(sessionRetryOptions);

        if (regionSwitchHint == null || regionSwitchHint == CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED) {
            return true;
        }

        // SessionTokenMismatchRetryPolicy is invoked after 1 attempt on a region
        // sessionTokenMismatchRetryAttempts increments only after shouldRetry triggers
        // another attempt on the same region
        // hence to curb the retry attempts on a region,
        // compare sessionTokenMismatchRetryAttempts with max retry attempts allowed on the region - 1
        return !(regionSwitchHint == CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED
            && sessionTokenMismatchRetryAttempts == (this.maxRetryAttemptsInCurrentRegion.get() - 1));
    }
}
