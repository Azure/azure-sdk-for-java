// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.Quadruple;
import com.azure.cosmos.implementation.RetryPolicyWithDiagnostics;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class GoneAndRetryWithRetryPolicy extends RetryPolicyWithDiagnostics {
    private final static Logger logger = LoggerFactory.getLogger(GoneAndRetryWithRetryPolicy.class);
    private final static int DEFAULT_WAIT_TIME_IN_SECONDS = 30;
    private final static int MAXIMUM_BACKOFF_TIME_IN_SECONDS = 15;
    private final static int INITIAL_BACKOFF_TIME = 1;
    private final static int BACK_OFF_MULTIPLIER = 2;

    private final RxDocumentServiceRequest request;
    private volatile int attemptCount = 1;
    private volatile int attemptCountInvalidPartition = 1;
    private volatile int currentBackoffSeconds = GoneAndRetryWithRetryPolicy.INITIAL_BACKOFF_TIME;
    private volatile RetryWithException lastRetryWithException;
    private final StopWatch durationTimer = new StopWatch();
    private final int waitTimeInSeconds;
    //TODO once this is moved to IRetryPolicy, remove from here.
    public final static Quadruple<Boolean, Boolean, Duration, Integer> INITIAL_ARGUMENT_VALUE_POLICY_ARG = Quadruple.with(false, false,
            Duration.ofSeconds(60), 0);

    public GoneAndRetryWithRetryPolicy(RxDocumentServiceRequest request, Integer waitTimeInSeconds) {
        this.request = request;
        startStopWatch(this.durationTimer);
        if (waitTimeInSeconds != null) {
            this.waitTimeInSeconds = waitTimeInSeconds;
        } else {
            this.waitTimeInSeconds = DEFAULT_WAIT_TIME_IN_SECONDS;
        }
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        CosmosException exceptionToThrow = null;
        Duration backoffTime = Duration.ofSeconds(0);
        Duration timeout = Duration.ofSeconds(0);
        boolean forceRefreshAddressCache = false;
        if (!(exception instanceof GoneException) &&
            !(exception instanceof RetryWithException) &&
            !(exception instanceof PartitionIsMigratingException) &&
            !(exception instanceof InvalidPartitionException &&
                (this.request.getPartitionKeyRangeIdentity() == null ||
                this.request.getPartitionKeyRangeIdentity().getCollectionRid() == null)) &&
            !(exception instanceof PartitionKeyRangeIsSplittingException)) {

            logger.warn("Operation will NOT be retried. Current attempt {}, Exception: ", this.attemptCount,
                exception);
            stopStopWatch(this.durationTimer);
            return Mono.just(ShouldRetryResult.noRetry());
        } else if (exception instanceof GoneException &&
            !request.isReadOnly() &&
            BridgeInternal.hasSendingRequestStarted((CosmosException)exception)) {

            logger.warn(
                "Operation will NOT be retried. Write operations can not be retried safely when sending the request " +
                    "to the service because they aren't idempotent. Current attempt {}, Exception: ",
                this.attemptCount,
                exception);
            stopStopWatch(this.durationTimer);

            return Mono.just(ShouldRetryResult.noRetry(
                Quadruple.with(true, true, Duration.ofMillis(0), this.attemptCount)));
        } else if (exception instanceof RetryWithException) {
            this.lastRetryWithException = (RetryWithException) exception;
        }
        long remainingSeconds = this.waitTimeInSeconds - this.durationTimer.getTime() / 1000;
        int currentRetryAttemptCount = this.attemptCount;
        if (this.attemptCount++ > 1) {
            if (remainingSeconds <= 0) {
                if (exception instanceof GoneException) {
                    if (this.lastRetryWithException != null) {
                        logger.warn(
                                "Received gone exception after backoff/retry including at least one RetryWithException. "
                                        + "Will fail the request with RetryWithException. GoneException: {}. RetryWithException: {}",
                                exception, this.lastRetryWithException);
                        exceptionToThrow = this.lastRetryWithException;
                    } else {
                        logger.warn("Received gone exception after backoff/retry. Will fail the request. {}",
                                exception.toString());
                        exceptionToThrow = BridgeInternal.createServiceUnavailableException(exception);
                    }
                } else if (exception instanceof PartitionKeyRangeGoneException) {
                    if (this.lastRetryWithException != null) {
                        logger.warn(
                                "Received partition key range gone exception after backoff/retry including at least one RetryWithException."
                                        + "Will fail the request with RetryWithException. GoneException: {}. RetryWithException: {}",
                                exception, this.lastRetryWithException);
                        exceptionToThrow = this.lastRetryWithException;
                    } else {
                        logger.warn(
                                "Received partition key range gone exception after backoff/retry. Will fail the request. {}",
                                exception.toString());
                        exceptionToThrow = BridgeInternal.createServiceUnavailableException(exception);
                    }
                } else if (exception instanceof InvalidPartitionException) {
                    if (this.lastRetryWithException != null) {
                        logger.warn(
                                "Received InvalidPartitionException after backoff/retry including at least one RetryWithException. "
                                        + "Will fail the request with RetryWithException. InvalidPartitionException: {}. RetryWithException: {}",
                                exception, this.lastRetryWithException);
                    } else {
                        logger.warn(
                                "Received invalid collection partition exception after backoff/retry. Will fail the request. {}",
                                exception.toString());
                        exceptionToThrow = BridgeInternal.createServiceUnavailableException(exception);
                    }
                } else {
                    logger.warn("Received retrywith exception after backoff/retry. Will fail the request. {}",
                            exception.toString());
                }
                stopStopWatch(this.durationTimer);
                return Mono.just(ShouldRetryResult.error(exceptionToThrow));
            }
            backoffTime = Duration.ofSeconds(Math.min(Math.min(this.currentBackoffSeconds, remainingSeconds),
                    GoneAndRetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_SECONDS));
            this.currentBackoffSeconds *= GoneAndRetryWithRetryPolicy.BACK_OFF_MULTIPLIER;
            logger.debug("BackoffTime: {} seconds.", backoffTime.getSeconds());
        }

        // Calculate the remaining time based after accounting for the backoff that we
        // will perform
        long timeoutInMillSec = remainingSeconds*1000 - backoffTime.toMillis();
        timeout = timeoutInMillSec > 0 ? Duration.ofMillis(timeoutInMillSec)
                : Duration.ofSeconds(GoneAndRetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_SECONDS);
        if (exception instanceof GoneException) {
            logger.debug("Received gone exception, will retry, {}", exception.toString());
            forceRefreshAddressCache = true; // indicate we are in retry.
        } else if (exception instanceof PartitionIsMigratingException) {
            logger.warn("Received PartitionIsMigratingException, will retry, {}", exception.toString());
            this.request.forceCollectionRoutingMapRefresh = true;
            forceRefreshAddressCache = true;
        } else if (exception instanceof InvalidPartitionException) {
            this.request.requestContext.quorumSelectedLSN = -1;
            this.request.requestContext.resolvedPartitionKeyRange = null;
            this.request.requestContext.quorumSelectedStoreResponse = null;
            this.request.requestContext.globalCommittedSelectedLSN = -1;
            if (this.attemptCountInvalidPartition++ > 2) {
                // for second InvalidPartitionException, stop retrying.
                logger.warn("Received second InvalidPartitionException after backoff/retry. Will fail the request. {}",
                        exception.toString());
                return Mono.just(ShouldRetryResult
                        .error(BridgeInternal.createServiceUnavailableException(exception)));
            }

            if (this.request != null) {
                logger.warn("Received invalid collection exception, will retry, {}", exception.toString());
                this.request.forceNameCacheRefresh = true;
            } else {
                logger.error("Received unexpected invalid collection exception, request should be non-null.",
                        exception);
                return Mono.just(ShouldRetryResult
                        .error(BridgeInternal.createCosmosException(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, exception)));
            }
            forceRefreshAddressCache = false;
        } else if (exception instanceof PartitionKeyRangeIsSplittingException) {
            this.request.requestContext.resolvedPartitionKeyRange = null;
            this.request.requestContext.quorumSelectedLSN = -1;
            this.request.requestContext.quorumSelectedStoreResponse = null;
            logger.info("Received partition key range splitting exception, will retry, {}", exception.toString());
            this.request.forcePartitionKeyRangeRefresh = true;
            forceRefreshAddressCache = false;
        } else {
            logger.warn("Received retrywith exception, will retry, {}", exception);
            // For RetryWithException, prevent the caller
            // from refreshing any caches.
            forceRefreshAddressCache = false;
        }
        return Mono.just(ShouldRetryResult.retryAfter(backoffTime,
                Quadruple.with(forceRefreshAddressCache, true, timeout, currentRetryAttemptCount)));
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
