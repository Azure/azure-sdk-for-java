// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.implementation.PollContextRequiredException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * INTERNAL PACKAGE PRIVATE CLASS
 *
 * Default implementation of {@link SyncPoller} that uses blocking reactor call underneath.
 * The DefaultSyncPoller is not thread safe but we make every attempt to be safe in cases
 * it is possible to be so, e.g. by using volatile and copying context.
 *
 * @param <T> The type of poll response value
 * @param <U> The type of the final result of the long running operation
 */
final class SyncOverAsyncPoller<T, U> implements SyncPoller<T, U> {
    private static final ClientLogger LOGGER = new ClientLogger(SyncOverAsyncPoller.class);
    private final Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation;
    private final BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation;
    private final Function<PollingContext<T>, Mono<U>> fetchResultOperation;
    private final PollResponse<T> activationResponse;
    private final PollingContext<T> pollingContext = new PollingContext<>();
    private volatile PollingContext<T> terminalPollContext;
    private volatile Duration pollInterval;

    /**
     * Creates DefaultSyncPoller.
     *
     * @param pollInterval the polling interval.
     * @param syncActivationOperation the operation to synchronously activate (start) the long running operation,
     *     this operation will be called with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long running operation, this parameter
     *     is required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long running operation
     *     if service supports cancellation, this parameter is required and if service does not support cancellation
     *     then the implementer should return Mono.error with an error message indicating absence of cancellation
     *     support, the operation will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of
     *     the long running operation if service support it, this parameter is required and operation will be called
     *     current {@link PollingContext}, if service does not have an api to fetch final result and if final result
     *     is same as final poll response value then implementer can choose to simply return value from provided
     *     final poll response.
     */
    SyncOverAsyncPoller(Duration pollInterval,
                        Function<PollingContext<T>, PollResponse<T>> syncActivationOperation,
                        Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
                        BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
                        Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        Objects.requireNonNull(syncActivationOperation, "'syncActivationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
            "'fetchResultOperation' cannot be null.");
        this.activationResponse = syncActivationOperation.apply(this.pollingContext);
        //
        this.pollingContext.setOnetimeActivationResponse(this.activationResponse);
        this.pollingContext.setLatestResponse(this.activationResponse);
        if (this.activationResponse.getStatus().isComplete()) {
            this.terminalPollContext = this.pollingContext;
        }
    }

    @Override
    public PollResponse<T> poll() {
        PollResponse<T> response = this.pollOperation
            .apply(this.pollingContext)
            .block();
        this.pollingContext.setLatestResponse(response);
        if (response.getStatus().isComplete()) {
            this.terminalPollContext = this.pollingContext.copy();
        }
        return response;
    }

    @Override
    public PollResponse<T> waitForCompletion() {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            AsyncPollResponse<T, U> finalAsyncPollResponse = PollingUtil
                .pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
                    .blockLast();
            PollResponse<T> response = PollingUtil.toPollResponse(finalAsyncPollResponse);
            this.terminalPollContext = context;
            return response;
        }
    }

    @Override
    public PollResponse<T> waitForCompletion(Duration timeout) {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            AsyncPollResponse<T, U> finalAsyncPollResponse = PollingUtil
                .pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
                    .timeout(timeout)
                    .blockLast();
            PollResponse<T> response = PollingUtil.toPollResponse(finalAsyncPollResponse);
            this.terminalPollContext = context;
            return response;
        }
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor) {
        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null
                && currentTerminalPollContext.getLatestResponse().getStatus() == statusToWaitFor) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            AsyncPollResponse<T, U> asyncPollResponse = PollingUtil
                .pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
                .takeUntil(apr -> PollingUtil.matchStatus(apr, statusToWaitFor))
                .last()
                .switchIfEmpty(Mono.error(() -> new NoSuchElementException(
                    "Polling completed without receiving the given status '" + statusToWaitFor + "'.")))
                .block();
            PollResponse<T> response = PollingUtil.toPollResponse(asyncPollResponse);
            if (response.getStatus().isComplete()) {
                this.terminalPollContext = context;
            }
            return response;
        }
    }

    @Override
    public PollResponse<T> waitUntil(Duration timeout, LongRunningOperationStatus statusToWaitFor) {
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        if (timeout.isNegative() || timeout.isZero()) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for timeout is not allowed."));
        }
        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null
                && currentTerminalPollContext.getLatestResponse().getStatus() == statusToWaitFor) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            AsyncPollResponse<T, U> asyncPollResponse = PollingUtil
                    .pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
                    .takeUntil(apr -> PollingUtil.matchStatus(apr, statusToWaitFor))
                    .last()
                    .timeout(timeout)
                    .switchIfEmpty(Mono.error(() -> new NoSuchElementException(
                        "Polling completed without receiving the given status '" + statusToWaitFor + "'.")))
                    .block();
            PollResponse<T> response = PollingUtil.toPollResponse(asyncPollResponse);
            if (response.getStatus().isComplete()) {
                this.terminalPollContext = context;
            }
            return response;
        }
    }

    @Override
    public U getFinalResult() {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            return this.fetchResultOperation
                .apply(currentTerminalPollContext)
                .block();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            AsyncPollResponse<T, U> finalAsyncPollResponse = PollingUtil
                .pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
                    .blockLast();
            this.terminalPollContext = context;
            return finalAsyncPollResponse.getFinalResult().block();
        }
    }

    @Override
    public void cancelOperation() {
        PollingContext<T> context1 = this.pollingContext.copy();
        if (context1.getActivationResponse() == context1.getLatestResponse()) {
            this.cancelOperation.apply(context1, context1.getActivationResponse())
                .block();
        } else {
            try {
                this.cancelOperation.apply(null, this.activationResponse).block();
            } catch (PollContextRequiredException crp) {
                PollingContext<T> context2 = this.pollingContext.copy();
                PollingUtil.pollingLoopAsync(context2, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
                    .next()
                    .block();
                this.cancelOperation
                    .apply(context2, this.activationResponse)
                    .block();
            }
        }
    }

    @Override
    public SyncPoller<T, U> setPollInterval(Duration pollInterval) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'pollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        return this;
    }
}
