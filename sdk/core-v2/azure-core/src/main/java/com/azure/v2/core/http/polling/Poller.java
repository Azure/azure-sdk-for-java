// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.http.polling;

import com.azure.v2.core.implementation.ImplUtils;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.CoreException;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A type that offers API that simplifies the task of executing long-running operations against an Azure service.
 *
 * <p>
 * It provides the following functionality:
 * <ul>
 * <li>Querying the current state of the long-running operation.</li>
 * <li>Requesting cancellation of long-running operation, if supported by the service.</li>
 * <li>Fetching final result of long-running operation, if supported by the service.</li>
 * <li>Wait for long-running operation to complete, with optional timeout.</li>
 * <li>Wait for long-running operation to reach a specific state.</li>
 * </ul>
 *
 * @param <T> The type of poll response value.
 * @param <U> The type of the final result of long-running operation.
 */
public interface Poller<T, U> {
    /**
     * Poll once and return the poll response received.
     *
     * @return the poll response
     */
    PollResponse<T> poll();

    /**
     * Wait for polling to complete. The polling is considered complete based on status defined in
     * {@link LongRunningOperationStatus}.
     * <p>
     * This operation will wait indefinitely until a completed {@link LongRunningOperationStatus} is received.
     *
     * @return the final poll response
     */
    PollResponse<T> waitForCompletion();

    /**
     * Wait for polling to complete with a timeout. The polling is considered complete based on status defined in
     * {@link LongRunningOperationStatus} or if the timeout expires.
     * <p>
     * Polling will continue until a completed {@link LongRunningOperationStatus} is received or the timeout expires.
     * <p>
     * The {@code timeout} is applied in two ways, first it's used during each poll operation to time it out if the
     * polling operation takes too long. Second, it's used to determine when the wait for should stop. If polling
     * doesn't reach a completion state before the {@code timeout} elapses a {@link RuntimeException} wrapping a
     * {@link TimeoutException} will be thrown.
     *
     * @param timeout the duration to wait for polling completion.
     * @return the final poll response.
     * @throws NullPointerException If {@code timeout} is null.
     * @throws IllegalArgumentException If {@code timeout} is zero or negative.
     * @throws RuntimeException If polling doesn't complete before the {@code timeout} elapses.
     * ({@link RuntimeException#getCause()} should be a {@link TimeoutException}).
     */
    PollResponse<T> waitForCompletion(Duration timeout);

    /**
     * Wait for the given {@link LongRunningOperationStatus} to receive.
     * <p>
     * This operation will wait indefinitely until the {@code statusToWaitFor} is received or a
     * {@link LongRunningOperationStatus#isComplete()} state is reached.
     *
     * @param statusToWaitFor the desired {@link LongRunningOperationStatus} to block for.
     * @return {@link PollResponse} whose {@link PollResponse#getStatus()} matches {@code statusToWaitFor} or is
     * {@link LongRunningOperationStatus#isComplete()}.
     * @throws NullPointerException if {@code statusToWaitFor} is {@code null}.
     */
    PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor);

    /**
     * Wait for the given {@link LongRunningOperationStatus} with a timeout.
     * <p>
     * Polling will continue until a response is returned with a {@link LongRunningOperationStatus} matching
     * {@code statusToWaitFor}, a {@link LongRunningOperationStatus#isComplete()} state is reached, or the timeout
     * expires.
     * <p>
     * Unlike {@link #waitForCompletion(Duration)} or {@link #getFinalResult(Duration)}, when the timeout elapses a
     * {@link RuntimeException} wrapping a {@link TimeoutException} will not be thrown. Instead, the last poll response
     * will be returned. This is because unlike a completion state, a wait for state may be skipped if the state
     * is reached and completed before a poll operation is executed. For example, if a long-running operation has the
     * flow {@code A -> B -> C -> D} and the {@code statusToWaitFor} is {@code B} and the first poll request returns
     * state {@code A} but in the time between polls state {@code B} completes, then the next poll request will return
     * state {@code C} and the {@code statusToWaitFor} will never be returned.
     * <p>
     * This may return null if no poll operation completes within the timeout.
     *
     * @param timeout the duration to wait for the polling.
     * @param statusToWaitFor the desired {@link LongRunningOperationStatus} to block for.
     * @return {@link PollResponse} whose {@link PollResponse#getStatus()} matches {@code statusToWaitFor}, or null if
     * no response was returned within the timeout.
     * @throws NullPointerException if {@code statusToWaitFor} or {@code timeout} is {@code null}.
     * @throws IllegalArgumentException if {@code timeout} is zero or negative.
     */
    PollResponse<T> waitUntil(Duration timeout, LongRunningOperationStatus statusToWaitFor);

    /**
     * Retrieve the final result of the long-running operation.
     * <p>
     * If polling hasn't completed this will wait indefinitely until polling completes.
     *
     * @return the final result of the long-running operation if there is one.
     */
    U getFinalResult();

    /**
     * Retrieve the final result of the long-running operation.
     * <p>
     * If polling hasn't completed this will wait for the {@code timeout} for polling to complete. In this case this
     * API is effectively equivalent to {@link #waitForCompletion(Duration)} + {@link #getFinalResult()}.
     * <p>
     * Polling will continue until a completed {@link LongRunningOperationStatus} is received or the timeout expires.
     * <p>
     * The {@code timeout} is applied in two ways, first it's used during each poll operation to time it out if the
     * polling operation takes too long. Second, it's used to determine when the wait for should stop. If polling
     * doesn't reach a completion state before the {@code timeout} elapses a {@link RuntimeException} wrapping a
     * {@link TimeoutException} will be thrown.
     * <p>
     * If this method isn't overridden by the implementation then this method is effectively equivalent to calling
     * {@link #waitForCompletion(Duration)} then {@link #getFinalResult()}.
     *
     * @param timeout the duration to wait for polling completion.
     * @return the final result of the long-running operation if there is one.
     * @throws NullPointerException If {@code timeout} is null.
     * @throws IllegalArgumentException If {@code timeout} is zero or negative.
     * @throws RuntimeException If polling doesn't complete before the {@code timeout} elapses.
     * ({@link RuntimeException#getCause()} should be a {@link TimeoutException}).
     */
    default U getFinalResult(Duration timeout) {
        return getFinalResult();
    }

    /**
     * Cancels the remote long-running operation if cancellation is supported by the service.
     * <p>
     * If cancellation isn't supported by the service this will throw an exception.
     *
     * @throws RuntimeException if the operation does not support cancellation.
     */
    void cancelOperation();

    /**
     * Sets the poll interval for this poller. The new interval will be used for all subsequent polling operations
     * including the polling operations that are already in progress.
     *
     * @param pollInterval The new poll interval for this poller.
     * @return The updated instance of {@link Poller}.
     * @throws NullPointerException if the {@code pollInterval} is null.
     * @throws IllegalArgumentException if the {@code pollInterval} is zero or negative.
     */
    default Poller<T, U> setPollInterval(Duration pollInterval) {
        // This method is made default to prevent breaking change to the interface.
        // no-op
        return this;
    }

    /**
     * Creates default SyncPoller.
     *
     * @param pollInterval the polling interval.
     * @param syncActivationOperation the operation to synchronously activate (start) the long-running operation, this
     * operation will be called with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long-running operation, this parameter is
     * required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long-running operation if
     * service supports cancellation, this parameter is required and if service does not support cancellation then the
     * implementer should throw an exception with an error message indicating absence of cancellation support, the
     * operation will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of the
     * long-running operation if service support it, this parameter is required and operation will be called current
     * {@link PollingContext}, if service does not have an api to fetch final result and if final result is same as
     * final poll response value then implementer can choose to simply return value from provided final poll response.
     * @param <T> The type of poll response value.
     * @param <U> The type of the final result of long-running operation.
     * @return new {@link Poller} instance.
     * @throws NullPointerException if {@code pollInterval}, {@code syncActivationOperation}, {@code pollOperation},
     * {@code cancelOperation} or {@code fetchResultOperation} is {@code null}.
     * @throws IllegalArgumentException if {@code pollInterval} is zero or negative.
     */
    static <T, U> Poller<T, U> createPoller(Duration pollInterval,
        Function<PollingContext<T>, PollResponse<T>> syncActivationOperation,
        Function<PollingContext<T>, PollResponse<T>> pollOperation,
        BiFunction<PollingContext<T>, PollResponse<T>, T> cancelOperation,
        Function<PollingContext<T>, U> fetchResultOperation) {
        return new SimplePoller<>(pollInterval, syncActivationOperation, pollOperation, cancelOperation,
            fetchResultOperation);
    }

    /**
     * Creates PollerFlux.
     * <p>
     * This method uses a {@link PollingStrategy} to poll the status of a long-running operation after the
     * activation operation is invoked. See {@link PollingStrategy} for more details of known polling strategies and
     * how to create a custom strategy.
     *
     * @param pollInterval the polling interval
     * @param initialOperation the activation operation to activate (start) the long-running operation. This operation
     * will be invoked at most once across all subscriptions. This parameter is required. If there is no specific
     * activation work to be done then invocation should return null, this operation will be called with a new
     * {@link PollingContext}.
     * @param strategy a known synchronous strategy for polling a long-running operation in Azure
     * @param pollResponseType the {@link Type} of the response type from a polling call, or BinaryData if raw
     * response body should be kept. This should match the generic parameter {@link U}.
     * @param resultType the {@link Type} of the final result object to deserialize into, or BinaryData if raw
     * response body should be kept. This should match the generic parameter {@link U}.
     * @param <T> The type of poll response value.
     * @param <U> The type of the final result of long-running operation.
     * @return new {@link Poller} instance.
     * @throws NullPointerException if {@code pollInterval}, {@code initialOperation}, {@code strategy},
     * {@code pollResponseType} or {@code resultType} is {@code null}.
     * @throws IllegalArgumentException if {@code pollInterval} is zero or negative.
     */
    static <T, U> Poller<T, U> createPoller(Duration pollInterval, Supplier<Response<T>> initialOperation,
        PollingStrategy<T, U> strategy, Type pollResponseType, Type resultType) {
        Function<PollingContext<T>, PollResponse<T>> syncActivationOperation = pollingContext -> {
            Response<T> response = initialOperation.get();
            if (!strategy.canPoll(response)) {
                throw ImplUtils.getPollerLogger()
                    .throwableAtError()
                    .addKeyValue("pollingStrategy", strategy.getClass().getCanonicalName())
                    .log("Cannot poll with provided strategy.", CoreException::from);
            }

            return strategy.onInitialResponse(response, pollingContext, pollResponseType);
        };

        Function<PollingContext<T>, PollResponse<T>> pollOperation
            = context -> strategy.poll(context, pollResponseType);
        BiFunction<PollingContext<T>, PollResponse<T>, T> cancelOperation = strategy::cancel;
        Function<PollingContext<T>, U> fetchResultOperation = context -> strategy.getResult(context, resultType);

        return createPoller(pollInterval, syncActivationOperation, pollOperation, cancelOperation,
            fetchResultOperation);
    }

}
