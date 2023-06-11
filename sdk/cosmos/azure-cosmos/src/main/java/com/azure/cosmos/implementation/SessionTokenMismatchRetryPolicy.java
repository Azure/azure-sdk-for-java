// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosSessionRetryOptions;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import com.azure.cosmos.models.CosmosRegionSwitchHint;
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
    private final RetryContext retryContext;
    private final RxDocumentServiceRequest request;
    private final AtomicInteger maxRetryAttemptsInLocalRegion;

    public SessionTokenMismatchRetryPolicy(RxDocumentServiceRequest request) {
        this.waitTimeTimeoutHelper = new TimeoutHelper(Duration.ofMillis(Configs.getSessionTokenMismatchDefaultWaitTimeInMs()));
        this.maximumBackoff = Duration.ofMillis(Configs.getSessionTokenMismatchMaximumBackoffTimeInMs());
        this.request = request;
        this.retryCount = new AtomicInteger();
        this.retryCount.set(0);
        this.currentBackoff = Duration.ofMillis(Configs.getSessionTokenMismatchInitialBackoffTimeInMs());
        this.maxRetryAttemptsInLocalRegion = new AtomicInteger(Configs.getMaxRetriesLocalRegionWhenRemoteRegionPreferred());
        this.retryContext = BridgeInternal.getRetryContext(request.requestContext.cosmosDiagnostics);
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

        // when sessionTokenMismatchRetryAttempt == 0, this is a request routed
        // to some possible local region (first region from preferredRegions) or
        // first read-available (for read requests) region or first write-available
        // (for write requests) region
        // when sessionTokenMismatchRetryAttempt > 0, this is a request possibly routed to
        // a different region which is not a local region
        boolean isRequestForPossibleLocalRegion = request.requestContext.sessionTokenMismatchRetryAttempt == 0;

        if (isRequestForPossibleLocalRegion) {
            // when request is directed to a possible local region
            // we should use the region-switch hint to determine
            // to move to a different region
            // special case of switching to the same region again:
            //   1. for single-write account, if the original read request is directed
            //      to the write region then region switch using ClientRetryPolicy will route
            //      the retry to the same write region again, therefore the REMOTE_REGION_PREFERRED
            //      hint causes quicker switch to the same write region which is reasonable
            if (!shouldRetryInPossibleLocalRegion(request, retryCount.get())) {

                LOGGER.debug("SessionTokenMismatchRetryPolicy not retrying because it a retry attempt for a local region and " +
                    "fallback to remote region is preferred ");

                return Mono.just(ShouldRetryResult.noRetry());
            }
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

    private boolean shouldRetryInPossibleLocalRegion(RxDocumentServiceRequest request, int retryCountForRegion) {

        CosmosSessionRetryOptions sessionRetryOptions = request.requestContext.getSessionRetryOptions();

        if (sessionRetryOptions == null) {
            return true;
        }

        CosmosRegionSwitchHint regionSwitchHint = ImplementationBridgeHelpers
            .CosmosSessionRetryOptionsHelper
            .getCosmosSessionRetryOptionsAccessor()
            .getRegionSwitchHint(sessionRetryOptions);

        if (regionSwitchHint == null || regionSwitchHint == CosmosRegionSwitchHint.LOCAL_REGION_PREFERED) {
            return true;
        }

        return !(regionSwitchHint == CosmosRegionSwitchHint.REMOTE_REGION_PREFERED
            && retryCountForRegion == this.maxRetryAttemptsInLocalRegion.get());
    }
}
