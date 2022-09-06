// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import reactor.core.publisher.Mono;

import java.time.Duration;
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
    private SyncPoller<T, U> syncPoller;

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
        syncPoller = new SyncOverAsyncPoller(pollInterval, syncActivationOperation, pollOperation, cancelOperation, fetchResultOperation);
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
}
