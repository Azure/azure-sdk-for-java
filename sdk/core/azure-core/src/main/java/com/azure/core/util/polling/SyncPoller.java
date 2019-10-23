// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;
import java.time.Duration;

/**
 * Type for doing synchronous polling of a long-running operation.
 *
 * @param <T> The type of poll response value.
 * @param <U> The type of the final result of long-running operation.
 */
public interface SyncPoller<T, U> {
    /**
     * Poll once and return the poll response received.
     *
     * @return the poll response
     */
    PollResponse<T> poll();

    /**
     * Wait for polling to complete. The polling is considered complete based on status defined in
     * {@link LongRunningOperationStatus}.
     *
     * @return the final poll response
     */
    PollResponse<T> waitForCompletion();

    /**
     * Wait for polling to complete with a timeout. The polling is considered complete based on
     * status defined in {@link LongRunningOperationStatus}.
     *
     * @param timeout the duration to waits for polling completion.
     * @return the final poll response
     * @throws java.util.concurrent.TimeoutException TimeoutException will be thrown if polling
     *         does not complete within the given {@link Duration}.
     */
    PollResponse<T> waitForCompletion(Duration timeout);

    /**
     * Wait until the given {@link LongRunningOperationStatus} is received.
     *
     * @param statusToWaitFor the desired {@link LongRunningOperationStatus} to block for.
     * @return {@link PollResponse} whose {@link PollResponse#getStatus()} matches {@code statusToWaitFor}.
     * @throws IllegalArgumentException if {@code timeout} is zero or negative and if {@code statusToWaitFor} is
     * {@code null}.
     */
    PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor);

    /**
     * Wait until the given {@link LongRunningOperationStatus} is received.
     *
     * @param statusToWaitFor the desired {@link LongRunningOperationStatus} to block for.
     * @param timeout the duration to waits for the polling.
     * @return {@link PollResponse} whose {@link PollResponse#getStatus()} matches {@code statusToWaitFor}.
     * @throws java.util.concurrent.TimeoutException TimeoutException will be thrown if polling
     *         does not find the matching status within the given {@link Duration}.
     */
    PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor, Duration timeout);

    /**
     * Retrieve the final result of long running operation.
     *
     * @return the final result of long-running operation if there is one
     */
    U getFinalResult();

    /**
     * cancels the remote long-running operation if cancellation is supported by the service.
     */
    void cancelOperation();
}
