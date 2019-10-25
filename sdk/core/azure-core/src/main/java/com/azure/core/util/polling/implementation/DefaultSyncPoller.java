// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * INTERNAL CLASS.
 *
 * Default implementation of {@link SyncPoller} that uses {@link PollerFlux} underneath.
 *
 * @param <T> The type of poll response value
 * @param <U> The type of the final result of long-running operation
 */
public class DefaultSyncPoller<T, U> implements SyncPoller<T, U> {
    private final ClientLogger logger = new ClientLogger(DefaultSyncPoller.class);
    //
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<PollResponse<T>>> pollOperation;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<T>> cancelOperation;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<U>> fetchResultOperation;
    //
    private final PollerFlux<T, U> pollerFlux;
    private final PollResponse<T> activationResponse;
    private volatile PollResponse<T> lastResponse;
    private volatile PollResponse<T> terminalPollResponse;

    /**
     * Creates DefaultSyncPoller.
     *
     * @param defaultPollInterval the default polling interval
     * @param activationOperation the activation operation to be invoked at most once across all subscriptions,
     *                            this parameter is required, if there is no specific activation work to be
     *                            done then invocation should return Mono.empty().
     * @param pollOperation the operation to poll the current state of long running operation, this parameter
     *                      is required and the operation will be called with the activation {@link PollResponse}
     *                      and last {@link PollResponse}.
     * @param cancelOperation a {@link BiFunction} that represents the operation to cancel the long-running operation
     *                        if service supports cancellation, this parameter is required and if service does not
     *                        support cancellation then the implementer should return Mono.error with an error message
     *                        indicating absence of cancellation support, the operation will be called with the
     *                        activation {@link PollResponse} and latest {@link PollResponse}.
     * @param fetchResultOperation a {@link BiFunction} that represents the  operation to retrieve final result of
     *                             the long-running operation if service support it, this parameter is required and
     *                             operation will be called with the activation {@link PollResponse} and final
     *                             {@link PollResponse}, if service does not have an api to fetch final result and
     *                             if final result is same as final poll response value then implementer can choose
     *                             to simply return value from provided final poll response.
     */
    public DefaultSyncPoller(Duration defaultPollInterval,
                             Supplier<Mono<T>> activationOperation,
                             BiFunction<PollResponse<T>, PollResponse<T>, Mono<PollResponse<T>>> pollOperation,
                             BiFunction<PollResponse<T>, PollResponse<T>, Mono<T>> cancelOperation,
                             BiFunction<PollResponse<T>, PollResponse<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        if (defaultPollInterval.compareTo(Duration.ZERO) <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        Objects.requireNonNull(activationOperation, "'activationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
            "'fetchResultOperation' cannot be null.");
        this.activationResponse = new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
            activationOperation.get().block());
        this.lastResponse = this.activationResponse;
        this.pollerFlux = new PollerFlux<>(defaultPollInterval,
            () -> Mono.empty(),
            pollOperation,
            cancelOperation,
            fetchResultOperation);
    }

    @Override
    public PollResponse<T> poll() {
        PollResponse<T> currentLastResponse = this.lastResponse;
        PollResponse<T> response = this.pollOperation
            .apply(this.activationResponse, currentLastResponse)
            .block();
        if (response == null) {
            logger.error("PollOperation returned empty publisher.");
        }
        if (response.getStatus().isComplete()) {
            this.terminalPollResponse = response;
        }
        this.lastResponse = response;
        return response;
    }

    @Override
    public PollResponse<T> waitForCompletion() {
        AsyncPollResponse<T, U> finalAsyncPollResponse = this.pollerFlux
            .blockLast();
        if (finalAsyncPollResponse == null) {
            logger.error("PollerFlux completed without emitting any event.");
        }
        PollResponse<T> response = toPollResponse(finalAsyncPollResponse);
        this.terminalPollResponse = response;
        return response;
    }

    @Override
    public PollResponse<T> waitForCompletion(Duration timeout) {
        AsyncPollResponse<T, U> finalAsyncPollResponse = this.pollerFlux
            .timeout(timeout)
            .last()
            .block();
        PollResponse<T> response = toPollResponse(finalAsyncPollResponse);
        this.terminalPollResponse = response;
        return response;
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor) {
        if (statusToWaitFor == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for status is not allowed."));
        }
        AsyncPollResponse<T, U> asyncPollResponse = this.pollerFlux
            .takeUntil(apr -> matchStatus(apr, statusToWaitFor))
            .last()
            .switchIfEmpty(Mono.error(new NoSuchElementException("Polling completed without receiving the given status "
                    + "'" + statusToWaitFor + "'.")))
            .block();
        PollResponse<T> response = toPollResponse(asyncPollResponse);
        if (response.getStatus().isComplete()) {
            this.terminalPollResponse = response;
        }
        return response;
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor, Duration timeout) {
        if (statusToWaitFor == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for status is not allowed."));
        }
        if (timeout == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for timeout is not allowed."));
        }
        if (timeout.toNanos() <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for timeout is not allowed."));
        }
        AsyncPollResponse<T, U> asyncPollResponse = this.pollerFlux
            .takeUntil(apr -> matchStatus(apr, statusToWaitFor))
            .last()
            .timeout(timeout)
            .switchIfEmpty(Mono.error(new NoSuchElementException("Polling completed without receiving the given status "
                    +  "'" + statusToWaitFor + "'.")))
            .block();
        PollResponse<T> response = toPollResponse(asyncPollResponse);
        if (response.getStatus().isComplete()) {
            this.terminalPollResponse = response;
        }
        return response;
    }

    @Override
    public U getFinalResult() {
        PollResponse<T> currentTerminalResponse = this.terminalPollResponse;
        if (currentTerminalResponse != null) {
            return this.fetchResultOperation
                .apply(this.activationResponse, currentTerminalResponse)
                .block();
        } else {
            AsyncPollResponse<T, U> asyncPollResponse = this.pollerFlux
                .blockLast();
            this.terminalPollResponse = toPollResponse(asyncPollResponse);
            return asyncPollResponse
                .getFinalResult()
                .block();
        }
    }

    @Override
    public void cancelOperation() {
        PollResponse<T> currentLastResponse = this.lastResponse;
        if (currentLastResponse == this.activationResponse) {
            this.cancelOperation.apply(this.activationResponse, this.activationResponse)
                .block();
        } else {
            try {
                this.cancelOperation.apply(this.activationResponse, null).block();
            } catch (OperationRequirePollResponse crp) {
                AsyncPollResponse<T, U> asyncPollResponse = this.pollerFlux
                    .next() // Do one poll
                    .block();
                this.cancelOperation
                    .apply(this.activationResponse, toPollResponse(asyncPollResponse))
                    .block();
            }
        }
    }

    private static <T, U> PollResponse<T> toPollResponse(AsyncPollResponse<T, U> asyncPollResponse) {
        return new PollResponse<>(asyncPollResponse.getStatus(),
            asyncPollResponse.getValue(),
            asyncPollResponse.getRetryAfter(),
            asyncPollResponse.getProperties());
    }

    private boolean matchStatus(AsyncPollResponse<T, U> currentPollResponse,
                                LongRunningOperationStatus statusToWaitFor) {
        if (currentPollResponse == null || statusToWaitFor == null) {
            return false;
        }
        if (statusToWaitFor == currentPollResponse.getStatus()) {
            return true;
        }
        return false;
    }
}
