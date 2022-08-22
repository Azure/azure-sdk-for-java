// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * SyncPollResponse represents an event emitted by the {@link SimpleSyncPoller} that synchronously polls
 * a long-running operation (LRO). An SyncPollResponse event provides information such as the current
 * {@link LongRunningOperationStatus status} of the LRO, any {@link #getValue value} returned
 * by the poll, as well as other useful information provided by the service.
 * AsyncPollResponse also exposes {@link #cancelOperation} method to cancel the long-running operation
 * from reactor operator chain and {@link #getFinalResult()} method that returns final result of
 * the long-running operation.
 *
 * @param <T> The type of poll response value.
 * @param <U> The type of the final result of long-running operation.
 */
final class SyncPollResponse<T, U> {
    // AsyncPollResponse is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(SyncPollResponse.class);
    private final PollingContext<T> pollingContext;
    private final BiFunction<PollingContext<T>, PollResponse<T>, T> cancellationOperation;
    private final Function<PollingContext<T>, U> fetchResultOperation;
    private final PollResponse<T> pollResponse;

    /**
     * Creates SyncPollResponse.
     *
     * @param pollingContext the polling context
     * @param cancellationOperation the cancellation operation if supported by the service
     * @param fetchResultOperation the operation to fetch final result of long-running operation, if supported
     *                             by the service
     */
    SyncPollResponse(PollingContext<T> pollingContext,
                     BiFunction<PollingContext<T>, PollResponse<T>, T> cancellationOperation,
                     Function<PollingContext<T>, U> fetchResultOperation) {
        this.pollingContext = Objects.requireNonNull(pollingContext,
                "'pollingContext' cannot be null.");
        this.cancellationOperation = Objects.requireNonNull(cancellationOperation,
                "'cancellationOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
                "'fetchResultOperation' cannot be null.");
        this.pollResponse = this.pollingContext.getLatestResponse();
    }

    /**
     * Represents the status of the long-running operation at the time the last polling operation finished successfully.
     * @return A {@link LongRunningOperationStatus} representing the result of the poll operation.
     */
    public LongRunningOperationStatus getStatus() {
        return pollResponse.getStatus();
    }

    /**
     * The value returned as a result of the last successful poll operation. This can be any custom user defined object,
     * or null if no value was returned from the service.
     *
     * @return T result of poll operation.
     */
    public T getValue() {
        return pollResponse.getValue();
    }

    /**
     * Cancels the remote long-running operation if cancellation is supported by the service.
     *
     * @return T result of cancel operation.
     */
    public T cancelOperation() {
        try {
            return this.cancellationOperation
                    .apply(this.pollingContext, this.pollingContext.getActivationResponse());
        } catch (RuntimeException ex) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(ex));
        }
    }

    /**
     * Fetches the final result of long-running operation if it
     * is supported by the service. If the long-running operation is not completed, null will be returned.
     *
     * @return U result of fetch result operation.
     */
    public U getFinalResult() {
        if (!this.pollResponse.getStatus().isComplete()) {
            return null;
        } else {
            try {
                return this.fetchResultOperation
                        .apply(this.pollingContext);
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(Exceptions.propagate(ex));
            }
        }
    }

    /**
     * Returns the delay the service has requested until the next polling operation is performed. A null or negative
     * value will be taken to mean that the Poller should determine on its own when the next poll operation is
     * to occur.
     *
     * @return Duration How long to wait before next retry.
     */
    Duration getRetryAfter() {
        return pollResponse.getRetryAfter();
    }
}

