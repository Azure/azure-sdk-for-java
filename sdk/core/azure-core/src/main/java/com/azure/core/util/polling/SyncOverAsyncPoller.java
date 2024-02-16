// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.implementation.PollContextRequiredException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.polling.PollingUtil.validatePollInterval;
import static com.azure.core.util.polling.PollingUtil.validateTimeout;

/**
 * INTERNAL PACKAGE PRIVATE CLASS
 * <p>
 * Default implementation of {@link SyncPoller} that uses blocking reactor call underneath. The DefaultSyncPoller is not
 * thread safe, but we make every attempt to be safe in cases it is possible to be so, e.g. by using volatile and
 * copying context.
 *
 * @param <T> The type of poll response value
 * @param <U> The type of the final result of the long-running operation
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
     * @param syncActivationOperation the operation to synchronously activate (start) the long-running operation, this
     * operation will be called with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long-running operation, this parameter is
     * required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long-running operation if
     * service supports cancellation, this parameter is required and if service does not support cancellation then the
     * implementer should return {@link Mono#error(Throwable)} with an error message indicating absence of cancellation
     * support, the operation will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of the
     * long-running operation if service support it, this parameter is required and operation will be called current
     * {@link PollingContext}, if service does not have an api to fetch final result and if final result is same as
     * final poll response value then implementer can choose to simply return value from provided final poll response.
     */
    SyncOverAsyncPoller(Duration pollInterval, Function<PollingContext<T>, PollResponse<T>> syncActivationOperation,
        Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
        BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
        Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw LOGGER.logExceptionAsWarning(
                new IllegalArgumentException("Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        Objects.requireNonNull(syncActivationOperation, "'syncActivationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation
            = Objects.requireNonNull(fetchResultOperation, "'fetchResultOperation' cannot be null.");
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
        return this.pollOperation.apply(this.pollingContext).map(response -> {
            this.pollingContext.setLatestResponse(response);
            if (response.getStatus().isComplete()) {
                this.terminalPollContext = this.pollingContext.copy();
            }

            return response;
        }).block();
    }

    @Override
    public PollResponse<T> waitForCompletion() {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            return currentTerminalPollContext.getLatestResponse();
        }

        PollingContext<T> context = this.pollingContext.copy();
        return PollingUtil.pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
            .last()
            .map(response -> {
                this.terminalPollContext = context;
                return PollingUtil.toPollResponse(response);
            })
            .block();
    }

    @Override
    public PollResponse<T> waitForCompletion(Duration timeout) {
        validateTimeout(timeout, LOGGER);

        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            return currentTerminalPollContext.getLatestResponse();
        }

        PollingContext<T> context = this.pollingContext.copy();
        return PollingUtil.pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
            .take(timeout) // take with a timeout to halt the loop once the timeout period elapses
            .switchIfEmpty(Mono.error(() -> new TimeoutException("Polling didn't complete before the timeout period.")))
            .last()
            .flatMap(response -> {
                if (response != null && response.getStatus().isComplete()) {
                    this.terminalPollContext = context;
                    return Mono.just(PollingUtil.toPollResponse(response));
                } else {
                    return Mono.error(new TimeoutException("Polling didn't complete before the timeout period."));
                }
            })
            .block();
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor) {
        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            // Don't attempt to waitUntil status as it will never happen.
            return currentTerminalPollContext.getLatestResponse();
        }

        PollingContext<T> context = this.pollingContext.copy();
        return PollingUtil.pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
            .takeUntil(apr -> PollingUtil.matchStatus(apr, statusToWaitFor)) // take until terminal status
            .last()
            .map(response -> {
                if (response.getStatus().isComplete()) {
                    this.terminalPollContext = context;
                }
                return PollingUtil.toPollResponse(response);
            })
            .block();
    }

    @Override
    public PollResponse<T> waitUntil(Duration timeout, LongRunningOperationStatus statusToWaitFor) {
        validateTimeout(timeout, LOGGER);

        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            // Don't attempt to waitUntil status as it will never happen.
            return currentTerminalPollContext.getLatestResponse();
        }

        PollingContext<T> context = this.pollingContext.copy();
        return PollingUtil.pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
            .take(timeout) // take until the timeout happens
            .takeUntil(apr -> PollingUtil.matchStatus(apr, statusToWaitFor)) // take until terminal status
            .takeLast(1)
            .flatMap(response -> {
                if (response.getStatus().isComplete()) {
                    this.terminalPollContext = context;
                }
                return Mono.just(PollingUtil.toPollResponse(response));
            })
            .switchIfEmpty(Mono.fromCallable(this.pollingContext::getLatestResponse))
            .blockLast();
    }

    @Override
    public U getFinalResult() {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            return this.fetchResultOperation.apply(currentTerminalPollContext).block();
        }

        PollingContext<T> context = this.pollingContext.copy();
        return PollingUtil.pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
            .last()
            .flatMap(response -> {
                this.terminalPollContext = context;
                return response.getFinalResult();
            })
            .block();
    }

    @Override
    public U getFinalResult(Duration timeout) {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null) {
            // If the terminal poll context is not null, then the operation has already completed.
            return this.fetchResultOperation.apply(currentTerminalPollContext).block();
        }

        PollingContext<T> context = this.pollingContext.copy();
        return PollingUtil.pollingLoopAsync(context, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
            .take(timeout) // take with a timeout to halt the loop once the timeout period elapses
            .switchIfEmpty(Mono.error(() -> new TimeoutException("Polling didn't complete before the timeout period.")))
            .last()
            .flatMap(response -> {
                if (response != null && response.getStatus().isComplete()) {
                    this.terminalPollContext = context;
                    return response.getFinalResult();
                } else {
                    return Mono.error(new TimeoutException("Polling didn't complete before the timeout period."));
                }
            })
            .block();
    }

    @Override
    public void cancelOperation() {
        PollingContext<T> context1 = this.pollingContext.copy();
        if (context1.getActivationResponse() == context1.getLatestResponse()) {
            this.cancelOperation.apply(context1, context1.getActivationResponse()).block();
        } else {
            this.cancelOperation.apply(null, this.activationResponse)
                .onErrorResume(PollContextRequiredException.class, crp -> {
                    PollingContext<T> context2 = this.pollingContext.copy();
                    return PollingUtil
                        .pollingLoopAsync(context2, pollOperation, cancelOperation, fetchResultOperation, pollInterval)
                        .next()
                        .then(this.cancelOperation.apply(context2, this.activationResponse));
                })
                .block();
        }
    }

    @Override
    public SyncPoller<T, U> setPollInterval(Duration pollInterval) {
        this.pollInterval = validatePollInterval(pollInterval, LOGGER);
        return this;
    }
}
