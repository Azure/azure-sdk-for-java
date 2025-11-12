// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.polling;

import com.azure.core.management.implementation.polling.PollingState;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.SerializerAdapter;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Azure Resource Manager (ARM) Long-Running Operation SyncPoller implementation with continuation token support.
 * <p>
 * This implementation wraps a standard {@link SyncPoller} and adds ARM-specific functionality including
 * the ability to serialize the poller state to a continuation token and resume from such a token.
 * <p>
 * This class is package-private and should only be created through {@link SyncPollerFactory}.
 *
 * @param <T> The type of poll response value.
 * @param <U> The type of the final result of long-running operation.
 */
final class ArmLroSyncPoller<T, U> implements SyncPoller<PollResult<T>, U> {
    private static final ClientLogger LOGGER = new ClientLogger(ArmLroSyncPoller.class);

    private final SyncPoller<PollResult<T>, U> innerPoller;
    private final SerializerAdapter serializerAdapter;
    // We'll need a way to access the PollingContext from the inner poller
    // Since SimpleSyncPoller doesn't expose it, we'll need to track it ourselves
    private final PollingContextAccessor<PollResult<T>> contextAccessor;

    /**
     * Functional interface to access the PollingContext from a poller.
     */
    @FunctionalInterface
    interface PollingContextAccessor<T> {
        PollingContext<T> getContext();
    }

    /**
     * Creates an ArmLroSyncPoller.
     *
     * @param innerPoller The underlying SyncPoller implementation.
     * @param serializerAdapter The serializer for encoding/decoding.
     * @param contextAccessor Accessor to get the current PollingContext.
     */
    ArmLroSyncPoller(SyncPoller<PollResult<T>, U> innerPoller, SerializerAdapter serializerAdapter,
        PollingContextAccessor<PollResult<T>> contextAccessor) {
        this.innerPoller = Objects.requireNonNull(innerPoller, "'innerPoller' cannot be null.");
        this.serializerAdapter = Objects.requireNonNull(serializerAdapter, "'serializerAdapter' cannot be null.");
        this.contextAccessor = Objects.requireNonNull(contextAccessor, "'contextAccessor' cannot be null.");
    }

    @Override
    public PollResponse<PollResult<T>> poll() {
        return innerPoller.poll();
    }

    @Override
    public PollResponse<PollResult<T>> waitForCompletion() {
        return innerPoller.waitForCompletion();
    }

    @Override
    public PollResponse<PollResult<T>> waitForCompletion(Duration timeout) {
        return innerPoller.waitForCompletion(timeout);
    }

    @Override
    public PollResponse<PollResult<T>>
        waitUntil(com.azure.core.util.polling.LongRunningOperationStatus statusToWaitFor) {
        return innerPoller.waitUntil(statusToWaitFor);
    }

    @Override
    public PollResponse<PollResult<T>> waitUntil(Duration timeout,
        com.azure.core.util.polling.LongRunningOperationStatus statusToWaitFor) {
        return innerPoller.waitUntil(timeout, statusToWaitFor);
    }

    @Override
    public U getFinalResult() {
        return innerPoller.getFinalResult();
    }

    @Override
    public U getFinalResult(Duration timeout) {
        return innerPoller.getFinalResult(timeout);
    }

    @Override
    public void cancelOperation() {
        innerPoller.cancelOperation();
    }

    @Override
    public SyncPoller<PollResult<T>, U> setPollInterval(Duration pollInterval) {
        innerPoller.setPollInterval(pollInterval);
        return this;
    }

    @Override
    public String serializeContinuationToken() {
        try {
            PollingContext<PollResult<T>> context = contextAccessor.getContext();
            PollingState pollingState = PollingState.from(serializerAdapter, context);
            return pollingState.toContinuationToken();
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Failed to serialize continuation token. "
                + "The poller may not have been started or the polling state may be unavailable.", e));
        }
    }
}
