// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.util.SharedExecutorService;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.polling.implementation.PollingUtils.getAbsolutePath;

/**
 * Utility class for Polling APIs.
 */
class PollingUtil {
    private static final ClientLogger LOGGER = new ClientLogger(PollingUtil.class);

    static <T> PollResponse<T> pollingLoop(PollingContext<T> pollingContext, Duration timeout,
        LongRunningOperationStatus statusToWaitFor, Function<PollingContext<T>, PollResponse<T>> pollOperation,
        Duration pollInterval, boolean isWaitForStatus) {
        boolean timeBound = timeout != null;
        long timeoutInMillis = timeBound ? timeout.toMillis() : -1;
        long startTime = System.currentTimeMillis();
        PollResponse<T> intermediatePollResponse = pollingContext.getLatestResponse();

        boolean firstPoll = true;
        while (!intermediatePollResponse.getStatus().isComplete()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (timeBound && elapsedTime >= timeoutInMillis) {
                if (intermediatePollResponse.getStatus().equals(statusToWaitFor) || isWaitForStatus) {
                    return intermediatePollResponse;
                } else {
                    throw LOGGER.logExceptionAsError(new RuntimeException(
                        new TimeoutException("Polling didn't complete before the timeout period.")));
                }
            }

            if (intermediatePollResponse.getStatus().equals(statusToWaitFor)) {
                return intermediatePollResponse;
            }

            final Future<PollResponse<T>> pollOp;
            if (firstPoll) {
                firstPoll = false;
                pollOp = SharedExecutorService.getInstance().submit(() -> pollOperation.apply(pollingContext));
            } else {
                final PollResponse<T> finalIntermediatePollResponse = intermediatePollResponse;
                pollOp = SharedExecutorService.getInstance().submit(() -> {
                    Thread.sleep(getDelay(finalIntermediatePollResponse, pollInterval).toMillis());
                    return pollOperation.apply(pollingContext);
                });
            }

            try {
                long pollTimeout = timeBound ? timeoutInMillis - elapsedTime : -1;
                intermediatePollResponse = ImplUtils.getResultWithTimeout(pollOp, pollTimeout);
                pollingContext.setLatestResponse(intermediatePollResponse);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                // waitUntil should not throw when timeout is reached.
                if (isWaitForStatus) {
                    return intermediatePollResponse;
                }
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        return intermediatePollResponse;
    }

    static <T, U> Flux<AsyncPollResponse<T, U>> pollingLoopAsync(PollingContext<T> pollingContext,
        Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
        BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
        Function<PollingContext<T>, Mono<U>> fetchResultOperation, Duration pollInterval) {
        return Flux.using(
            // Create a Polling Context per subscription
            () -> pollingContext,
            // Do polling
            // set|read to|from context as needed, reactor guarantee thread-safety of cxt object.
            cxt -> Mono.defer(() -> pollOperation.apply(cxt))
                .delaySubscription(getDelay(cxt.getLatestResponse(), pollInterval))
                .switchIfEmpty(Mono.error(() -> new IllegalStateException("PollOperation returned Mono.empty().")))
                .repeat()
                .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
                .concatMap(currentPollResponse -> {
                    cxt.setLatestResponse(currentPollResponse);
                    return Mono.just(new AsyncPollResponse<>(cxt, cancelOperation, fetchResultOperation));
                }),
            cxt -> {
            });
    }

    /**
     * Get the duration to wait before making next poll attempt.
     *
     * @param pollResponse the poll response to retrieve delay duration from
     * @return the delay
     */
    private static <T> Duration getDelay(PollResponse<T> pollResponse, Duration pollInterval) {
        Duration retryAfter = pollResponse.getRetryAfter();
        if (retryAfter == null) {
            return pollInterval;
        } else {
            return retryAfter.compareTo(Duration.ZERO) > 0 ? retryAfter : pollInterval;
        }
    }

    static <T, U> PollResponse<T> toPollResponse(AsyncPollResponse<T, U> asyncPollResponse) {
        return new PollResponse<>(asyncPollResponse.getStatus(), asyncPollResponse.getValue(),
            asyncPollResponse.getRetryAfter());
    }

    static boolean matchStatus(AsyncPollResponse<?, ?> currentPollResponse,
        LongRunningOperationStatus statusToWaitFor) {
        if (currentPollResponse == null || statusToWaitFor == null) {
            return false;
        }

        return statusToWaitFor == currentPollResponse.getStatus();
    }

    /**
     * Determines if the location can poll.
     * <p>
     * Shared functionality for {@link LocationPollingStrategy} and {@link SyncLocationPollingStrategy}.
     *
     * @param initialResponse The initial response.
     * @param endpoint The endpoint.
     * @param logger The logger.
     * @return Whether the location can poll.
     */
    static boolean locationCanPoll(Response<?> initialResponse, String endpoint, ClientLogger logger) {
        HttpHeader locationHeader = initialResponse.getHeaders().get(HttpHeaderName.LOCATION);

        if (locationHeader != null) {
            try {
                ImplUtils.createUrl(getAbsolutePath(locationHeader.getValue(), endpoint, logger));
                return true;
            } catch (MalformedURLException e) {
                logger.info("Failed to parse Location header into a URL.", e);
                return false;
            }
        }

        return false;
    }

    /**
     * Determines if the operation resource can poll.
     * <p>
     * Shared functionality for {@link OperationResourcePollingStrategy} and
     * {@link SyncOperationResourcePollingStrategy}.
     *
     * @param initialResponse The initial response.
     * @param operationLocationHeader The operation location header.
     * @param endpoint The endpoint.
     * @param logger The logger.
     * @return Whether the operation resource can poll.
     */
    static boolean operationResourceCanPoll(Response<?> initialResponse, HttpHeaderName operationLocationHeader,
        String endpoint, ClientLogger logger) {
        HttpHeader header = initialResponse.getHeaders().get(operationLocationHeader);

        if (header != null) {
            try {
                ImplUtils.createUrl(getAbsolutePath(header.getValue(), endpoint, logger));
                return true;
            } catch (MalformedURLException e) {
                return false;
            }
        }

        return false;
    }

    /**
     * Validates the timeout.
     *
     * @param timeout The timeout.
     * @param logger The logger.
     * @throws NullPointerException if {@code timeout} is null.
     * @throws IllegalArgumentException if {@code timeout} is negative or zero.
     */
    static void validateTimeout(Duration timeout, ClientLogger logger) {
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        if (timeout.isNegative() || timeout.isZero()) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException("Negative or zero value for timeout is not allowed."));
        }
    }

    /**
     * Validates the poll interval.
     *
     * @param pollInterval The poll interval.
     * @param logger The logger.
     * @return The poll interval.
     * @throws NullPointerException if {@code pollInterval} is null.
     * @throws IllegalArgumentException if {@code pollInterval} is negative or zero.
     */
    static Duration validatePollInterval(Duration pollInterval, ClientLogger logger) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw logger.logExceptionAsWarning(
                new IllegalArgumentException("Negative or zero value for pollInterval is not allowed."));
        }
        return pollInterval;
    }
}
