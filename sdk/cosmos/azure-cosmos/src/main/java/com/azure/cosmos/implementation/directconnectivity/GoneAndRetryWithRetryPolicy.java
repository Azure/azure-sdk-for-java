// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.Quadruple;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GoneAndRetryWithRetryPolicy implements IRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(GoneAndRetryWithRetryPolicy.class);
    private final GoneRetryPolicy goneRetryPolicy;
    private final RetryWithRetryPolicy retryWithRetryPolicy;
    private final Instant start;
    private volatile Instant end;

    private volatile RetryWithException lastRetryWithException;
    private RetryContext retryContext;

    public GoneAndRetryWithRetryPolicy(RxDocumentServiceRequest request, Integer waitTimeInSeconds) {
        this.retryContext = BridgeInternal.getRetryContext(request.requestContext.cosmosDiagnostics);
        this.goneRetryPolicy = new GoneRetryPolicy(
            request,
            waitTimeInSeconds,
            this.retryContext
        );
        this.retryWithRetryPolicy = new RetryWithRetryPolicy(
            waitTimeInSeconds, this.retryContext);
        this.start = Instant.now();
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {

        return this.retryWithRetryPolicy.shouldRetry(exception)
                                        .flatMap((retryWithResult) -> {

            if (retryWithResult.shouldRetry) {
                return Mono.just(retryWithResult);
            }

            return this.goneRetryPolicy.shouldRetry(exception)
                .flatMap((goneRetryResult) -> {
                    if (!goneRetryResult.shouldRetry) {
                        logger.debug("Operation will NOT be retried. Exception:",
                            exception);
                        this.end = Instant.now();
                    }

                    return Mono.just(goneRetryResult);
                });
        });
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }

    private Duration getElapsedTime() {
        Instant endSnapshot = this.end != null ? this.end : Instant.now();

        return Duration.between(this.start, endSnapshot);
    }

    class GoneRetryPolicy implements IRetryPolicy {
        private final static int DEFAULT_WAIT_TIME_IN_SECONDS = 30;
        private final static int MAXIMUM_BACKOFF_TIME_IN_SECONDS = 15;
        private final static int INITIAL_BACKOFF_TIME = 1;
        private final static int BACK_OFF_MULTIPLIER = 2;

        private final RxDocumentServiceRequest request;
        private volatile int attemptCount = 1;
        private volatile int attemptCountInvalidPartition = 1;
        private volatile int currentBackoffSeconds = GoneRetryPolicy.INITIAL_BACKOFF_TIME;
        private final int waitTimeInSeconds;
        private RetryContext retryContext;

        public GoneRetryPolicy(
            RxDocumentServiceRequest request,
            Integer waitTimeInSeconds,
            RetryContext retryContext) {

            checkNotNull(request, "request must not be null.");
            this.request = request;
            this.waitTimeInSeconds = waitTimeInSeconds != null ? waitTimeInSeconds : DEFAULT_WAIT_TIME_IN_SECONDS;
            this.retryContext = retryContext;
        }

        private boolean isNonRetryableException(Exception exception) {
            if (exception instanceof GoneException ||
                exception instanceof RetryWithException ||
                exception instanceof PartitionIsMigratingException ||
                exception instanceof PartitionKeyRangeIsSplittingException) {

                return false;
            }

            if (exception instanceof InvalidPartitionException) {
                return this.request.getPartitionKeyRangeIdentity() != null &&
                    this.request.getPartitionKeyRangeIdentity().getCollectionRid() != null;
            }

            return true;
        }

        private CosmosException logAndWrapExceptionWithLastRetryWithException(Exception exception) {
            String exceptionType;
            if (exception instanceof GoneException) {
                exceptionType = "GoneException";
            } else if (exception instanceof PartitionKeyRangeGoneException) {
                exceptionType = "PartitionKeyRangeGoneException";
            } else if (exception instanceof  InvalidPartitionException) {
                exceptionType = "InvalidPartitionException";
            } else if (exception instanceof  PartitionKeyRangeIsSplittingException) {
                exceptionType = "PartitionKeyRangeIsSplittingException";
            } else if (exception instanceof CosmosException) {
                logger.warn("Received CosmosException after backoff/retry. Will fail the request.",
                    exception);

                return (CosmosException)exception;
            } else {
                throw new IllegalStateException("Invalid exception type", exception);
            }

            RetryWithException lastRetryWithExceptionSnapshot =
                GoneAndRetryWithRetryPolicy.this.lastRetryWithException;
            if (lastRetryWithExceptionSnapshot != null) {
                logger.warn(
                    "Received {} after backoff/retry including at least one RetryWithException. "
                        + "Will fail the request with RetryWithException. {}: {}. RetryWithException: {}",
                    exceptionType,
                    exceptionType,
                    exception,
                    lastRetryWithExceptionSnapshot);

                return lastRetryWithExceptionSnapshot;
            }

            logger.warn(
                "Received {} after backoff/retry. Will fail the request. {}",
                exceptionType,
                exception);
            return BridgeInternal.createServiceUnavailableException(exception);
        }

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
            CosmosException exceptionToThrow;
            Duration backoffTime = Duration.ofSeconds(0);
            Duration timeout;
            boolean forceRefreshAddressCache;
            if (isNonRetryableException(exception)) {

                logger.debug("Operation will NOT be retried. Current attempt {}, Exception: ", this.attemptCount,
                    exception);
                return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
            } else if (exception instanceof GoneException &&
                !request.isReadOnly() &&
                BridgeInternal.hasSendingRequestStarted((CosmosException)exception) &&
                !((GoneException)exception).isBasedOn410ResponseFromService()) {

                logger.warn(
                    "Operation will NOT be retried. Write operations which failed due to transient transport errors " +
                        "can not be retried safely when sending the request " +
                        "to the service because they aren't idempotent. Current attempt {}, Exception: ",
                    this.attemptCount,
                    exception);

                return Mono.just(ShouldRetryResult.noRetry(
                    Quadruple.with(true, true, Duration.ofMillis(0), this.attemptCount)));
            }

            long remainingSeconds = this.waitTimeInSeconds -
                GoneAndRetryWithRetryPolicy.this.getElapsedTime().toMillis() / 1_000L;
            int currentRetryAttemptCount = this.attemptCount;
            if (this.attemptCount++ > 1) {
                if (remainingSeconds <= 0) {
                    exceptionToThrow = logAndWrapExceptionWithLastRetryWithException(exception);
                    return Mono.just(ShouldRetryResult.error(exceptionToThrow));
                }

                backoffTime = Duration.ofSeconds(Math.min(Math.min(this.currentBackoffSeconds, remainingSeconds),
                    GoneRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_SECONDS));
                this.currentBackoffSeconds *= GoneRetryPolicy.BACK_OFF_MULTIPLIER;
                logger.debug("BackoffTime: {} seconds.", backoffTime.getSeconds());
            }

            // Calculate the remaining time based after accounting for the backoff that we
            // will perform
            long timeoutInMillSec = remainingSeconds*1000 - backoffTime.toMillis();
            timeout = timeoutInMillSec > 0 ? Duration.ofMillis(timeoutInMillSec)
                : Duration.ofSeconds(GoneRetryPolicy.MAXIMUM_BACKOFF_TIME_IN_SECONDS);
            logger.debug("Timeout. {} - BackoffTime {} - currentBackoffSeconds {} - CurrentRetryAttemptCount {}",
                timeout.toMillis(),
                backoffTime,
                this.currentBackoffSeconds,
                currentRetryAttemptCount);

            Pair<Mono<ShouldRetryResult>, Boolean> exceptionHandlingResult = handleException(exception);
            Mono<ShouldRetryResult> result = exceptionHandlingResult.getLeft();
            if (result != null) {
                return result;
            }

            forceRefreshAddressCache = exceptionHandlingResult.getRight();

            return Mono.just(ShouldRetryResult.retryAfter(backoffTime,
                Quadruple.with(forceRefreshAddressCache, true, timeout, currentRetryAttemptCount)));
        }

        @Override
        public RetryContext getRetryContext() {
            return this.retryContext;
        }

        private Pair<Mono<ShouldRetryResult>, Boolean> handleException(Exception exception) {
            if (exception instanceof GoneException) {
                return handleGoneException((GoneException)exception);
            } else if (exception instanceof PartitionIsMigratingException) {
                return handlePartitionIsMigratingException((PartitionIsMigratingException)exception);
            } else if (exception instanceof InvalidPartitionException) {
                return handleInvalidPartitionException((InvalidPartitionException)exception);
            } else if (exception instanceof PartitionKeyRangeIsSplittingException) {
                return handlePartitionKeyIsSplittingException((PartitionKeyRangeIsSplittingException) exception);
            }

            throw new IllegalStateException("Invalid exception type", exception);
        }

        private Pair<Mono<ShouldRetryResult>, Boolean> handleGoneException(GoneException exception) {
            logger.debug("Received gone exception, will retry, {}", exception.toString());
            return Pair.of(null, true); // indicate we are in retry.
        }

        private Pair<Mono<ShouldRetryResult>, Boolean> handlePartitionIsMigratingException(PartitionIsMigratingException exception) {
            logger.debug("Received PartitionIsMigratingException, will retry, {}", exception.toString());
            this.request.forceCollectionRoutingMapRefresh = true;
            return Pair.of(null, true);
        }

        private Pair<Mono<ShouldRetryResult>, Boolean> handlePartitionKeyIsSplittingException(PartitionKeyRangeIsSplittingException exception) {
            this.request.requestContext.resolvedPartitionKeyRange = null;
            this.request.requestContext.quorumSelectedLSN = -1;
            this.request.requestContext.quorumSelectedStoreResponse = null;
            logger.debug("Received partition key range splitting exception, will retry, {}", exception.toString());
            this.request.forcePartitionKeyRangeRefresh = true;
            return Pair.of(null, false);
        }

        private Pair<Mono<ShouldRetryResult>, Boolean> handleInvalidPartitionException(InvalidPartitionException exception) {
            this.request.requestContext.quorumSelectedLSN = -1;
            this.request.requestContext.resolvedPartitionKeyRange = null;
            this.request.requestContext.quorumSelectedStoreResponse = null;
            this.request.requestContext.globalCommittedSelectedLSN = -1;
            if (this.attemptCountInvalidPartition++ > 2) {
                // for second InvalidPartitionException, stop retrying.
                logger.warn("Received second InvalidPartitionException after backoff/retry. Will fail the request. {}",
                    exception.toString());
                return Pair.of(
                    Mono.just(ShouldRetryResult.error(BridgeInternal.createServiceUnavailableException(exception))),
                    false);
            }

            logger.debug("Received invalid collection exception, will retry, {}", exception.toString());
            this.request.forceNameCacheRefresh = true;

            return Pair.of(null, false);
        }
    }

    class RetryWithRetryPolicy implements IRetryPolicy {
        private final static int DEFAULT_WAIT_TIME_IN_SECONDS = 30;
        private final static int MAXIMUM_BACKOFF_TIME_IN_MS = 15000;
        private final static int INITIAL_BACKOFF_TIME_MS = 10;
        private final static int BACK_OFF_MULTIPLIER = 2;

        private volatile int attemptCount = 1;
        private volatile int currentBackoffMilliseconds = RetryWithRetryPolicy.INITIAL_BACKOFF_TIME_MS;

        private final int waitTimeInSeconds;
        private RetryContext retryContext;


        public RetryWithRetryPolicy(Integer waitTimeInSeconds, RetryContext retryContext) {
            this.waitTimeInSeconds = waitTimeInSeconds != null ? waitTimeInSeconds : DEFAULT_WAIT_TIME_IN_SECONDS;
            this.retryContext = retryContext;
        }

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
            Duration backoffTime;
            Duration timeout;

            if (!(exception instanceof RetryWithException)) {
                logger.debug("Operation will NOT be retried. Current attempt {}, Exception: ", this.attemptCount,
                    exception);
                return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
            }

            RetryWithException lastRetryWithException = (RetryWithException)exception;
            GoneAndRetryWithRetryPolicy.this.lastRetryWithException = lastRetryWithException;

            long remainingMilliseconds =
                (this.waitTimeInSeconds * 1_000L) -
                    GoneAndRetryWithRetryPolicy.this.getElapsedTime().toMillis();
            int currentRetryAttemptCount = this.attemptCount++;

            if (remainingMilliseconds <= 0) {
                logger.warn("Received RetryWithException after backoff/retry. Will fail the request.",
                    lastRetryWithException);
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

            logger.debug("Received RetryWithException, will retry, ", exception);

            // For RetryWithException, prevent the caller
            // from refreshing any caches.
            return Mono.just(ShouldRetryResult.retryAfter(backoffTime,
                Quadruple.with(false, true, timeout, currentRetryAttemptCount)));
        }

        @Override
        public RetryContext getRetryContext() {
            return this.retryContext;
        }
    }
}
