// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Default implementation of {@link SyncPoller}.
 *
 * @param <T> The type of poll response value
 * @param <U> The type of the final result of the long running operation
 */
public final class DefaultSyncPoller<T, U> implements SyncPoller<T, U> {
    private final SyncPoller<T, U> syncPoller;

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
    public DefaultSyncPoller(Duration pollInterval,
                            Function<PollingContext<T>, PollResponse<T>> syncActivationOperation,
                            Function<PollingContext<T>, PollResponse<T>> pollOperation,
                            BiFunction<PollingContext<T>, PollResponse<T>, T> cancelOperation,
                            Function<PollingContext<T>, U> fetchResultOperation) {

        syncPoller = new SimpleSyncPoller<>(pollInterval, syncActivationOperation, pollOperation, cancelOperation,
            fetchResultOperation);
    }


    /**
     * Creates an instance of DefaultSyncPoller.
     * @param syncPoller the poller instance to use.
     */
    DefaultSyncPoller(SyncPoller<T, U> syncPoller) {
        this.syncPoller = syncPoller;
    }

    @Override
    public PollResponse<T> poll() {
        return syncPoller.poll();
    }

    @Override
    public PollResponse<T> waitForCompletion() {
        return syncPoller.waitForCompletion();
    }

    @Override
    public PollResponse<T> waitForCompletion(Duration timeout) {
        return syncPoller.waitForCompletion();
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor) {
        return syncPoller.waitUntil(statusToWaitFor);
    }

    @Override
    public PollResponse<T> waitUntil(Duration timeout, LongRunningOperationStatus statusToWaitFor) {
        return syncPoller.waitUntil(timeout, statusToWaitFor);
    }

    @Override
    public U getFinalResult() {
        return syncPoller.getFinalResult();
    }

    @Override
    public void cancelOperation() {
        syncPoller.cancelOperation();
    }

    @Override
    public SyncPoller<T, U> setPollInterval(Duration pollInterval) {
        return syncPoller.setPollInterval(pollInterval);
    }

    @SuppressWarnings("unchecked")
    static <T, U> DefaultSyncPoller<T, U> createSyncOverAsyncPoller(Duration pollInterval,
            Function<PollingContext<T>, PollResponse<T>> syncActivationOperation,
            Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
            BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
            Function<PollingContext<T>, Mono<U>> fetchResultOperation)  {
        return new DefaultSyncPoller<>(new SyncOverAsyncPoller<>(pollInterval, syncActivationOperation, pollOperation,
            cancelOperation, fetchResultOperation));
    }
}
