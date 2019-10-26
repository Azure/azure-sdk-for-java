// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.implementation.OperationRequirePollContext;
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
 *
 * @param <T> The type of poll response value
 * @param <U> The type of the final result of long-running operation
 */
final class DefaultSyncPoller<T, U> implements SyncPoller<T, U> {
    private final ClientLogger logger = new ClientLogger(DefaultSyncPoller.class);
    private final Duration defaultPollInterval;
    private final Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation;
    private final BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation;
    private final Function<PollingContext<T>, Mono<U>> fetchResultOperation;
    private final PollResponse<T> activationResponse;
    private volatile PollingContext<T> terminalPollContext;
    private final PollingContext<T> pollingContext = new PollingContext<>();

    /**
     * Creates DefaultSyncPoller.
     *
     * @param defaultPollInterval the default polling interval
     * @param activationOperation the activation operation to be invoked at most once across all subscriptions,
     *                            this parameter is required, if there is no specific activation work to be
     *                            done then invocation should return Mono.empty().
     * @param pollOperation the operation to poll the current state of long running operation, this parameter
     *                      is required and the operation will be called with the {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long-running operation
     *                        if service supports cancellation, this parameter is required and if service does not
     *                        support cancellation then the implementer should return Mono.error with an error message
     *                        indicating absence of cancellation support, the operation will be called with
     *                        {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of
     *                             the long-running operation if service support it, this parameter is required and
     *                             operation will be called with {@link PollingContext}, if service does not have an
     *                             api to fetch final result and if final result is same as final poll response value
     *                             then implementer can choose to simply return value from provided final poll response.
     */
    DefaultSyncPoller(Duration defaultPollInterval,
                             Function<PollingContext<T>, Mono<T>> activationOperation,
                             Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
                             BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
                             Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        if (defaultPollInterval.compareTo(Duration.ZERO) <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        this.defaultPollInterval = defaultPollInterval;
        Objects.requireNonNull(activationOperation, "'activationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
            "'fetchResultOperation' cannot be null.");
        this.activationResponse = new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
            activationOperation.apply(this.pollingContext).block());
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
            this.terminalPollContext = this.pollingContext.clone();
        }
        return response;
    }

    @Override
    public PollResponse<T> waitForCompletion() {
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null
                && currentTerminalPollContext.getLatestResponse().getStatus().isComplete()) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.clone();
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
        if (currentTerminalPollContext != null
                && currentTerminalPollContext.getLatestResponse().getStatus().isComplete()) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.clone();
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
        if (statusToWaitFor == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for status is not allowed."));
        }
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null
                && currentTerminalPollContext.getLatestResponse().getStatus() == statusToWaitFor) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.clone();
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
        PollingContext<T> currentTerminalPollContext = this.terminalPollContext;
        if (currentTerminalPollContext != null
                && currentTerminalPollContext.getLatestResponse().getStatus() == statusToWaitFor) {
            return currentTerminalPollContext.getLatestResponse();
        } else {
            PollingContext<T> context = this.pollingContext.clone();
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
            PollingContext<T> context = this.pollingContext.clone();
            AsyncPollResponse<T, U> finalAsyncPollResponse = pollingLoop(context)
                    .blockLast();
            this.terminalPollContext = context;
            return finalAsyncPollResponse.getFinalResult().block();
        }
    }

    @Override
    public void cancelOperation() {
        PollingContext<T> context1 = this.pollingContext.clone();
        if (context1.getActivationResponse() == context1.getLatestResponse()) {
            this.cancelOperation.apply(context1, context1.getActivationResponse())
                .block();
        } else {
            try {
                this.cancelOperation.apply(null, this.activationResponse).block();
            } catch (OperationRequirePollContext crp) {
                PollingContext<T> context2 = this.pollingContext.clone();
                pollingLoop(context2)
                    .next()
                    .block();
                this.cancelOperation
                    .apply(context2, this.activationResponse)
                    .block();
            }
        }
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
            // set|read in state as needed, reactor guarantee thread-safety of state object.
            cxt -> Mono.defer(() -> pollOperation.apply(cxt))
                    .delaySubscription(getDelay(cxt.getLatestResponse()))
                    .switchIfEmpty(Mono.error(new IllegalStateException("PollOperation returned Mono.empty().")))
                    .repeat()
                    .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
                    .onErrorResume(throwable -> {
                        logger.warning("Received an error from pollOperation. Any error from pollOperation "
                               + "will be ignored and polling will be continued. Error:" + throwable.getMessage());
                        return Mono.empty();
                    })
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
            return this.defaultPollInterval;
        } else {
            return retryAfter.compareTo(Duration.ZERO) > 0
                    ? retryAfter
                    : this.defaultPollInterval;
        }
    }
}
