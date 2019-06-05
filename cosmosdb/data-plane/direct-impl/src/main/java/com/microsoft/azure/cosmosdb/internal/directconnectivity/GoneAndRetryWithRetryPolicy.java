/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import java.time.Duration;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.internal.Quadruple;
import com.microsoft.azure.cosmosdb.rx.internal.IDocumentClientRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.IRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.InvalidPartitionException;
import com.microsoft.azure.cosmosdb.rx.internal.PartitionIsMigratingException;
import com.microsoft.azure.cosmosdb.rx.internal.PartitionKeyRangeIsSplittingException;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;

import rx.Single;

public class GoneAndRetryWithRetryPolicy implements IRetryPolicy {

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
    public static Quadruple<Boolean, Boolean, Duration, Integer> INITIAL_ARGUMENT_VALUE_POLICY_ARG = Quadruple.with(false, false,
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
    public Single<ShouldRetryResult> shouldRetry(Exception exception) {
        DocumentClientException exceptionToThrow = null;
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
            logger.debug("Operation will NOT be retried. Current attempt {}, Exception: {} ", this.attemptCount,
                    exception);
            stopStopWatch(this.durationTimer);
            return Single.just(ShouldRetryResult.noRetry());
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
                        exceptionToThrow = new DocumentClientException(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                                exception);
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
                        exceptionToThrow = new DocumentClientException(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                                exception);
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
                        exceptionToThrow = new DocumentClientException(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE,
                                exception);
                    }
                } else {
                    logger.warn("Received retrywith exception after backoff/retry. Will fail the request. {}",
                            exception.toString());
                }
                stopStopWatch(this.durationTimer);
                return Single.just(ShouldRetryResult.error(exceptionToThrow));
            }
            backoffTime = Duration.ofSeconds(Math.min(Math.min(this.currentBackoffSeconds, remainingSeconds),
                    GoneAndRetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_SECONDS));
            this.currentBackoffSeconds *= GoneAndRetryWithRetryPolicy.BACK_OFF_MULTIPLIER;
            logger.info("BackoffTime: {} seconds.", backoffTime.getSeconds());
        }

        // Calculate the remaining time based after accounting for the backoff that we
        // will perform
        long timeoutInMillSec = remainingSeconds*1000 - backoffTime.toMillis();
        timeout = timeoutInMillSec > 0 ? Duration.ofMillis(timeoutInMillSec)
                : Duration.ofSeconds(GoneAndRetryWithRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_SECONDS);
        if (exception instanceof GoneException) {
            logger.warn("Received gone exception, will retry, {}", exception.toString());
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
                return Single.just(ShouldRetryResult
                        .error(new DocumentClientException(HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, exception)));
            }

            if (this.request != null) {
                logger.warn("Received invalid collection exception, will retry, {}", exception.toString());
                this.request.forceNameCacheRefresh = true;
            } else {
                logger.error("Received unexpected invalid collection exception, request should be non-null.",
                        exception);
                return Single.just(ShouldRetryResult
                        .error(new DocumentClientException(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR, exception)));
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
        return Single.just(ShouldRetryResult.retryAfter(backoffTime,
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
