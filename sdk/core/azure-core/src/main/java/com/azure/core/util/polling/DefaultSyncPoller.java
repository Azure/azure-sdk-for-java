// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.implementation.PollContextRequiredException;
import reactor.core.publisher.Flux;
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
final class DefaultSyncPoller<T, U> implements SyncPoller<T, U> {
    private final ClientLogger logger = new ClientLogger(DefaultSyncPoller.class);
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
    DefaultSyncPoller(Duration pollInterval,
                             Function<PollingContext<T>, PollResponse<T>> syncActivationOperation,
                             Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
                             BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
                             Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
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
            AsyncPollResponse<T, U> finalAsyncPollResponse = pollingLoop(context)
                    .blockLast();
            PollResponse<T> response = toPollResponse(finalAsyncPollResponse);
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
            AsyncPollResponse<T, U> finalAsyncPollResponse = pollingLoop(context)
                    .timeout(timeout)
                    .blockLast();
            PollResponse<T> response = toPollResponse(finalAsyncPollResponse);
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
            AsyncPollResponse<T, U> asyncPollResponse = pollingLoop(context)
                .takeUntil(apr -> matchStatus(apr, statusToWaitFor))
                .last()
                .switchIfEmpty(Mono.error(new NoSuchElementException("Polling completed without receiving the given"
                        + "status '" + statusToWaitFor + "'.")))
                .block();
            PollResponse<T> response = toPollResponse(asyncPollResponse);
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
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for timeout is not allowed."));
        }
        Objects.requireNonNull(statusToWaitFor, "'statusToWaitFor' cannot be null.");
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null
                && currentTerminalPollContext.getLatestResponse().getStatus() == statusToWaitFor) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.copy();
            AsyncPollResponse<T, U> asyncPollResponse = pollingLoop(context)
                    .takeUntil(apr -> matchStatus(apr, statusToWaitFor))
                    .last()
                    .timeout(timeout)
                    .switchIfEmpty(Mono.error(new NoSuchElementException("Polling completed without receiving the given"
                            + "status '" + statusToWaitFor + "'.")))
                    .block();
            PollResponse<T> response = toPollResponse(asyncPollResponse);
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
            AsyncPollResponse<T, U> finalAsyncPollResponse = pollingLoop(context)
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
                pollingLoop(context2)
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
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'pollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        return this;
    }

    private static <T, U> PollResponse<T> toPollResponse(AsyncPollResponse<T, U> asyncPollResponse) {
        return new PollResponse<>(asyncPollResponse.getStatus(),
            asyncPollResponse.getValue(),
            asyncPollResponse.getRetryAfter());
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

    private Flux<AsyncPollResponse<T, U>> pollingLoop(PollingContext<T> pollingContext) {
        return Flux.using(
            // Create a Polling Context per subscription
            () -> pollingContext,
            // Do polling
            // set|read to|from context as needed, reactor guarantee thread-safety of cxt object.
            cxt -> Mono.defer(() -> pollOperation.apply(cxt))
                    .delaySubscription(getDelay(cxt.getLatestResponse()))
                    .switchIfEmpty(Mono.error(new IllegalStateException("PollOperation returned Mono.empty().")))
                    .repeat()
                    .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
                    .concatMap(currentPollResponse -> {
                        cxt.setLatestResponse(currentPollResponse);
                        return Mono.just(new AsyncPollResponse<>(cxt,
                                this.cancelOperation,
                                this.fetchResultOperation));
                    }),
            cxt -> { });
    }

    /**
     * Get the duration to wait before making next poll attempt.
     *
     * @param pollResponse the poll response to retrieve delay duration from
     * @return the delay
     */
    private Duration getDelay(PollResponse<T> pollResponse) {
        Duration retryAfter = pollResponse.getRetryAfter();
        if (retryAfter == null) {
            return this.pollInterval;
        } else {
            return retryAfter.compareTo(Duration.ZERO) > 0
                    ? retryAfter
                    : this.pollInterval;
        }
    }
}
